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
import org.apache.commons.lang3.tuple.*;
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

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import org.languagetool.language.Russian;
import org.languagetool.UserConfig;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ru.RussianHybridDisambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tagging.ru.RussianTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.synthesis.ru.RussianSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.patterns.RuleFilterCreator;
import org.languagetool.rules.ru.RussianPartialPosTagFilter;
import org.languagetool.rules.ConfusionSet;

public class DefaultRussianResourceDataBroker extends DefaultResourceDataBroker implements RussianResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For ru_RU this would become: ru/hunspell/ru_RU.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/russian.dict";

    public static String STEMMER_DICT_FILE_NAME = "%1$s/russian_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/tags_russian.txt";

    public static String REPLACE_FILE_NAME = "%1$s/replace.txt";

    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";

    public static String COHERENCY_WORD_LIST_FILE_NAME = "%1$s/coherency.txt";

    public static String PROHIBITED_WORDS_FILE_NAME = "%1$s/hunspell/prohibit.txt";
    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    private RussianTagger tagger;
    private RussianSynthesizer synthesizer;
    private SentenceTokenizer sentenceTokenizer;
    private Disambiguator disambiguator;
    private LuceneLanguageModel languageModel;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private RuleFilterCreator ruleFilterCreator;
    private Map<String, List<String>> wrongWords;
    private CompoundRuleData compounds;
    private Map<String, String> coherencyMappings;
    private Set<Dictionary> dictionaries;
    private List<String> spellingIgnoreWords;
    private List<String> spellingProhibitWords;
    private List<PatternRule> patternRules;

    public DefaultRussianResourceDataBroker(Russian lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public List<PatternRule> getCompoundPatternRules(String message) throws Exception {
        if (patternRules == null) {
            patternRules = loadCompoundPatternRulesFromResourcePath(String.format(COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, message);
        }
        return patternRules;
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
    public RuleFilterCreator getRuleFilterCreator() throws Exception {
        if (ruleFilterCreator == null) {
            ruleFilterCreator = new RuleFilterCreator(classLoader) {
                @Override
                public RuleFilter getFilter(String className) throws Exception {
                    // This could be made more sophisticated by examining the args for the constructors and
                    // selecting one that we could build.
                    Class<?> aClass;
                    try {
                        aClass = Class.forName(className, true, classLoader);
                    } catch(Exception e) {
                        throw new RuntimeException(String.format("Unable to load class: %1$s", className), e);
                    }
                    if (RussianPartialPosTagFilter.class.isAssignableFrom(aClass)) {
                        return new RussianPartialPosTagFilter(getTagger(), getDisambiguator());
                    }
                    return super.getFilter(className);
                }
            };
        }
        return ruleFilterCreator;
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
        if (spellingProhibitWords == null) {
            spellingProhibitWords = loadSpellingProhibitedWordsFromResourcePath(String.format(PROHIBITED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return spellingProhibitWords;
    }

    @Override
    public Map<String, String> getCoherencyMappings() throws Exception {
        if (coherencyMappings == null) {
            String fileName = String.format(COHERENCY_WORD_LIST_FILE_NAME, language.getLocale().getLanguage());
            coherencyMappings = createCoherencyMappingsFromRulesPath(fileName);
        }
        return coherencyMappings;
    }
/*
    @Override
    public List<ContextWords> getWrongWordsInContext() throws Exception {
        if (wrongWordsInContext == null) {
            wrongWordsInContext = loadContextWords(getRulesDirPath(String.format(WRONG_WORDS_IN_CONTEXT_FILE_NAME, language.getLocale().getLanguage())));
        }
        return wrongWordsInContext;
    }
*/
    /**
     * Get the wrong words for Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getWrongWords() throws Exception {
        if (wrongWords == null) {
            String file = String.format(REPLACE_FILE_NAME, language.getLocale().getLanguage());
            wrongWords = createWrongWordsFromRulesPath(file);
        }
        return wrongWords;
    }

    @Override
    public Disambiguator getDisambiguator() throws Exception {
      if (disambiguator == null) {
          disambiguator = new RussianHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
      }
      return disambiguator;
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
            synthesizer = new RussianSynthesizer(getIStemmer(), getSynthesizerWordTags());
        }
        return synthesizer;
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
    public RussianTagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new RussianTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
    }

    @Override
    public LuceneLanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
            languageModel = createLanguageModelFromResourcePath();
        }
        return languageModel;
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
