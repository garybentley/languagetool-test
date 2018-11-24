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

import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.ArrayList;

import java.util.stream.Collectors;

import java.nio.charset.*;

import morfologik.stemming.IStemmer;
import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;

import org.languagetool.UserConfig;
import org.languagetool.language.Spanish;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tagging.disambiguation.es.SpanishHybridDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tokenizers.es.SpanishWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.languagemodel.LanguageModel;

public class DefaultSpanishResourceDataBroker extends DefaultResourceDataBroker implements SpanishResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/spanish.dict";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/spanish_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/spanish_tags.txt";

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String WIKIPEDIA_WORDS_FILE_NAME = "%1$s/wikipedia.txt";

    private Tagger tagger;
    private Tokenizer wordTokenizer;
    private Disambiguator disambiguator;
    private Synthesizer synthesizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private Set<Dictionary> dictionaries;
    private LanguageModel languageModel;
    private List<Map<String, String>> wikipediaWords;

    public DefaultSpanishResourceDataBroker(Spanish lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
     * Get the wikipedia words for Spanish.
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

    @Override
    public LanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
            languageModel = createLanguageModelFromResourcePath();
        }
        return languageModel;
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
    public List<String> getSpellingIgnoreWords() throws Exception {
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
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

    public Set<String> getSynthesizerWordTags() throws Exception {
        String file = String.format(SYNTHESIZER_WORD_TAGS_FILE_NAME, language.getLocale().getLanguage());
        return createSynthesizerWordTagsFromResourcePath(file);
    }

    @Override
    public Synthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new BaseSynthesizer(getIStemmer(), getSynthesizerWordTags());
        }
        return synthesizer;
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
      if (tagger == null) {
        tagger = new SpanishTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
    }

    /**
     * Get the disambiguator.  We override to return a specialization, however we still use {@link super.getDisambiguator()} to get the
     * xml rule based disambiguator.
     *
     * @return The disambiguator.
     */
    @Override
    public Disambiguator getDisambiguator() throws Exception {
        if (disambiguator == null) {
          disambiguator = new SpanishHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
        }
        return disambiguator;
    }

    @Override
    public Tokenizer getWordTokenizer() throws Exception {
      if (wordTokenizer == null) {
        wordTokenizer = new SpanishWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

}
