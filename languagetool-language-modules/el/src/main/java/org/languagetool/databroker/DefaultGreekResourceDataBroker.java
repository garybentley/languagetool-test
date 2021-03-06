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
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;
import java.io.*;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import org.languagetool.language.Greek;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.el.GreekWordTokenizer;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.el.GreekTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.Synthesizer;

public class DefaultGreekResourceDataBroker extends DefaultResourceDataBroker implements GreekResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/greek.dict";

    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For el_GR this would become: /en/hunspell/el_GR.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String STEMMER_DICT_FILE_NAME = "%1$s/greek_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/greek_tags.txt";

    private SentenceTokenizer sentenceTokenizer;
    private GreekWordTokenizer wordTokenizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private Hunspell.Dictionary hunspellDict;
    private GreekTagger tagger;
    private BaseSynthesizer synthesizer;
    private IStemmer istemmer;
    private Map<String, List<String>> wrongWords;
    private Set<Dictionary> dictionaries;

    public DefaultGreekResourceDataBroker(Greek lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

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

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public Synthesizer getSynthesizer() throws Exception {
      if (synthesizer == null) {
        synthesizer = new BaseSynthesizer(getIStemmer(), getSynthesizerWordTags());
      }
      return synthesizer;
    }

    public Set<String> getSynthesizerWordTags() throws Exception {
        String file = String.format(SYNTHESIZER_WORD_TAGS_FILE_NAME, language.getLocale().getLanguage());
        return createSynthesizerWordTagsFromResourcePath(file);
    }

    public IStemmer getIStemmer() throws Exception {
        if (istemmer == null) {
            String file = String.format(STEMMER_DICT_FILE_NAME, language.getLocale().getLanguage());
            try {
                istemmer = new DictionaryLookup(getMorfologikBinaryDictionaryFromResourcePath(file));
            } catch(Exception e) {
                throw new Exception(String.format("Unable to load stemmer dictionary from: %1$s", file), e);
            }
        }
        return istemmer;
    }

    @Override
    public Tokenizer getWordTokenizer() {
      if (wordTokenizer == null) {
        wordTokenizer = new GreekWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public Tagger getTagger() throws Exception {
        if (tagger == null) {
            tagger = new GreekTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
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
        if (sentenceTokenizer == null) {
            sentenceTokenizer = getDefaultSentenceTokenizer();
        }
        return sentenceTokenizer;
    }

}
