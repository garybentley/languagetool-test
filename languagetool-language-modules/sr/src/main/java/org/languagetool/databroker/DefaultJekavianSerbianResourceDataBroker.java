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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;

import morfologik.stemming.Dictionary;
import morfologik.stemming.IStemmer;
import morfologik.stemming.DictionaryLookup;

import org.languagetool.language.JekavianSerbian;
import org.languagetool.UserConfig;
import org.languagetool.tagging.sr.JekavianTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.Synthesizer;

public class DefaultJekavianSerbianResourceDataBroker extends DefaultSerbianResourceDataBroker {

    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/dictionary/serbian.info";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For ro_RO this would become: ro/hunspell/ro_RO.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/dictionary/jekavian/serbian_hunspell.dict";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/dictionary/jekavian/spelling.txt";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/dictionary/jekavian/ignored.txt";
    public static String PROHIBITED_WORDS_FILE_NAME = "%1$s/dictionary/jekavian/prohibit.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/dictionary/jekavian/serbian.dict";

    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/dictionary/serbian_synth_tags.txt";
    public static String SYNTHESIZER_ADDED_TAGS_FILE_NAME = "%1$s/dictionary/jekavian/added.txt";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/dictionary/jekavian/serbian_synth.dict";

    public static String WORD_TAGGER_ADDED_WORDS_FILE_NAME = "%1$s/dictionary/jekavian/added.txt";
    public static String WORD_TAGGER_REMOVED_WORDS_FILE_NAME = "%1$s/dictionary/jekavian/removed.txt";

    public static String STYLE_WRONG_WORDS_FILE_NAME = "%1$s/jekavian/replace-style.txt";
    public static String GRAMMAR_WRONG_WORDS_FILE_NAME = "%1$s/jekavian/replace-grammar.txt";

    private List<String> spellingIgnoreWords;
    private Set<Dictionary> dictionaries;
    private Tagger tagger;
    private Synthesizer synthesizer;
    private Set<String> synthesizerTags;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private Map<String, List<String>> grammarWrongWords;
    private Map<String, List<String>> styleWrongWords;

    public DefaultJekavianSerbianResourceDataBroker(JekavianSerbian lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public WordTagger getWordTagger() throws Exception {
        if (wordTagger == null) {
            String manualRemovalFileName = String.format(WORD_TAGGER_REMOVED_WORDS_FILE_NAME, language.getLocale().getLanguage());
            String manualAdditionFileName = String.format(WORD_TAGGER_ADDED_WORDS_FILE_NAME, language.getLocale().getLanguage());

            Path manualRemovalPath = null;
            Path manualAdditionPath = null;
            if (resourceDirPathExists(manualRemovalFileName)) {
                manualRemovalPath = getResourceDirPath(manualRemovalFileName);
            }

            if (resourceDirPathExists(manualAdditionFileName)) {
               manualAdditionPath = getResourceDirPath(manualAdditionFileName);
            }
            wordTagger = createWordTagger(new MorfologikTagger(getWordTaggerDictionary()), manualAdditionPath, manualRemovalPath, false);
        }
        return wordTagger;
    }

    public Dictionary getWordTaggerDictionary() throws Exception {
        if (wordTaggerDictionary == null) {
            wordTaggerDictionary = getMorfologikBinaryDictionaryFromResourcePath(String.format(WORD_TAGGER_DICT_FILE_NAME, language.getLocale().getLanguage()));
        }
        return wordTaggerDictionary;
    }

    @Override
    public Tagger getTagger() throws Exception {
        return new JekavianTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
    }

    @Override
    public Synthesizer getSynthesizer() throws Exception {
      if (synthesizer == null) {
        synthesizer = new BaseSynthesizer(getIStemmer(), getSynthesizerWordTags());
      }
      return synthesizer;
    }

    public Set<String> getSynthesizerWordTags() throws Exception {
        if (synthesizerTags == null) {
            synthesizerTags = createSynthesizerWordTagsFromResourcePath(String.format(SYNTHESIZER_WORD_TAGS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return synthesizerTags;
    }

    @Nullable
    public IStemmer getIStemmer() throws Exception {
        if (istemmer == null) {
            String file = String.format(STEMMER_DICT_FILE_NAME, language.getLocale().getLanguage());
            if (resourceDirPathExists(file)) {
                try {
                    istemmer = new DictionaryLookup(getMorfologikBinaryDictionaryFromResourcePath(file));
                } catch(Exception e) {
                    throw new IOException(String.format("Unable to load stemmer dictionary from: %1$s", getResourceDirPath(file)), e);
                }
            }
        }
        return istemmer;
    }

    @Override
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
    public List<String> getSpellingIgnoreWords() throws Exception {
        if (spellingIgnoreWords == null) {
            spellingIgnoreWords = loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return spellingIgnoreWords;
    }

    @Override
    public List<String> getSpellingProhibitedWords() throws Exception {
        return loadSpellingProhibitedWordsFromResourcePath(String.format(PROHIBITED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public Map<String, List<String>> getStyleWrongWords() throws Exception {
        if (styleWrongWords == null) {
            String replaceFile = String.format(STYLE_WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
            styleWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return styleWrongWords;
    }

    @Override
    public Map<String, List<String>> getGrammarWrongWords() throws Exception {
        if (grammarWrongWords == null) {
            String replaceFile = String.format(GRAMMAR_WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
            grammarWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return grammarWrongWords;
    }

}
