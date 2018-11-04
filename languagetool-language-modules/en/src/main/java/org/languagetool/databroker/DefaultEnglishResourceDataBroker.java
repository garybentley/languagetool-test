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

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.tokenize.TokenizerModel;

import org.languagetool.databroker.StringProcessor;
import org.languagetool.UserConfig;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.SimpleReplaceDataLoader;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilterCreator;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.language.English;
import org.languagetool.Language;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.EnglishChunker;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.CombiningTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.neuralnetwork.Classifier;
import org.languagetool.rules.en.EnglishPartialPosTagFilter;
import org.languagetool.rules.en.NoDisambiguationEnglishPartialPosTagFilter;
import org.languagetool.rules.neuralnetwork.Word2VecModel;

public class DefaultEnglishResourceDataBroker extends DefaultResourceDataBroker implements EnglishResourceDataBroker {

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

    /**
     * The locale variant plain text spelling file.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/spelling_en-GB.txt
     */
    public static String LOCALE_VARIANT_PLAIN_TEXT_SPELLING_FILE_NAME = "%1$s/hunspell/spelling_%1$s-%2$s.txt";

    /**
     * The location of wrong words (words to be replaced) in the rules dir.  %1$s is replaced with the language code from the locale,
     * %2$s is the country.  This file is used in {@link getWrongWords()}.
     */
     // GTODO Change to replace language for /en/
    public static String REPLACE_FILE_NAME = "/%1$s/%1$s-%2$s/replace.txt";
    public static String CONTRACTIONS_FILE_NAME = "/en/contractions.txt";
    public static String COMPOUNDS_FILE_NAME = "/en/compounds.txt";
    public static String WRONG_WORDS_IN_CONTEXT_FILE_NAME = "/en/wrongWordInContext.txt";
    public static String GENERAL_PATTERN_RULES_FILE_NAME = "/en/grammar.xml";
    public static String LOCALE_VARIANT_PATTERN_RULES_FILE_NAME = "/en/en_%1$s/grammar.xml";
    public static String WORD_TAGGER_DICT_FILE_NAME = "/%1$s/english.dict";
    public static String STEMMER_DICT_FILE_NAME = "/%1$s/english_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "/en/english_tags.txt";
    public static String REQUIRES_A_WORD_LIST_FILE_NAME = "/en/det_a.txt";
    public static String REQUIRES_AN_WORD_LIST_FILE_NAME = "/en/det_an.txt";
    public static String COHERENCY_WORD_LIST_FILE_NAME = "/%1$s/coherency.txt";

    public static String CHUNKER_TOKENIZER_MODEL_FILE_NAME = "/en-token.bin";
    public static String CHUNKER_POS_TAGGER_MODEL_FILE_NAME = "/en-pos-maxent.bin";
    public static String CHUNKER_MODEL_FILE_NAME = "/en-chunker.bin";

    private static TokenizerModel tokenModel;
    private static POSModel posModel;
    private static ChunkerModel chunkerModel;
    private static RuleFilterCreator ruleFilterCreator;

    private static final StringProcessor<String> requireAOrANWordsStringProcessor = new StringProcessor<String>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.startsWith("#");
        }
        /**
         * If the line starts with * then return without the *, otherwise return a lowercase version of the line.
         */
        @Override
        public String getProcessed(String line) {
            line = line.trim();
            return line.startsWith("*") ? line.substring(1) : line.toLowerCase();
        }
    };

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

    private Map<String, List<String>> wrongWords;
    private Map<String, List<String>> contractionWrongWords;
    private Map<String, String> coherencyMappings;
    private List<ContextWords> wrongWordsInContext;
    private CompoundRuleData compounds;
    private List<AbstractPatternRule> patternRules;
    private EnglishChunker chunker;
    private WordTagger wordTagger;
    private Dictionary wordTaggerDictionary;
    private EnglishTagger tagger;
    private EnglishSynthesizer synthesizer;
    private IStemmer istemmer;
    private EnglishWordTokenizer wordTokenizer;
    private LuceneLanguageModel languageModel;
    private Set<Dictionary> dictionaries;

    public DefaultEnglishResourceDataBroker(English lang, ClassLoader classLoader) throws Exception {
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
            String localeVariantFile = String.format(LOCALE_VARIANT_PLAIN_TEXT_SPELLING_FILE_NAME, lang, country);
            if (resourceDirPathExists(localeVariantFile)) {
                availableFiles.add(localeVariantFile);
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
                    if (NoDisambiguationEnglishPartialPosTagFilter.class.isAssignableFrom(aClass)) {
                        return new NoDisambiguationEnglishPartialPosTagFilter(getTagger());
                    }
                    if (EnglishPartialPosTagFilter.class.isAssignableFrom(aClass)) {
                        return new EnglishPartialPosTagFilter(getTagger(), getDisambiguator());
                    }
                    return super.getFilter(className);
                }
            };
        }
        return ruleFilterCreator;
    }

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

    /**
     * Get the list of words that require a 'A' rather than 'AN'.
     *
     * @return The list of words.
     */
    @Override
    public Set<String> getRequiresAWords() throws Exception {
        // GTODO Improve
        return new LinkedHashSet<>(loadWordsFromRulesPath(REQUIRES_A_WORD_LIST_FILE_NAME, DEFAULT_CHARSET, requireAOrANWordsStringProcessor));
    }

    /**
     * Get the list of words that require a 'AN' rather than 'A'.
     *
     * @return The list of words.
     */
    @Override
    public Set<String> getRequiresANWords() throws Exception {
        // GTODO Improve.  Use getWordList?
        return new LinkedHashSet<>(loadWordsFromRulesPath(REQUIRES_AN_WORD_LIST_FILE_NAME, DEFAULT_CHARSET, requireAOrANWordsStringProcessor));
    }

    /**
     * Get the chunker to use.  For now we just look up the files on the classpath, no rules or resources prefix.
     *
     * @return the chunker.
     */
    @Override
    public EnglishChunker getChunker() throws Exception {
      if (chunker == null) {
          if (tokenModel == null) {
              Path path = pathProvider.getPath(Arrays.asList(CHUNKER_TOKENIZER_MODEL_FILE_NAME));
              try (InputStream is = Files.newInputStream(path)) {
                  tokenModel = new TokenizerModel(is);
              } catch(Exception e) {
                  throw new IOException(String.format("Unable to load tokenizer model from path: %1$s", path), e);
              }
          }
          if (posModel == null) {
              Path path = pathProvider.getPath(Arrays.asList(CHUNKER_POS_TAGGER_MODEL_FILE_NAME));
              try (InputStream is = Files.newInputStream(path)) {
                  posModel = new POSModel(is);
              } catch(Exception e) {
                  throw new IOException(String.format("Unable to load pos tagger model from path: %1$s", path), e);
              }
          }
          if (chunkerModel == null) {
              Path path = pathProvider.getPath(Arrays.asList(CHUNKER_MODEL_FILE_NAME));
              try (InputStream is = Files.newInputStream(path)) {
                  chunkerModel = new ChunkerModel(is);
              } catch(Exception e) {
                  throw new IOException(String.format("Unable to load chunker model from path: %1$s", path), e);
              }
          }
          chunker = new EnglishChunker(tokenModel, posModel, chunkerModel);
      }
      return chunker;
    }

    public Set<String> getSynthesizerWordTags() throws Exception {
        return createSynthesizerWordTagsFromResourcePath(SYNTHESIZER_WORD_TAGS_FILE_NAME);
    }

    @Override
    public EnglishSynthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new EnglishSynthesizer(getMessageBundle(), getIStemmer(), getSynthesizerWordTags(), getRequiresAWords(), getRequiresANWords());
        }
        return synthesizer;
    }

    @Override
    public List<ContextWords> getWrongWordsInContext() throws Exception {
        if (wrongWordsInContext == null) {
            wrongWordsInContext = loadContextWords(getRulesDirPath(WRONG_WORDS_IN_CONTEXT_FILE_NAME));
        }
        return wrongWordsInContext;
    }

    @Override
    public List<PatternRule> getCompoundPatternRules(String message) throws Exception {
        return loadCompoundPatternRulesFromResourcePath(COMPOUNDS_FILE_NAME, DEFAULT_CHARSET, message);
    }

    @Override
    public CompoundRuleData getCompounds() throws Exception {
        if (compounds == null) {
            if (resourceDirPathExists(COMPOUNDS_FILE_NAME)) {
                compounds = createCompoundRuleDataFromResourcePaths(COMPOUNDS_FILE_NAME);
            }
        }
        return compounds;
    }

    public boolean allowCaching() {
        return false;
    }

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

    /**
     * Get the tagger.
     *
     * @return The tagger.
     */
    @Override
    public EnglishTagger getTagger() throws Exception {
        if (tagger == null) {
              tagger = new EnglishTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
        }
        return tagger;
    }

    /**
     * Get the word tokenizer, this just returns a standard WordTokenizer instance.
     *
     * @return The tokenizer.
     */
    @Override
    public EnglishWordTokenizer getWordTokenizer() {
        if (wordTokenizer == null) {
            wordTokenizer = new EnglishWordTokenizer();
        }
        return wordTokenizer;
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

    @Override
    public synchronized LuceneLanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
            languageModel = createLanguageModelFromResourcePath();
        }
        return languageModel;
    }

    /**
     * Get the wrong words for a locale, files in use are:
     *   - /rules/en/en-<locale.countrycode>/replace.txt
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getWrongWords() throws Exception {
        if (wrongWords == null) {
            String replaceFile = String.format(REPLACE_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
            wrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return wrongWords;
    }

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return getSpellingIgnoreWordsFromResourcePath();
    }

    @Override
    public List<String> getSpellingProhibitedWords() throws Exception {
        return getSpellingProhibitedWordsFromResourcePath();
    }

    @Override
    public WordTagger getWordTagger() throws Exception {
        if (wordTagger == null) {
            wordTagger = createWordTaggerFromResourcePath(new MorfologikTagger(getWordTaggerDictionary()), false);
        }
        return wordTagger;
    }

    @Override
    public Dictionary getWordTaggerDictionary() throws Exception {
        if (wordTaggerDictionary == null) {
            wordTaggerDictionary = getMorfologikBinaryDictionaryFromResourcePath(String.format(WORD_TAGGER_DICT_FILE_NAME, language.getLocale().getLanguage()));
        }
        return wordTaggerDictionary;
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
