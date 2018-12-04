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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.Collections;

import java.io.IOException;

import java.util.stream.Collectors;

import java.nio.charset.*;
import java.nio.file.*;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import morfologik.stemming.IStemmer;
import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;

import org.languagetool.UserConfig;
import org.languagetool.language.Catalan;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.disambiguation.ca.CatalanHybridDisambiguator;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.AnalyzedToken;

public class DefaultCatalanResourceDataBroker extends DefaultResourceDataBroker implements CatalanResourceDataBroker {

    public static String LOCALE_VARIANT_PATTERN_RULES_FILE_NAME = "%1$s/%1$s-%2$s-%3$s/grammar.xml";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/%1$s-%2$s-valencia_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/%1$s-%2$s-valencia_tags.txt";
    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/%1$s-%2$s-valencia.dict";
    public static String WORD_TAGGER_ADDED_WORDS_FILE_NAME = "%1$s/manual-tagger.txt";
    public static String WORD_TAGGER_REMOVED_WORDS_FILE_NAME = "%1$s/removed-tagger.txt";
    public static String OPERATION_NAME_REPLACE_FILE_NAME = "%1$s/replace_operationnames.txt";
    public static String REPLACE_FILE_NAME = "%1$s/replace.txt";
    public static String DIACRITICS_IEC_REPLACE_FILE_NAME = "%1$s/replace_diacritics_iec.txt";
    public static String VERBS_REPLACE_FILE_NAME = "%1$s/replace_verbs.txt";
    public static String DNV_REPLACE_FILE_NAME = "%1$s/replace_dnv.txt";
    public static String BALEARIC_REPLACE_FILE_NAME = "%1$s/replace_balearic.txt";
    public static String DIACRITICS_TRADITIONAL_REPLACE_FILE_NAME = "%1$s/replace_diacritics_traditional.txt";
    public static String ACCENTUATION_RELEVANT_WORDS_1_FILE_NAME = "%1$s/verb_senseaccent_nom_ambaccent.txt";
    public static String ACCENTUATION_RELEVANT_WORDS_2_FILE_NAME = "%1$s/verb_senseaccent_adj_ambaccent.txt";
    public static String WRONG_WORDS_IN_CONTEXT_FILE_NAME = "%1$s/wrongWordInContext.txt";
    public static String DIACRITIC_WRONG_WORDS_IN_CONTEXT_FILE_NAME = "%1$s/wrongWordInContext2.txt";
    public static String DNV_COLLOQUIAL_REPLACE_FILE_NAME = "%1$s/replace_dnv_colloquial.txt";
    public static String DNV_SECONDARY_REPLACE_FILE_NAME = "%1$s/replace_dnv_secondary.txt";

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/spelling.txt";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/%1$s_%2$s.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static final StringProcessor<Pair<String, AnalyzedTokenReadings>> accentuationProc = new StringProcessor<Pair<String, AnalyzedTokenReadings>>() {
        @Override
        public boolean shouldSkip(String line) {
            return line.isEmpty() || line.charAt(0) == '#';
        }
        @Override
        public Set<String> getErrors(String line) {
            Set<String> errors = null;
            final String[] parts = line.split(";");
            if (parts.length != 3) {
                errors = new HashSet<>();
                errors.add(String.format("Expected 3 semicolon-separated parts, got: %1$s", parts.length));
            }
            return errors;
        }
        @Override
        public Pair<String, AnalyzedTokenReadings> getProcessed(String line) {
            String[] parts = line.split(";");
            AnalyzedToken analyzedToken = new AnalyzedToken(parts[1], parts[2], null);
            return new ImmutablePair(parts[0], new AnalyzedTokenReadings(analyzedToken, 0));
        }
    };

/*
    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/polish.dict";


    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";

*/

   private CatalanHybridDisambiguator disambiguator;
   private CatalanTagger tagger;
   private CatalanWordTokenizer wordTokenizer;
   private CatalanSynthesizer synthesizer;
   private Dictionary wordTaggerDictionary;
   private WordTagger wordTagger;
   private IStemmer istemmer;
   private Map<String, List<String>> wrongWords;
   private Map<String, List<String>> diacriticIECWrongWords;
   private Map<String, List<String>> diacriticTradWrongWords;
   private Map<String, List<String>> opNameWrongWords;
   private Map<String, List<String>> verbsWrongWords;
   private Map<String, List<String>> dnvWrongWords;
   private Map<String, List<String>> dnvSecondaryWrongWords;
   private Map<String, List<String>> dnvColloquialWrongWords;
   private Map<String, List<String>> balearicWrongWords;
   private Map<String, AnalyzedTokenReadings> relevantWords1;
   private Map<String, AnalyzedTokenReadings> relevantWords2;
   private List<ContextWords> wrongWordsInContext;
   private List<ContextWords> diacriticWrongWordsInContext;
   private Set<Dictionary> dictionaries;
   private List<AbstractPatternRule> patternRules;
/*


    private CompoundRuleData compounds;

    private Set<Dictionary> dictionaries;
*/
    public DefaultCatalanResourceDataBroker(Catalan lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
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
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public List<ContextWords> getWrongWordsInContext() throws Exception {
        if (wrongWordsInContext == null) {
            wrongWordsInContext = loadContextWords(getRulesDirPath(String.format(WRONG_WORDS_IN_CONTEXT_FILE_NAME, language.getLocale().getLanguage())));
        }
        return wrongWordsInContext;
    }

    @Override
    public List<ContextWords> getDiacriticWrongWordsInContext() throws Exception {
        if (diacriticWrongWordsInContext == null) {
            diacriticWrongWordsInContext = loadContextWords(getRulesDirPath(String.format(DIACRITIC_WRONG_WORDS_IN_CONTEXT_FILE_NAME, language.getLocale().getLanguage())));
        }
        return diacriticWrongWordsInContext;
    }

    @Override
    public Map<String, AnalyzedTokenReadings> getAccentuationRelevantWords1() throws Exception {
        if (relevantWords1 == null) {
            String file = String.format(ACCENTUATION_RELEVANT_WORDS_1_FILE_NAME, language.getLocale().getLanguage());
            List<Pair<String, AnalyzedTokenReadings>> data = loadWordsFromRulesPath(file, DEFAULT_CHARSET, accentuationProc);
            relevantWords1 = data.stream()
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        }
        return relevantWords1;
    }

    @Override
    public Map<String, AnalyzedTokenReadings> getAccentuationRelevantWords2() throws Exception {
        if (relevantWords2 == null) {
            String file = String.format(ACCENTUATION_RELEVANT_WORDS_2_FILE_NAME, language.getLocale().getLanguage());
            List<Pair<String, AnalyzedTokenReadings>> data = loadWordsFromRulesPath(file, DEFAULT_CHARSET, accentuationProc);
            relevantWords2 = data.stream()
                    .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
        }
        return relevantWords2;
    }

    /**
     * Get the wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getWrongWords() throws Exception {
        if (wrongWords == null) {
            String replaceFile = String.format(REPLACE_FILE_NAME, language.getLocale().getLanguage());
            wrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return wrongWords;
    }

    /**
     * Get the balearic wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getBalearicWrongWords() throws Exception {
        if (balearicWrongWords == null) {
            String replaceFile = String.format(BALEARIC_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            balearicWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return balearicWrongWords;
    }

    /**
     * Get the DNV wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getDNVWrongWords() throws Exception {
        if (dnvWrongWords == null) {
            String replaceFile = String.format(DNV_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            dnvWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return dnvWrongWords;
    }

    /**
     * Get the DNV secondary wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getDNVSecondaryWrongWords() throws Exception {
        if (dnvSecondaryWrongWords == null) {
            String replaceFile = String.format(DNV_SECONDARY_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            dnvSecondaryWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return dnvSecondaryWrongWords;
    }

    /**
     * Get the DNV colloquial wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getDNVColloquialWrongWords() throws Exception {
        if (dnvColloquialWrongWords == null) {
            String replaceFile = String.format(DNV_COLLOQUIAL_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            dnvColloquialWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return dnvColloquialWrongWords;
    }

    /**
     * Get the verbs wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getVerbsWrongWords() throws Exception {
        if (verbsWrongWords == null) {
            String replaceFile = String.format(VERBS_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            verbsWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return verbsWrongWords;
    }

    /**
     * Get the diacritic IEC wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getDiactriticsIECWrongWords() throws Exception {
        if (diacriticIECWrongWords == null) {
            String replaceFile = String.format(DIACRITICS_IEC_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            diacriticIECWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return diacriticIECWrongWords;
    }

    /**
     * Get the diacritic traditional wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getDiactriticsTraditionalWrongWords() throws Exception {
        if (diacriticTradWrongWords == null) {
            String replaceFile = String.format(DIACRITICS_TRADITIONAL_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            diacriticTradWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return diacriticTradWrongWords;
    }

    /**
     * Get the operation name wrong words for Catalan.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getOperationNameWrongWords() throws Exception {
        if (opNameWrongWords == null) {
            String replaceFile = String.format(OPERATION_NAME_REPLACE_FILE_NAME, language.getLocale().getLanguage());
            opNameWrongWords = createWrongWordsFromRulesPath(replaceFile);
        }
        return opNameWrongWords;
    }
/*
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
*/
/*
    @Override
    public List<PatternRule> getCompoundPatternRules(String message) throws Exception {
        return loadCompoundPatternRulesFromResourcePath(String.format(COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, message);
    }
*/
    public IStemmer getIStemmer() throws Exception {
        if (istemmer == null) {
            String file = String.format(STEMMER_DICT_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
            try {
                istemmer = new DictionaryLookup(getMorfologikBinaryDictionaryFromResourcePath(file));
            } catch(Exception e) {
                throw new Exception(String.format("Unable to load stemmer dictionary from: %1$s", file), e);
            }
        }
        return istemmer;
    }

    public Set<String> getSynthesizerWordTags() throws Exception {
        String file = String.format(SYNTHESIZER_WORD_TAGS_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry());
        return createSynthesizerWordTagsFromResourcePath(file);
    }

    @Override
    public CatalanSynthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new CatalanSynthesizer(getIStemmer(), getSynthesizerWordTags());
        }
        return synthesizer;
    }

    @Override
    public WordTagger getWordTagger() throws Exception {
        if (wordTagger == null) {
            Path addedWords = getResourceDirPath(String.format(WORD_TAGGER_ADDED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
            Path removedWords = getResourceDirPath(String.format(WORD_TAGGER_REMOVED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
            wordTagger = createWordTagger(new MorfologikTagger(getWordTaggerDictionary()), addedWords, removedWords, false);
        }
        return wordTagger;
    }

    public Dictionary getWordTaggerDictionary() throws Exception {
        if (wordTaggerDictionary == null) {
            wordTaggerDictionary = getMorfologikBinaryDictionaryFromResourcePath(String.format(WORD_TAGGER_DICT_FILE_NAME, language.getLocale().getLanguage(), language.getLocale().getCountry()));
        }
        return wordTaggerDictionary;
    }

    @Override
    public CatalanTagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new CatalanTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter(), language.isVariant());
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
    public CatalanHybridDisambiguator getDisambiguator() throws Exception {
        if (disambiguator == null) {
          disambiguator = new CatalanHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
        }
        return disambiguator;
    }

    @Override
    public CatalanWordTokenizer getWordTokenizer() throws Exception {
      if (wordTokenizer == null) {
        wordTokenizer = new CatalanWordTokenizer(getWordTaggerDictionary());
      }
      return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

    @Override
    public List<AbstractPatternRule> getPatternRules() throws Exception {
        // GTODO This leads to a out of memory errors so we return an empty list for now.
        if (true) {
            return Collections.emptyList();
        }

        if (patternRules != null) {
            return patternRules;
        }
        Locale locale = language.getLocale();
        List<AbstractPatternRule> rules = new ArrayList<>();
        String genFile = String.format(GENERAL_PATTERN_RULES_FILE_NAME, locale.getLanguage());
        if (rulesDirPathExists(genFile)) {
            try {
                rules.addAll(getPatternRules(getRulesDirPath(genFile), getRuleFilterCreator()));
            } catch(Exception e) {
                throw new IOException(String.format("Unable to load pattern rules from: %1$s", genFile), e);
            }
        }

        if (locale != null) {
            String localeFile = String.format(LOCALE_VARIANT_PATTERN_RULES_FILE_NAME, locale.getLanguage(), locale.getCountry(), locale.getVariant());
            if (rulesDirPathExists(localeFile)) {
                try {
                    rules.addAll(getPatternRules(getRulesDirPath(localeFile), getRuleFilterCreator()));
                } catch(Exception e) {
                    throw new IOException(String.format("Unable to load pattern rules from: %1$s", localeFile), e);
                }
            }
        }
        patternRules = rules;
        return patternRules;
    }

}
