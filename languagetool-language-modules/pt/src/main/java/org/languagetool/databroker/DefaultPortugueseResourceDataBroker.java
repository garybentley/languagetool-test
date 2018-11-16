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

import org.languagetool.language.Portuguese;
import org.languagetool.UserConfig;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.pt.PortugueseHybridDisambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tokenizers.pt.PortugueseWordTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.patterns.RuleFilterCreator;
import org.languagetool.rules.pt.NoDisambiguationPortuguesePartialPosTagFilter;

public class DefaultPortugueseResourceDataBroker extends DefaultResourceDataBroker implements PortugueseResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s_%2$s";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For it_IT this would become: it/hunspell/it_IT.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/portuguese.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String STEMMER_DICT_FILE_NAME = "%1$s/portuguese_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/portuguese_tags.txt";

    public static String REPLACE_FILE_NAME = "%1$s/replace.txt";
    public static String LOCALE_REPLACE_FILE_NAME = "%1$s/%1$s-%2$s/replace.txt";
    public static String AGREEMENT_REPLACE_FILE_NAME = "%1$s/AOreplace.txt";
    public static String WORDINESS_FILE_NAME = "%1$s/wordiness.txt";
    public static String WEASEL_WORDS_FILE_NAME = "%1$s/weaselwords.txt";
    public static String REDUNDANCY_WORDS_FILE_NAME = "%1$s/redundancies.txt";
    public static String CLICHE_WORDS_FILE_NAME = "%1$s/cliches.txt";
    public static String WIKIPEDIA_WORDS_FILE_NAME = "%1$s/wikipedia.txt";
    public static String BARBARISMS_WORDS_FILE_NAME = "%1$s/barbarisms.txt";

    public static String WRONG_WORDS_IN_CONTEXT_FILE_NAME = "%1$s/wrongWordInContext.txt";

    public static String COHERENCY_WORD_LIST_FILE_NAME = "%1$s/coherency.txt";

    public static String POST_REFORM_COMPOUNDS_FILE_NAME = "%1$s/post-reform-compounds.txt";

    public static String PRE_REFORM_COMPOUNDS_FILE_NAME = "%1$s/pre-reform-compounds.txt";

    public static String VERB_TO_NOUN_ACCENT_WORDS_FILE_NAME = "%1$s/verbos_sem_acento_nomes_com_acento.txt";

    public static String VERB_TO_ADJECTIVE_ACCENT_WORDS_FILE_NAME = "%1$s/verbos_sem_acento_adj_com_acento.txt";

    public static final StringProcessor<Pair<String, AnalyzedToken>> wordToAccentedWord = new StringProcessor<Pair<String, AnalyzedToken>>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.startsWith("#");
        }
        @Override
        public Set<String> getErrors(String line) {
            Set<String> errors = null;
            line = line.trim();
            final String[] parts = line.split(";");
            if (parts.length != 3) {
                errors = new HashSet<>();
                errors.add("Expected format: <word without accent>;<replacement with accent>;<pos tag>");
            }
            return errors;
        }
        @Override
        public Pair<String, AnalyzedToken> getProcessed(String line) {
            line = line.trim();
            final String[] parts = line.split(";");
            return new ImmutablePair<String, AnalyzedToken>(parts[0], new AnalyzedToken(parts[1], parts[2], null));
        }
    };

    private PortugueseTagger tagger;
    private Disambiguator disambiguator;
    private WordTokenizer wordTokenizer;
    private Synthesizer synthesizer;
    private SentenceTokenizer sentenceTokenizer;
    private LuceneLanguageModel languageModel;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private Map<String, List<String>> wrongWords;
    private Map<String, List<String>> portugalWrongWords;
    private Map<String, List<String>> brazilWrongWords;
    private Map<String, List<String>> agreementWrongWords;
    private List<Map<String, String>> wordinessWords;
    private List<Map<String, String>> weaselWords;
    private List<Map<String, String>> redundancyWords;
    private List<Map<String, String>> clicheWords;
    private List<Map<String, String>> wikipediaWords;
    private List<Map<String, String>> barbarismsWords;
    private List<ContextWords> wrongWordsInContext;
    private Map<String, String> coherencyMappings;
    private CompoundRuleData postReformCompoundData;
    private CompoundRuleData preReformCompoundData;
    private Map<String, AnalyzedToken> verbToNounAccentWords;
    private Map<String, AnalyzedToken> verbToAdjAccentWords;
    private List<String> spellingIgnoreWords;
    private Hunspell.Dictionary hunspellDict;
    private RuleFilterCreator ruleFilterCreator;

    public DefaultPortugueseResourceDataBroker(Portuguese lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
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
                    if (NoDisambiguationPortuguesePartialPosTagFilter.class.isAssignableFrom(aClass)) {
                        return new NoDisambiguationPortuguesePartialPosTagFilter(getTagger());
                    }
                    return super.getFilter(className);
                }
            };
        }
        return ruleFilterCreator;
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
    public List<String> getSpellingIgnoreWords() throws Exception {
        if (spellingIgnoreWords == null) {
            spellingIgnoreWords = loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return spellingIgnoreWords;
    }

    @Override
    public Map<String, AnalyzedToken> getVerbToNounAccentWords() throws Exception {
        if (verbToNounAccentWords == null) {
            List<Pair<String, AnalyzedToken>> data = loadWordsFromRulesPath(String.format(VERB_TO_NOUN_ACCENT_WORDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, wordToAccentedWord);
            verbToNounAccentWords = data.stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        }
        return verbToNounAccentWords;
    }

    @Override
    public Map<String, AnalyzedToken> getVerbToAdjectiveAccentWords() throws Exception {
        if (verbToAdjAccentWords == null) {
            List<Pair<String, AnalyzedToken>> data = loadWordsFromRulesPath(String.format(VERB_TO_ADJECTIVE_ACCENT_WORDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, wordToAccentedWord);
            verbToAdjAccentWords = data.stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        }
        return verbToAdjAccentWords;
    }

    @Override
    public List<PatternRule> getPostReformCompoundPatternRules(String message) throws Exception {
        return loadCompoundPatternRulesFromResourcePath(POST_REFORM_COMPOUNDS_FILE_NAME, DEFAULT_CHARSET, message);
    }

    @Override
    public List<PatternRule> getPreReformCompoundPatternRules(String message) throws Exception {
        return loadCompoundPatternRulesFromResourcePath(PRE_REFORM_COMPOUNDS_FILE_NAME, DEFAULT_CHARSET, message);
    }

    @Override
    public CompoundRuleData getPostReformCompoundRuleData() throws Exception {
        if (postReformCompoundData == null) {
            postReformCompoundData = createCompoundRuleDataFromResourcePaths(String.format(POST_REFORM_COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return postReformCompoundData;
    }

    @Override
    public CompoundRuleData getPreReformCompoundRuleData() throws Exception {
        if (preReformCompoundData == null) {
            preReformCompoundData = createCompoundRuleDataFromResourcePaths(String.format(PRE_REFORM_COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return preReformCompoundData;
    }

    @Override
    public Map<String, String> getCoherencyMappings() throws Exception {
        if (coherencyMappings == null) {
            String fileName = String.format(COHERENCY_WORD_LIST_FILE_NAME, language.getLocale().getLanguage());
            coherencyMappings = createCoherencyMappingsFromRulesPath(fileName);
        }
        return coherencyMappings;
    }

    @Override
    public List<ContextWords> getWrongWordsInContext() throws Exception {
        if (wrongWordsInContext == null) {
            wrongWordsInContext = loadContextWords(getRulesDirPath(String.format(WRONG_WORDS_IN_CONTEXT_FILE_NAME, language.getLocale().getLanguage())));
        }
        return wrongWordsInContext;
    }

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

    /**
     * Get the wrong words for Portugal Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getPortugalWrongWords() throws Exception {
        if (portugalWrongWords == null) {
            String file = String.format(LOCALE_REPLACE_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
            portugalWrongWords = createWrongWordsFromRulesPath(file);
        }
        return portugalWrongWords;
    }

    /**
     * Get the wrong words for Brazil Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getBrazilWrongWords() throws Exception {
        if (brazilWrongWords == null) {
            String file = String.format(LOCALE_REPLACE_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
            brazilWrongWords = createWrongWordsFromRulesPath(file);
        }
        return brazilWrongWords;
    }

    /**
     * Get the agreement wrong words for Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getAgreementWrongWords() throws Exception {
        if (agreementWrongWords == null) {
            String file = String.format(AGREEMENT_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            agreementWrongWords = createWrongWordsFromRulesPath(file);
        }
        return agreementWrongWords;
    }

    /**
     * Get the wikipedai words for Portuguese.
     *
     * @return A map of the words.
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
     * Get the wordiness words for Portuguese.
     *
     * @return A map of the words.
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
     * Get the redundancy words for Portuguese.
     *
     * @return A map of the words.
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
     * Get the barbarisms words for Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public List<Map<String, String>> getBarbarismsWords() throws Exception {
        if (barbarismsWords == null) {
            String file = String.format(BARBARISMS_WORDS_FILE_NAME, language.getLocale().getLanguage());
            barbarismsWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return barbarismsWords;
    }

    /**
     * Get the cliche words for Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public List<Map<String, String>> getClicheWords() throws Exception {
        if (clicheWords == null) {
            String file = String.format(CLICHE_WORDS_FILE_NAME, language.getLocale().getLanguage());
            clicheWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return clicheWords;
    }

    /**
     * Get the weasel words for Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public List<Map<String, String>> getWeaselWords() throws Exception {
        if (weaselWords == null) {
            String file = String.format(WEASEL_WORDS_FILE_NAME, language.getLocale().getLanguage());
            weaselWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return weaselWords;
    }

    @Override
    public Disambiguator getDisambiguator() throws Exception {
      if (disambiguator == null) {
          disambiguator = new PortugueseHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
      }
      return disambiguator;
    }

    @Override
    public WordTokenizer getWordTokenizer() {
      if (wordTokenizer == null) {
        wordTokenizer = new PortugueseWordTokenizer();
      }
      return wordTokenizer;
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
            synthesizer = new PortugueseSynthesizer(getIStemmer(), getSynthesizerWordTags());
        }
        return synthesizer;
    }

    @Override
    public PortugueseTagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new PortugueseTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
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

    @Override
    public List<NeuralNetworkRule> createNeuralNetworkRules(ResourceBundle messages, Word2VecModel model) throws Exception {
        return createNeuralNetworkRules(messages, language, createNeuralNetworkRuleClassifierFromResourceDir(model), getNeuralNetworkScoredConfusionSetsFromResourcePath(DEFAULT_CHARSET));
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
