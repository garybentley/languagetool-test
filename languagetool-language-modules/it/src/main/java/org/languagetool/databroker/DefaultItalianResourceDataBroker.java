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
import java.util.LinkedHashSet;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;

import morfologik.stemming.Dictionary;

import org.languagetool.language.Italian;
import org.languagetool.UserConfig;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tagging.it.ItalianTagger;
import org.languagetool.tagging.MorfologikTagger;

public class DefaultItalianResourceDataBroker extends DefaultResourceDataBroker implements ItalianResourceDataBroker {

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

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/italian.dict";

    /**
     * The location of wrong words (words to be replaced) in the rules dir.  %1$s is replaced with the language code from the locale,
     * %2$s is the country.  This file is used in {@link getWrongWords()}.
     */
//    public static String CONTRACTIONS_FILE_NAME = "/en/contractions.txt";
//    public static String COMPOUNDS_FILE_NAME = "/en/compounds.txt";
//    public static String WRONG_WORDS_IN_CONTEXT_FILE_NAME = "/en/wrongWordInContext.txt";
//    public static String GENERAL_PATTERN_RULES_FILE_NAME = "/en/grammar.xml";
//    public static String LOCALE_VARIANT_PATTERN_RULES_FILE_NAME = "/en/en_%1$s/grammar.xml";

//    public static String STEMMER_DICT_FILE_NAME = "/%1$s/english_synth.dict";

    private WordTagger wordTagger;
    private Dictionary wordTaggerDictionary;
    private ItalianTagger tagger;
    private LuceneLanguageModel languageModel;
    private Set<Dictionary> dictionaries;

    public DefaultItalianResourceDataBroker(Italian lang, ClassLoader classLoader) throws Exception {
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

    /**
     * Get the tagger.
     *
     * @return The tagger.
     */
    @Override
    public ItalianTagger getTagger() throws Exception {
        if (tagger == null) {
              tagger = new ItalianTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
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
    public synchronized LuceneLanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
            languageModel = createLanguageModelFromResourcePath();
        }
        return languageModel;
    }

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return getSpellingIgnoreWordsFromResourcePath();
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

    /**
     * Close our resources.
     */
    @Override
    public void close() throws Exception {
        super.close();
        if (languageModel != null) {
            languageModel.close();
        }
    }

}
