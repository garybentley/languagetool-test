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

import org.languagetool.language.Galician;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.gl.GalicianWordTokenizer;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.gl.GalicianTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.tagging.disambiguation.gl.GalicianHybridDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.Synthesizer;

public class DefaultGalicianResourceDataBroker extends DefaultResourceDataBroker implements GalicianResourceDataBroker {

    public static String WRONG_WORDS_FILE_NAME = "%1$s/words.txt";

    public static String CAST_WORDS_FILE_NAME = "%1$s/spanish.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/galician.dict";

    public static String MANUAL_WORD_TAGGER_FILE_NAME = "%1$s/manual-tagger.txt";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s_%2$s";

    public static String STEMMER_DICT_FILE_NAME = "%1$s/galician_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/galician_tags.txt";

    public static String REDUNDANCY_WORDS_FILE_NAME = "%1$s/redundancies.txt";
    public static String WIKIPEDIA_WORDS_FILE_NAME = "%1$s/wikipedia.txt";
    public static String WORDINESS_FILE_NAME = "%1$s/wordiness.txt";
    public static String BARBARISMS_WORDS_FILE_NAME = "%1$s/barbarisms.txt";

    private SentenceTokenizer sentenceTokenizer;
    private GalicianWordTokenizer wordTokenizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private Hunspell.Dictionary hunspellDict;
    private GalicianTagger tagger;
    private BaseSynthesizer synthesizer;
    private GalicianHybridDisambiguator disambiguator;
    private IStemmer istemmer;
    private Map<String, List<String>> wrongWords;
    private Map<String, List<String>> castWords;
    private List<Map<String, String>> redundancyWords;
    private List<Map<String, String>> wikipediaWords;
    private List<Map<String, String>> barbarismsWords;
    private List<Map<String, String>> wordinessWords;

    public DefaultGalicianResourceDataBroker(Galician lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public Hunspell.Dictionary getHunspellDictionary() throws Exception {
        if (hunspellDict == null) {
            String fileName = String.format(HUNSPELL_BASE_FILE_NAME_PREFIX, language.getLocale().getLanguage(), language.getLocale().getCountry());
            hunspellDict = createHunspellDictionaryFromResourcePath(fileName);
        }
        return hunspellDict;
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
    public Disambiguator getDisambiguator() throws Exception {
      if (disambiguator == null) {
          disambiguator = new GalicianHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
      }
      return disambiguator;
    }

    @Override
    public Tokenizer getWordTokenizer() {
      if (wordTokenizer == null) {
        wordTokenizer = new GalicianWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public Tagger getTagger() throws Exception {
        if (tagger == null) {
            tagger = new GalicianTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
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

    /**
     * Get the wrong words for Galician.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getCastWords() throws Exception {
        if (wrongWords == null) {
            String file = String.format(CAST_WORDS_FILE_NAME, language.getLocale().getLanguage());
            wrongWords = createWrongWordsFromRulesPath(file);
        }
        return wrongWords;
    }

    /**
     * Get the wrong words for Galician.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getWrongWords() throws Exception {
        if (wrongWords == null) {
            String file = String.format(WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage());
            wrongWords = createWrongWordsFromRulesPath(file);
        }
        return wrongWords;
    }

    /**
     * Get the redundancy words for Galician.
     *
     * @return The words.
     */
    @Override
    public List<Map<String, String>> getRedundancyWords() throws Exception {
        if (redundancyWords == null) {
            String file = String.format(REDUNDANCY_WORDS_FILE_NAME, language.getLocale().getLanguage());
            redundancyWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return redundancyWords;
    }

    /**
     * Get the wikipedia words for Galician.
     *
     * @return The words.
     */
    @Override
    public List<Map<String, String>> getWikipediaWords() throws Exception {
        if (wikipediaWords == null) {
            String file = String.format(WIKIPEDIA_WORDS_FILE_NAME, language.getLocale().getLanguage());
            wikipediaWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return wikipediaWords;
    }

    /**
     * Get the wordiness words for Galician.
     *
     * @return The words.
     */
    @Override
    public List<Map<String, String>> getWordinessWords() throws Exception {
        if (wordinessWords == null) {
            String file = String.format(WORDINESS_FILE_NAME, language.getLocale().getLanguage());
            wordinessWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return wordinessWords;
    }

    /**
     * Get the barbarisms words for Galician.
     *
     * @return The words.
     */
    @Override
    public List<Map<String, String>> getBarbarismsWords() throws Exception {
        if (barbarismsWords == null) {
            String file = String.format(BARBARISMS_WORDS_FILE_NAME, language.getLocale().getLanguage());
            barbarismsWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return barbarismsWords;
    }

}
