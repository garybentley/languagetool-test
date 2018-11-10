/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Gary Bentley
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.databroker;

import org.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.Collections;
import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;

import morfologik.stemming.Dictionary;

import org.languagetool.language.Breton;
import org.languagetool.UserConfig;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.br.BretonWordTokenizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tagging.br.BretonTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tools.StringTools;

public class DefaultBretonResourceDataBroker extends DefaultResourceDataBroker implements BretonResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For it_IT this would become: it/hunspell/it_IT.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/breton.dict";

    public static String TOPOGRPHICAL_WRONG_WORDS_FILE_NAME = "%1$s/topo.txt";

    public static StringProcessor<Map<String, String>> topoWrongWordsProcessor = new StringProcessor<Map<String, String>>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.charAt(0) == '#';
        }
        @Override
        public Set<String> getErrors(String line) {
            Set<String> errors = null;
            line = line.trim();
            String[] parts = line.split("=");
            if (parts.length != 2) {
                errors = new HashSet<>();
                errors.add("Expected line to have format: <place>=<place>[|<place>...]");
            }
            return errors;
        }
        @Override
        public Map<String, String> getProcessed(String line) {
            line = line.trim();
            String[] parts = line.split("=");
            String[] wrongForms = parts[0].split("\\|"); // multiple incorrect forms
            Map<String, String> data = new HashMap<>();
            for (String wrongForm : wrongForms) {
                /*
                 GTODO Not sure this is doing anything useful
              int wordCount = 0;
              List<String> tokens = tokenizer.tokenize(wrongForm);
              for (String token : tokens) {
                if (!StringTools.isWhitespace(token)) {
                  wordCount++;
                }
              }
              */
              data.put(wrongForm, parts[1]);
            }
            return data;
        }
    };


    private WordTagger wordTagger;
    private Dictionary wordTaggerDictionary;
    private BretonTagger tagger;
    private Set<Dictionary> dictionaries;
    private BretonWordTokenizer wordTokenizer;
    private List<Map<String, String>> wrongTopoWords;

    public DefaultBretonResourceDataBroker(Breton lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
    * GTODO Tidy up doco
     * Return the dictionaries we use for spelling, files we use are:
     *   - /resource/<locale.language>/hunspell/<locale.language>_<locale.countrycode>.dict, this creates a binary hunspell dictionary.
     *   - /resource/<locale.language>/hunspell/spelling.txt, a text based dictionary.
     *   - /resource/<locale.language>/hunspell/spelling_<locale.language>-<locale.countrycode>.txt, a locale variant dictionary (optional)
     *   - /resource/<locale.language>/hunspell/<locale.language>_<locale.countrycode>.info, the info for text dictionary.
     *
     * The plain text and variant dictionary are merged to form a single extra dictionary.
     *
     * @return The dictionaries to use for the language locale.
     */
    @Override
    // GTODO Push this down into DefaultResourceDataBroker, pass the relevant filenames, en and de have slightly different naming for the variant files...
    public Set<Dictionary> getDictionaries(UserConfig userConfig) throws Exception {
        Set<Dictionary> dicts = new LinkedHashSet<>();
        String country = language.getLocale().getCountry();
        String lang = language.getLocale().getLanguage();
        String plainTextInfoFile = String.format(PLAIN_TEXT_SPELLING_INFO_FILE_NAME, lang, country);
        if (resourceDirPathExists(plainTextInfoFile) && userConfig != null) {
            // Create our user dictionary.
            List<String> userWords = userConfig.getAcceptedWords();
            if (userWords != null && userWords.size() > 0) {
                List<byte[]> lines = userWords.stream()
                                              .map(w -> w.getBytes(StandardCharsets.UTF_8))
                                              .collect(Collectors.toList());
                dicts.add (DefaultMorfologikDictionaryLoader.loadFromLines(lines, getResourceDirPathStream(plainTextInfoFile)));
            }
        }

        if (dictionaries == null) {
            // Try out binary file.
            Set<Dictionary> _dicts = new LinkedHashSet<>();
            String binDictFile = String.format(BINARY_DICT_FILE_NAME, lang, country);
            Dictionary binDict = getMorfologikBinaryDictionaryFromResourcePath(binDictFile);
            if (binDict != null) {
                _dicts.add(binDict);
            }

            List<String> availableFiles = new ArrayList<>();
            String spellingFile = String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, lang);
            if (resourceDirPathExists(spellingFile)) {
                availableFiles.add(spellingFile);
            }

            if (availableFiles.size() > 0) {
                Dictionary textDict = getMorfologikTextDictionaryFromResourcePaths(availableFiles, plainTextInfoFile, DEFAULT_CHARSET);
                if (textDict != null) {
                    _dicts.add(textDict);
                }
            }

            dictionaries = _dicts;
            dicts.addAll(_dicts);
        }
        dicts.addAll(dictionaries);
        return dicts;
    }

    @Override
    public List<Map<String, String>> getWrongTopographicalWords() throws Exception {
        if (wrongTopoWords == null) {
            List<Map<String, String>> list = new ArrayList<>();
            Path path = getRulesDirPath(String.format(TOPOGRPHICAL_WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage()));
            try (Scanner br = new Scanner(path, DEFAULT_CHARSET.name())) {
              String line;

              while (br.hasNextLine()) {
                line = br.nextLine().trim();
                if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
                  continue;
                }
                String[] parts = line.split("=");
                if (parts.length != 2) {
                  throw new IOException(String.format("Format error in line: %1$s, path: %2$s", line, path));
                }
                String[] wrongForms = parts[0].split("\\|"); // multiple incorrect forms
                for (String wrongForm : wrongForms) {
                  int wordCount = 0;
                  List<String> tokens = getWordTokenizer().tokenize(wrongForm);
                  for (String token : tokens) {
                    if (!StringTools.isWhitespace(token)) {
                      wordCount++;
                    }
                  }
                  // grow if necessary
                  for (int i = list.size(); i < wordCount; i++) {
                    list.add(new HashMap<>());
                  }
                  list.get(wordCount - 1).put(wrongForm, parts[1]);
                }
              }
            }
            // seal the result (prevent modification from outside this class)
            List<Map<String,String>> result = new ArrayList<>();
            for (Map<String, String> map : list) {
              result.add(Collections.unmodifiableMap(map));
            }
            wrongTopoWords = Collections.unmodifiableList(result);

            // GTODO This call isn't working... not entirely sure why.
            //wrongTopoWords = loadWordsFromRulesPath(String.format(TOPOGRPHICAL_WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, topoWrongWordsProcessor);
        }
        return wrongTopoWords;
    }

    @Override
    public BretonWordTokenizer getWordTokenizer() throws Exception {
        if (wordTokenizer == null) {
            wordTokenizer = new BretonWordTokenizer();
        }
        return wordTokenizer;
    }

    /**
     * Get the tagger.
     *
     * @return The tagger.
     */
    @Override
    public BretonTagger getTagger() throws Exception {
        if (tagger == null) {
              tagger = new BretonTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
        }
        return tagger;
    }

    /**
     * Get the sentence tokenizer.
     *
     * @return The sentence tokenizer.
     */
    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public WordTagger getWordTagger() throws Exception {
        if (wordTagger == null) {
            wordTagger = createWordTaggerFromResourcePath(new MorfologikTagger(getWordTaggerDictionary()), false);
        }
        return wordTagger;
    }

    public Dictionary getWordTaggerDictionary() throws Exception {
        if (wordTaggerDictionary == null) {
            wordTaggerDictionary = getMorfologikBinaryDictionaryFromResourcePath(String.format(WORD_TAGGER_DICT_FILE_NAME, language.getLocale().getLanguage()));
        }
        return wordTaggerDictionary;
    }

}
