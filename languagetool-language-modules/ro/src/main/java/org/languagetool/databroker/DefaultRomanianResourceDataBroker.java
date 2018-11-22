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

import org.languagetool.language.Romanian;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.ro.RomanianWordTokenizer;
import org.languagetool.tagging.ro.RomanianTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.synthesis.ManualSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ro.RomanianSynthesizer;
import org.languagetool.rules.CompoundRuleData;

public class DefaultRomanianResourceDataBroker extends DefaultResourceDataBroker implements RomanianResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For ml_IN this would become: /ml/hunspell/ml_IN.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For ro_RO this would become: ro/hunspell/ro_RO.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/romanian.dict";

    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/romanian_tags.txt";
    public static String SYNTHESIZER_ADDED_TAGS_FILE_NAME = "%1$s/added.txt";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/romanian_synth.dict";
    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";
    public static String WRONG_WORDS_FILE_NAME = "%1$s/replace.txt";

    private SentenceTokenizer sentenceTokenizer;
    private List<String> spellingIgnoreWords;
    private Set<Dictionary> dictionaries;
    private Tokenizer wordTokenizer;
    private Tagger tagger;
    private Synthesizer synthesizer;
    private Set<String> synthesizerTags;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private ManualSynthesizer addedTagsSynthesizer;
    private IStemmer istemmer;
    private CompoundRuleData compounds;
    private List<Map<String, String>> wrongWords;

    public DefaultRomanianResourceDataBroker(Romanian lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
     * Get the wrong words.
     *
     * @return A map of the words.
     */
    @Override
    public List<Map<String, String>> getWrongWords() throws Exception {
        if (wrongWords == null) {
            String file = String.format(WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage());
            wrongWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return wrongWords;
    }

    @Override
    public CompoundRuleData getCompounds() throws Exception {
        if (compounds == null) {
            String file = String.format(COMPOUNDS_FILE_NAME, language.getLocale().getLanguage());
            if (resourceDirPathExists(file)) {
                compounds = createCompoundRuleDataFromResourcePaths(file);
            }
        }
        return compounds;
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
    public Tagger getTagger() throws Exception {
        return new RomanianTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
    }

    @Override
    public Synthesizer getSynthesizer() throws Exception {
      if (synthesizer == null) {
        synthesizer = new RomanianSynthesizer(getMessageBundle(), getIStemmer(), getSynthesizerWordTags(), getSynthesizerAddedTagsSynthesizer());
      }
      return synthesizer;
    }

    public ManualSynthesizer getSynthesizerAddedTagsSynthesizer() throws Exception {
        String file = String.format(SYNTHESIZER_ADDED_TAGS_FILE_NAME, language.getLocale().getLanguage());
        if (resourceDirPathExists(file)) {
            try (InputStream is = Files.newInputStream(getResourceDirPath(file))) {
                addedTagsSynthesizer = new ManualSynthesizer(is);
            } catch(Exception e) {
                throw new IOException(String.format("Unable to load added synthesizer tags from: %1$s", getResourceDirPath(file)), e);
            }
        }
        return addedTagsSynthesizer;
    }

    public Set<String> getSynthesizerWordTags() throws Exception {
        if (synthesizerTags == null) {
            synthesizerTags = createSynthesizerWordTagsFromResourcePath(String.format(SYNTHESIZER_WORD_TAGS_FILE_NAME, language.getLocale().getLanguage()));
            ManualSynthesizer s = getSynthesizerAddedTagsSynthesizer();
            if (s != null) {
                synthesizerTags.addAll(s.getPossibleTags());
            }
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
    public Tokenizer getWordTokenizer() throws Exception {
        return new RomanianWordTokenizer();
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

    /**
     * Get the sentence tokenizer.
     *
     * @return The sentence tokenizer.
     */
    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

}
