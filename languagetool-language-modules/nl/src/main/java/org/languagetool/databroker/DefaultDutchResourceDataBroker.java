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
import java.util.HashSet;
import java.util.Collections;

import java.util.stream.Collectors;

import java.nio.charset.*;

import morfologik.stemming.IStemmer;
import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;

import org.languagetool.UserConfig;
import org.languagetool.language.Dutch;
import org.languagetool.synthesis.nl.DutchSynthesizer;
import org.languagetool.tagging.nl.DutchTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.rules.nl.PreferredWordRuleWithSuggestion;
import org.languagetool.rules.nl.PreferredWordRule;
import org.languagetool.AnalyzedTokenReadings;

public class DefaultDutchResourceDataBroker extends DefaultResourceDataBroker implements DutchResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/dutch.dict";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/dutch_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/dutch_tags.txt";
    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";
    public static String REPLACE_FILE_NAME = "%1$s/replace.txt";

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For nl_NL this would become: /nl/spelling/nl_NL.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/spelling/%1$s_%2$s.info";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/spelling/spelling.txt";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For nl_NL this would become: /nl/spelling/nl_NL.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/spelling/%1$s_%2$s.dict";

    public static String WRONG_WORDS_IN_CONTEXT_FILE_NAME = "%1$s/wrongWordInContext.txt";

    public static String COHERENCY_WORD_LIST_FILE_NAME = "%1$s/coherency.txt";

    public static String PREFERRED_WORDS_FILE_NAME = "%1$s/preferredwords.csv";

    public static String PROHIBITED_WORDS_FILE_NAME = "%1$s/spelling/prohibit.txt";
    public static String IGNORE_WORDS_FILE_NAME = "%1$s/spelling/ignore.txt";

    private DutchTagger tagger;
    private DutchWordTokenizer wordTokenizer;
    private DutchSynthesizer synthesizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private CompoundRuleData compounds;
    private List<Map<String, String>> wrongWords;
    private Set<Dictionary> dictionaries;
    private List<ContextWords> wrongWordsInContext;
    private Map<String, String> coherencyMappings;

    public DefaultDutchResourceDataBroker(Dutch lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public List<PreferredWordRuleWithSuggestion> getPreferredWordRules() throws Exception {
        String message = "Voor dit woord is een gebruikelijker alternatief.";
        String shortMessage = "Gebruikelijker woord";
        StringProcessor<PreferredWordRuleWithSuggestion> proc = new StringProcessor<PreferredWordRuleWithSuggestion>() {
            @Override
            public boolean shouldSkip(String line) {
                line = line.trim();
                return line.startsWith("#");
            }
            @Override
            public Set<String> getErrors(String line) {
                Set<String> errors = null;
                line = line.trim();
                String[] parts = line.split(";");
                if (parts.length != 2) {
                    errors = new HashSet<>();
                    errors.add ("Expected format: <oldword>;<newword>");
                }
                return errors;
            }
            @Override
            public PreferredWordRuleWithSuggestion getProcessed(String line) throws Exception {
                line = line.trim();
                String[] parts = line.split(";");
                String oldWord = parts[0];
                String newWord = parts[1];
                List<PatternToken> patternTokens = getTokens(oldWord);
                PatternRule rule = new PatternRule("NL_PREFERRED_WORD_RULE_INTERNAL", language, patternTokens, PreferredWordRule.DESC, message, shortMessage);
                return new PreferredWordRuleWithSuggestion(rule, oldWord, newWord);
            }
        };
        return loadWordsFromResourcePath(String.format(PREFERRED_WORDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, proc);
    }

    private List<PatternToken> getTokens(String oldWord) throws Exception {
      PatternTokenBuilder builder = new PatternTokenBuilder();
      String[] newWordTokens = oldWord.split(" ");
      List<PatternToken> patternTokens = new ArrayList<>();
      for (String part : newWordTokens) {
        PatternToken token;
        if (isBaseform(oldWord)) {
          token = builder.csToken(part).matchInflectedForms().build();
        } else {
          token = builder.csToken(part).build();
        }
        patternTokens.add(token);
      }
      return patternTokens;
    }

    private boolean isBaseform(String term) throws Exception {
        AnalyzedTokenReadings lookup = getTagger().tag(Collections.singletonList(term)).get(0);
        if (lookup != null) {
          return lookup.hasLemma(term);
        }
        return false;
    }

    @Override
    public Map<String, String> getCoherencyMappings() throws Exception {
        if (coherencyMappings == null) {
            String fileName = String.format(COHERENCY_WORD_LIST_FILE_NAME, language.getLocale().getLanguage());
            coherencyMappings = createCoherencyMappingsFromRulesPath(fileName);
        }
        return coherencyMappings;
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

    @Override
    public List<String> getSpellingProhibitedWords() throws Exception {
        return loadSpellingProhibitedWordsFromResourcePath(String.format(PROHIBITED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    /**
     * Get the case converter for Dutch.
     *
     * Note: this is just a pass through to our super since DefaultCaseConverter handles the Dutch special case but
     * if more complex Dutch case handling is required, this call should return a Dutch specific CaseConverter subclass.
     *
     * @return The case converter.
     */
    @Override
    public CaseConverter getCaseConverter() {
        return super.getCaseConverter();
    }

    /**
     * Get the wrong words for Dutch.
     *
     * @return A map of the words.
     */
    @Override
    public List<Map<String, String>> getWrongWords() throws Exception {
        if (wrongWords == null) {
            String replaceFile = String.format(REPLACE_FILE_NAME, language.getLocale().getLanguage());
            wrongWords = createWrongWords2FromRulesPath(replaceFile, getWordTokenizer());
        }
        return wrongWords;
    }

    @Override
    public List<ContextWords> getWrongWordsInContext() throws Exception {
        if (wrongWordsInContext == null) {
            wrongWordsInContext = loadContextWords(getRulesDirPath(String.format(WRONG_WORDS_IN_CONTEXT_FILE_NAME, language.getLocale().getLanguage())));
        }
        return wrongWordsInContext;
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
    public DutchSynthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new DutchSynthesizer(getIStemmer(), getSynthesizerWordTags());
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
    public DutchTagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new DutchTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
    }

    @Override
    public DutchWordTokenizer getWordTokenizer() throws Exception {
      if (wordTokenizer == null) {
        wordTokenizer = new DutchWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }
}
