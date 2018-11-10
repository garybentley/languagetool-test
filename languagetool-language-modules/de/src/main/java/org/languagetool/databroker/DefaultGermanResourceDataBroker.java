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
import java.io.IOException;
import java.io.InputStream;
import java.util.ResourceBundle;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

/*
GTODO Clean up

import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
*/
import org.languagetool.databroker.StringProcessor;
import org.languagetool.UserConfig;
import org.languagetool.language.German;
import org.languagetool.*;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.GermanChunker;
import org.languagetool.synthesis.GermanSynthesizer;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.rules.de.*;
import org.languagetool.tokenizers.de.*;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.patterns.*;

/*
GTODO Clean up
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.CombiningTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.rules.neuralnetwork.Embedding;
import org.languagetool.rules.neuralnetwork.Matrix;
import org.languagetool.rules.neuralnetwork.Classifier;
import org.languagetool.rules.en.EnglishPartialPosTagFilter;
import org.languagetool.rules.en.NoDisambiguationEnglishPartialPosTagFilter;
*/
public class DefaultGermanResourceDataBroker extends DefaultResourceDataBroker implements GermanResourceDataBroker {

     public static String STEMMER_DICT_FILE_NAME = "/%1$s/german_synth.dict";
     public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "/%1$s/german_tags.txt";
     public static String CASE_RULE_EXCEPTIONS_FILE_NAME = "/%1$s/case_rule_exceptions.txt";
     public static String OLD_SPELLING_RULES_FILE_NAME = "%1$s/alt_neu.csv";

     /**
      * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
      * For fr_FR this would become: fr/hunspell/fr_FR.dict
      */
     public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

     /**
      * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
      * For de_CH this would become: de/hunspell/de_CH.info
      */
     public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

     public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

     /**
      * The locale variant plain text spelling file.  The locale language and country values are replaced in the filename.
      * For de_AT this would become: de/hunspell/spelling-de-AT.txt
      */
     public static String LOCALE_VARIANT_PLAIN_TEXT_SPELLING_FILE_NAME = "%1$s/hunspell/spelling-%1$s-%2$s.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/german.dict";

    public static String COHERENCY_WORD_LIST_FILE_NAME = "%1$s/coherency.txt";

    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";

    public static String COMPOUND_CITIES_FILE_NAME = "%1$s/compound-cities.txt";

    public static String WRONG_WORDS_IN_CONTEXT_FILE_NAME = "%1$s/wrongWordInContext.txt";

    public static String PROHIBITED_WORDS_FILE_NAME = "%1$s/hunspell/prohibit.txt";
    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    private static RuleFilterCreator ruleFilterCreator;

    /**
     * Converts a line in the form: word[|word|...]=replacement[|replacement|...] to a map of the wrong word to the list of replacements.
     * i.e. the line:
     *    bigus|bogas=bogus|bogusy
     * would return a map popuplated with:
     *    String(bigas) -> Set(bogus, bogusy)
     *    String(bogas) -> Set(bogus, bogusy)
     */
    private static final StringProcessor<Map<String, List<String>>> contractionWrongWordsStringProcessor = new StringProcessor<Map<String, List<String>>>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.startsWith("#");
        }

        @Override
        public Set<String> getErrors(String line) {
            Set<String> errors = null;
            String[] parts = line.split("=");
            if (parts.length != 2) {
                errors = new LinkedHashSet<>();
                errors.add ("Expected format 'word[|word|...]=replacement[|replacement|...]' (the items in [] are optional and indicate how to add multiple wrong words and their potential replacements)");
            }
            return errors;
        }

        @Override
        public Map<String, List<String>> getProcessed(String line) {
            Map<String, List<String>> data = new HashMap<>();
            String[] parts = line.split("=");
            // Part 0 is the word[|word|...]
            String[] wrongForms = parts[0].split("\\|");

            // Part 1 is the replacement[|replacement|...]
            List<String> replacements = Arrays.asList(parts[1].split("\\|"));
            for (String wrongForm : wrongForms) {
              data.put(wrongForm, replacements);
            }
            return data;
        }
    };

    private static final StringProcessor<Pattern[]> caseRuleExceptionsStringProcessor = new StringProcessor<Pattern[]>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.startsWith("#");
        }

        @Override
        public Pattern[] getProcessed(String line) {
            String[] parts = line.trim().split(" ");
            Pattern[] patterns = new Pattern[parts.length];
            for (int j = 0; j < parts.length; j++) {
              patterns[j] = Pattern.compile(parts[j]);
            }
            return patterns;
        }
    };

/*

    private Map<String, List<String>> contractionWrongWords;
    private Map<String, List<String>> wrongWords;
    private List<ContextWords> wrongWordsInContext;
    private CompoundRuleData compounds;
    private List<AbstractPatternRule> patternRules;
    private EnglishChunker chunker;
    private WordTagger wordTagger;
    private Dictionary wordTaggerDictionary;
    private EnglishTagger tagger;
    private EnglishSynthesizer synthesizer;
    private EnglishWordTokenizer wordTokenizer;
    private LuceneLanguageModel languageModel;
    */
    private List<ContextWords> wrongWordsInContext;
    private IStemmer istemmer;
    private GermanChunker chunker;
    private GermanSynthesizer synthesizer;
    private GermanTagger tagger;
    private GermanCompoundTokenizer strictCompoundTokenizer;
    private CompoundWordTokenizer compoundTokenizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private LuceneLanguageModel languageModel;
    private Set<Pattern[]> caseRuleExceptions;
    private Set<Dictionary> dictionaries;
    private Hunspell.Dictionary hunspellDict;
    private Map<String, String> coherencyMappings;
    private List<SpellingRuleWithSuggestion> oldSpellingRules;
    private CompoundRuleData compounds;

    public DefaultGermanResourceDataBroker(German lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public CompoundRuleData getCompounds() throws Exception {
        if (compounds == null) {
            compounds = createCompoundRuleDataFromResourcePaths(String.format(COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()), String.format(COMPOUND_CITIES_FILE_NAME, language.getLocale().getLanguage()));
        }
        return compounds;
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
    public Set<Pattern[]> getCaseRuleExceptionPatterns() throws Exception {
        if (caseRuleExceptions == null) {
            caseRuleExceptions = new LinkedHashSet<>((List<Pattern[]>) getWordListFromResourcePath(String.format(CASE_RULE_EXCEPTIONS_FILE_NAME, language.getLocale().getLanguage()), caseRuleExceptionsStringProcessor));
        }
        return caseRuleExceptions;
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
                dicts.add(DefaultMorfologikDictionaryLoader.loadFromLines(lines, getResourceDirPathStream(plainTextInfoFile)));
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

            LineExpander lineExpander = new GermanLineExpander();

            List<byte[]> lines = new ArrayList<>();
            String spellingFile = String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, lang);
            if (resourceDirPathExists(spellingFile)) {
                List<String> llines = getWordListFromResourcePath(spellingFile, DEFAULT_CHARSET, spellingWordProcessor);
                lines.addAll(llines.stream()
                            .map(w -> lineExpander.expandLine(w))
                            .flatMap(c -> c.stream().map(l -> l.getBytes(DEFAULT_CHARSET)))
                            .collect(Collectors.toList()));
            }
            String localeVariantFile = String.format(LOCALE_VARIANT_PLAIN_TEXT_SPELLING_FILE_NAME, lang, country);
            if (resourceDirPathExists(localeVariantFile)) {
                List<String> llines = getWordListFromResourcePath(localeVariantFile, DEFAULT_CHARSET, spellingWordProcessor);
                lines.addAll(llines.stream()
                        .map(w -> lineExpander.expandLine(w))
                        .flatMap(c -> c.stream().map(l -> l.getBytes(DEFAULT_CHARSET)))
                        .collect(Collectors.toList()));
            }
            if (lines.size() > 0) {
                try (InputStream infoStream = Files.newInputStream(getResourceDirPath(plainTextInfoFile))) {
                    _dicts.add(DefaultMorfologikDictionaryLoader.loadFromLines(lines, infoStream));
                } catch (Exception e) {
                    Set<Path> wordsPaths = new LinkedHashSet<>();
                    if (resourceDirPathExists(spellingFile)) {
                        wordsPaths.add(getResourceDirPath(spellingFile));
                    }
                    if (resourceDirPathExists(localeVariantFile)) {
                        wordsPaths.add(getResourceDirPath(localeVariantFile));
                    }
                    throw new IOException(String.format("Unable to load morfologik dictionary for text resource paths: %1$s and info resource path: %2$s", wordsPaths, getResourceDirPath(plainTextInfoFile)), e);
                }
            }
            dictionaries = _dicts;
            dicts.addAll(_dicts);
        }
        dicts.addAll(dictionaries);
        return dicts;
    }

    @Override
    public Hunspell.Dictionary getHunspellDictionary() throws Exception {
        if (hunspellDict == null) {
            hunspellDict = createHunspellDictionaryFromResourcePath();
        }
        return hunspellDict;
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
                    if (UppercaseNounReadingFilter.class.isAssignableFrom(aClass)) {
                        return new UppercaseNounReadingFilter(getTagger());
                    }
                    return super.getFilter(className);
                }
            };
        }
        return ruleFilterCreator;
    }

/*
GTODO
    @Override
    public Map<String, List<String>> getContractionWrongWords() throws Exception {
        if (contractionWrongWords == null) {
            if (rulesDirPathExists(CONTRACTIONS_FILE_NAME)) {
                // GTODO Sort out this cast
                // GTODO Make an accumulator?  Stream?
                List<Map<String, List<String>>> words = (List<Map<String, List<String>>>) getWordListFromRulesPath(CONTRACTIONS_FILE_NAME, contractionWrongWordsStringProcessor);

                Map<String, List<String>> data = new HashMap<>();

                // Should we merge words?  i.e. if multiple lines have the same wrong word?
                for (Map<String, List<String>> line : words) {

                    data.putAll(line);
                }
                contractionWrongWords = data;
            }
        }
        return contractionWrongWords;
    }
*/
    /**
     * Get the chunker to use.  For now we just look up the files on the classpath, no rules or resources prefix.
     *
     * @return the chunker.
     */
    @Override
    public GermanChunker getPostDisambiguationChunker() throws Exception {
      if (chunker == null) {
          chunker = new GermanChunker();
      }
      return chunker;
    }

    public Set<String> getSynthesizerWordTags() throws Exception {
        return createSynthesizerWordTagsFromResourcePath(String.format(SYNTHESIZER_WORD_TAGS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public GermanSynthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new GermanSynthesizer(getIStemmer(), getSynthesizerWordTags(), getStrictCompoundTokenizer(), getCaseConverter());
        }
        return synthesizer;
    }

    @Override
    public List<ContextWords> getWrongWordsInContext() throws Exception {
        if (wrongWordsInContext == null) {
            wrongWordsInContext = loadContextWords(getRulesDirPath(String.format(WRONG_WORDS_IN_CONTEXT_FILE_NAME, language.getLocale().getLanguage())));
        }
        return wrongWordsInContext;
    }
/*
    @Override
    public List<PatternRule> getCompoundPatternRules(String message) throws Exception {
        return loadCompoundPatternRulesFromResourcePath(COMPOUNDS_FILE_NAME, DEFAULT_CHARSET, message);
    }
*/
/*
    @Override
    public CompoundRuleData getCompounds() throws Exception {
        if (compounds == null) {
            if (resourceDirPathExists(COMPOUNDS_FILE_NAME)) {
                compounds = createCompoundRuleDataFromResourcePath(COMPOUNDS_FILE_NAME);
            }
        }
        return compounds;
    }

    public boolean allowCaching() {
        return false;
    }
*/
/*
GTODO: Cleanup
MorfologikDictionaryProvider
   Set<Dictionary> getDictionaries()
   -- Most have a hunspell, <locale>.info binary
   -- Some havea  plain text, spelling_<locale>.txt

   English GB
     -- en_GB.dict
     -- spelling.txt
     -- spelling_en_GB.txt

   Add user dictionary.
   Add binary dictionary (<locale>.info)
   Add extra dictionary (plain text + language variant)
*/

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

    public ManualTagger createRemovalTaggerFromResourcePath() throws Exception {
        String manualRemovalFileName = String.format(WORD_TAGGER_REMOVED_WORDS_FILE_NAME, language.getLocale().getLanguage());
        ManualTagger removalTagger = null;
        if (resourceDirPathExists(manualRemovalFileName)) {
            Path path = getResourceDirPath(manualRemovalFileName);
            try (InputStream stream = Files.newInputStream(path)) {
              return new ManualTagger(stream);
          } catch(Exception e) {
              throw new IOException(String.format("Unable to load removed words file: %1$s", path), e);
          }
        }
        return null;
    }

    public CompoundWordTokenizer getNonStrictCompoundSplitter() throws Exception {
      if (compoundTokenizer == null) {
          // GTODO Maybe pass a Word splitter?
          GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer(false);  // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
          compoundTokenizer = word -> new ArrayList<>(tokenizer.tokenize(word));
      }
      return compoundTokenizer;
    }

    public GermanCompoundTokenizer getStrictCompoundTokenizer() throws Exception {
      if (strictCompoundTokenizer == null) {
          strictCompoundTokenizer = new GermanCompoundTokenizer(true);
      }
      return strictCompoundTokenizer;
    }

    /**
     * Get the sentence tokenizer.
     *
     * @return The sentence tokenizer.
     */
    @Override
    public SRXSentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

    /**
     * Get the tagger.
     *
     * @return The tagger.
     */
    @Override
    public GermanTagger getTagger() throws Exception {
        if (tagger == null) {
            // When tagging we use a strict tokenizer.
            tagger = new GermanTagger(getWordTaggerDictionary(), getWordTagger(), createRemovalTaggerFromResourcePath(), getStrictCompoundTokenizer(), getCaseConverter());
        }
        return tagger;
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
        List<String> words = loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));

        String localeVariantFile = String.format(LOCALE_VARIANT_PLAIN_TEXT_SPELLING_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
        if (resourceDirPathExists(localeVariantFile)) {
            words.addAll(getWordListFromResourcePath(localeVariantFile, DEFAULT_CHARSET, spellingWordProcessor));
        }
        return words;
    }

    @Override
    public List<String> getSpellingProhibitedWords() throws Exception {
        return loadSpellingProhibitedWordsFromResourcePath(String.format(PROHIBITED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
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
     * Creates a neural network rule Classifier.  It creates a SingleLayerClassifier is only the following files are available from the resource path:
     *    {@link NEURAL_NETWORK_CLASSIFIER_W_FC1_FILE_NAME}
     *    {@link NEURAL_NETWORK_CLASSIFIER_B_FC1_FILE_NAME}
     *
     * It creates a TwoLayerClassifier if the above files AND the files below are available from the resource path:
     *    {@link NEURAL_NETWORK_CLASSIFIER_W_FC2_FILE_NAME}
     *    {@link NEURAL_NETWORK_CLASSIFIER_B_FC2_FILE_NAME}
     *
     * If any of the files are missing then it returns null.
     *
     * @param model The model to use for creating the classifier.
     * @return A single or two layer classifier depending upon what resource files are available.
     */
     /*
     GTODO
    public Classifier createNeuralNetworkRuleClassifier(Word2VecModel model) throws Exception {
        if (resourceDirPathExists(NEURAL_NETWORK_CLASSIFIER_W_FC1_FILE_NAME) && resourceDirPathExists(NEURAL_NETWORK_CLASSIFIER_B_FC1_FILE_NAME)) {
            Path w1p = getResourceDirPath(NEURAL_NETWORK_CLASSIFIER_W_FC1_FILE_NAME);
            Path b1p = getResourceDirPath(NEURAL_NETWORK_CLASSIFIER_B_FC1_FILE_NAME);
            if (resourceDirPathExists(NEURAL_NETWORK_CLASSIFIER_W_FC2_FILE_NAME) && resourceDirPathExists(NEURAL_NETWORK_CLASSIFIER_B_FC2_FILE_NAME)) {
                Path w2p = getResourceDirPath(NEURAL_NETWORK_CLASSIFIER_W_FC2_FILE_NAME);
                Path b2p = getResourceDirPath(NEURAL_NETWORK_CLASSIFIER_B_FC2_FILE_NAME);
                try (InputStream w1 = Files.newInputStream(w1p);
                     InputStream b1 = Files.newInputStream(b1p);
                     InputStream w2 = Files.newInputStream(w2p);
                     InputStream b2 = Files.newInputStream(b2p);
                ) {
                    return new TwoLayerClassifier(model.getEmbedding(), w1, b1, w2, b2);
                } catch(Exception e) {
                    throw new IOException(String.format("Unable to create new two layer classifier from: %1$s, %2$s, %3$s, %4$s", w1p, b1p, w2p, b2p), e);
                }
            }

            try (InputStream w1 = Files.newInputStream(w1p);
                 InputStream b1 = Files.newInputStream(b1p);
            ) {
                return new SingleLayerClassifier(model.getEmbedding(), w1, b1);
            } catch(Exception e) {
                throw new IOException(String.format("Unable to create single two layer classifier from: %1$s, %2$s", w1p, b1p), e);
            }
        }
        return null;
    }
*/

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

    @Override
    public List<SpellingRuleWithSuggestion> getOldSpellingRuleSuggestions() throws Exception {
        if (oldSpellingRules == null) {
            StringProcessor<SpellingRuleWithSuggestion> processor = new StringProcessor<SpellingRuleWithSuggestion>() {
                  @Override
                  public boolean shouldSkip(String line) {
                      return line.startsWith("#");
                  }
                  @Override
                  public Set<String> getErrors(String line) {
                      Set<String> errors = null;
                      String[] parts = line.split(";");
                      if (parts.length != 2) {
                          errors = new LinkedHashSet<>();
                          errors.add ("Expected line to have format: <alternative>;<suggestion>");
                      }
                      return errors;
                  }
                  @Override
                  public SpellingRuleWithSuggestion getProcessed(String line) throws Exception {
                      String[] parts = line.split(";");
                      String alternative = parts[0];
                      String suggestion = parts[1];
                      List<PatternToken> patternTokens = getTokens(alternative);
                      PatternRule rule = new PatternRule(OldSpellingRule.RULE_INTERNAL, language, patternTokens, OldSpellingRule.DESC, OldSpellingRule.MESSAGE, OldSpellingRule.SHORT_MESSAGE);
                      rule.setLocQualityIssueType(OldSpellingRule.ISSUE_TYPE);
                      return new SpellingRuleWithSuggestion(rule, alternative, suggestion);
                  }
            };
            oldSpellingRules = loadWordsFromResourcePath(String.format(OLD_SPELLING_RULES_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, processor);
        }
        return oldSpellingRules;
    }

    private List<PatternToken> getTokens(String alternative) throws Exception {
      PatternTokenBuilder builder = new PatternTokenBuilder();
      String[] suggestionTokens = alternative.split(" ");
      List<PatternToken> patternTokens = new ArrayList<>();
      for (String part : suggestionTokens) {
        PatternToken token;
        if (isBaseform(alternative)) {
          token = builder.csToken(part).matchInflectedForms().build();
        } else {
          token = builder.csToken(part).build();
        }
        patternTokens.add(token);
      }
      return patternTokens;
    }

    private boolean isBaseform(String term) throws Exception {
      try {
        AnalyzedTokenReadings lookup = getTagger().lookup(term);
        if (lookup != null) {
          return lookup.hasLemma(term);
        }
        return false;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

}
