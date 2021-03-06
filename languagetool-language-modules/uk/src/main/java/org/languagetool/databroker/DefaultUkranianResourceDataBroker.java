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
import java.util.Arrays;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.stream.Collectors;

import java.nio.charset.*;

import morfologik.stemming.IStemmer;
import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;

import org.languagetool.UserConfig;
import org.languagetool.language.Ukrainian;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tagging.disambiguation.uk.UkrainianHybridDisambiguator;
import org.languagetool.tagging.disambiguation.uk.SimpleDisambiguator;
import org.languagetool.tagging.disambiguation.uk.UkrainianMultiwordChunker;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.uk.UkrainianTagger;
import org.languagetool.tokenizers.uk.UkrainianWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.uk.*;

public class DefaultUkranianResourceDataBroker extends DefaultResourceDataBroker implements UkranianResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/spanish.dict";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/ukrainian_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/ukrainian_tags.txt";

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

    public static String WRONG_WORDS_FILE_NAME = "%1$s/replace.txt";
    public static String SOFT_WRONG_WORDS_FILE_NAME = "%1$s/replace_soft.txt";
    public static String RENAMED_WRONG_WORDS_FILE_NAME = "%1$s/replace_renamed.txt";
    public static String DASH_PREFIXES_FILE_NAME = "%1$s/dash_prefixes.txt";
    public static String DASH_LEFT_MASTERS_FILE_NAME = "%1$s/dash_left_master.txt";
    public static String DASH_SLAVES_FILE_NAME = "%1$s/dash_slaves.txt";
    public static String DISAMBIGUATOR_REMOVE_FILE_NAME = "%1$s/disambig_remove.txt";
    public static String CASE_GOVERNMENT_FILE_NAME = "%1$s/case_government.txt";
    public static String MASC_FEM_FILE_NAME = "%1$s/masc_fem.txt";

    public static final StringProcessor<String> dashPrefixesProc = new StringProcessor<String>() {
        @Override
        public boolean shouldSkip(String line) {
            return line.trim().startsWith("#");
        }
        @Override
        public String getProcessed(String line) {
            return line;
        }
    };

    public static final StringProcessor<Pair<String, SimpleDisambiguator.TokenMatcher>> disambigRemoveMapProc = new StringProcessor<Pair<String, SimpleDisambiguator.TokenMatcher>>() {
        @Override
        public boolean shouldSkip(String line) {
            return line.startsWith("#") || line.trim().isEmpty();
        }
        @Override
        public Pair<String, SimpleDisambiguator.TokenMatcher> getProcessed(String line) {
            line = line.replaceFirst(" *#.*", "");

            String[] parts = line.trim().split(" ", 2);

            String[] matchers = parts[1].split("\\|");
            List<SimpleDisambiguator.MatcherEntry> matcherEntries = new ArrayList<>();
            for (String string : matchers) {
              String[] matcherParts = string.split(" ");
              matcherEntries.add(new SimpleDisambiguator.MatcherEntry(matcherParts[0], matcherParts[1]));
            }
            return new ImmutablePair(parts[0], new SimpleDisambiguator.TokenMatcher(matcherEntries));
        }
    };

    public static final StringProcessor<Pair<String, List<String>>> renamedWrongWordsProc = new StringProcessor<Pair<String, List<String>>>() {
        @Override
        public boolean shouldSkip(String line) {
            return line.startsWith("#") && ! line.trim().isEmpty();
        }
        @Override
        public Pair<String, List<String>> getProcessed(String line) {
            String[] split = line.split(" *= *|\\|");
            List<String> list = Arrays.asList(split).subList(1, split.length);
            return new ImmutablePair(split[0], list);
        }
    };

    public static final StringProcessor<Pair<String, Set<String>>> caseGovMappingsProc = new StringProcessor<Pair<String, Set<String>>>() {
        @Override
        public boolean shouldSkip(String line) {
            return line.trim().isEmpty();
        }
        @Override
        public Pair<String, Set<String>> getProcessed(String line) {
            String[] parts = line.split(" ");
            String[] vidm = parts[1].split(":");
            return new ImmutablePair(parts[0], new LinkedHashSet<>(Arrays.asList(vidm)));
        }
    };

    private Tagger tagger;
    private Tokenizer wordTokenizer;
    private Disambiguator disambiguator;
    private Synthesizer synthesizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private Set<Dictionary> dictionaries;
    private Map<String, List<String>> wrongWords;
    private Map<String, List<String>> softWrongWords;
    private Map<String, List<String>> renamedWrongWords;
    private Set<String> dashPrefixes;
    private Set<String> dashLeftMasters;
    private Set<String> dashSlaves;
    private Map<String, SimpleDisambiguator.TokenMatcher> disambigRemoveMappings;
    private CaseGovernmentHelper caseGovernmentHelper;
    private TokenAgreementAdjNounExceptionHelper tokenAgreementAdjNounExceptionHelper;
    private TokenAgreementNounVerbExceptionHelper tokenAgreementNounVerbExceptionHelper;

    public DefaultUkranianResourceDataBroker(Ukrainian lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public TokenAgreementAdjNounExceptionHelper getTokenAgreementAdjNounExceptionHelper() throws Exception {
        if (tokenAgreementAdjNounExceptionHelper == null) {
            tokenAgreementAdjNounExceptionHelper = new TokenAgreementAdjNounExceptionHelper(getCaseGovernmentHelper());
        }
        return tokenAgreementAdjNounExceptionHelper;
    }

    @Override
    public CaseGovernmentHelper getCaseGovernmentHelper() throws Exception {
        if (caseGovernmentHelper == null) {
            String file = String.format(CASE_GOVERNMENT_FILE_NAME, language.getLocale().getLanguage());
            Map<String, Set<String>> data = loadWordsFromResourcePath(file, DEFAULT_CHARSET, caseGovMappingsProc).stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            caseGovernmentHelper = new CaseGovernmentHelper(data);
        }
        return caseGovernmentHelper;
    }

    @Override
    public Map<String, SimpleDisambiguator.TokenMatcher> getDisambiguatorRemoveMappings() throws Exception {
        if (disambigRemoveMappings == null) {
            // GTODO: Note there is one or more duplicate keys the file thus the value mapper passed to the collector which returns the first value found.
            String file = String.format(DISAMBIGUATOR_REMOVE_FILE_NAME, language.getLocale().getLanguage());
            Map<String, SimpleDisambiguator.TokenMatcher> data = loadWordsFromResourcePath(file, DEFAULT_CHARSET, disambigRemoveMapProc).stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue(), (p1, p2) -> p1));
            disambigRemoveMappings = data;
        }
        return disambigRemoveMappings;
    }

    @Override
    public TokenAgreementNounVerbExceptionHelper getTokenAgreementNounVerbExceptionHelper() throws Exception {
        if (tokenAgreementNounVerbExceptionHelper == null) {
            String file = String.format(MASC_FEM_FILE_NAME, language.getLocale().getLanguage());
            // We can reuse the dash prefixes processor here.
            Set<String> femSet = new LinkedHashSet<>(loadWordsFromResourcePath(file, DEFAULT_CHARSET, dashPrefixesProc));
            tokenAgreementNounVerbExceptionHelper = new TokenAgreementNounVerbExceptionHelper(femSet, getCaseGovernmentHelper(), getCaseConverter());
        }
        return tokenAgreementNounVerbExceptionHelper;
    }

    @Override
    public Set<String> getDashPrefixes() throws Exception {
        if (dashPrefixes == null) {
            String file = String.format(DASH_PREFIXES_FILE_NAME, language.getLocale().getLanguage());
            dashPrefixes = new LinkedHashSet<>(loadWordsFromResourcePath(file, DEFAULT_CHARSET, dashPrefixesProc));
        }
        return dashPrefixes;
    }

    @Override
    public Set<String> getDashSlaves() throws Exception {
        if (dashSlaves == null) {
            String file = String.format(DASH_SLAVES_FILE_NAME, language.getLocale().getLanguage());
            dashSlaves = new LinkedHashSet<>(loadWordsFromResourcePath(file, DEFAULT_CHARSET, dashPrefixesProc));
        }
        return dashPrefixes;
    }

    @Override
    public Set<String> getDashLeftMasters() throws Exception {
        if (dashLeftMasters == null) {
            String file = String.format(DASH_LEFT_MASTERS_FILE_NAME, language.getLocale().getLanguage());
            dashLeftMasters = new LinkedHashSet<>(loadWordsFromResourcePath(file, DEFAULT_CHARSET, dashPrefixesProc));
        }
        return dashLeftMasters;
    }

    /**
     * Get the wrong words.
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
     * Get the wrong words.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getSoftWrongWords() throws Exception {
        if (softWrongWords == null) {
            String file = String.format(SOFT_WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage());
            softWrongWords = createWrongWordsFromRulesPath(file);
        }
        return softWrongWords;
    }

    /**
     * Get the wrong words.
     *
     * @return A map of the words.
     */
    @Override
    public Map<String, List<String>> getRenamedWrongWords() throws Exception {
        if (renamedWrongWords == null) {
            String file = String.format(RENAMED_WRONG_WORDS_FILE_NAME, language.getLocale().getLanguage());
            Map<String, List<String>> data = loadWordsFromRulesPath(file, DEFAULT_CHARSET, renamedWrongWordsProc).stream()
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
            renamedWrongWords = data;
        }
        return renamedWrongWords;
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
        tagger = new UkrainianTagger(getWordTaggerDictionary(), getWordTagger(), getDashPrefixes(), getDashLeftMasters(), getDashSlaves(), getCaseConverter());
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
          String file = String.format(MULTI_WORDS_FILE_NAME, language.getLocale().getLanguage());
          disambiguator = new UkrainianHybridDisambiguator(new UkrainianMultiwordChunker(createMultiWordChunker2MappingFromResourcePath(file, DEFAULT_CHARSET), true), super.getDisambiguator(), getDisambiguatorRemoveMappings(), getCaseConverter());
        }
        return disambiguator;
    }

    @Override
    public Tokenizer getWordTokenizer() throws Exception {
      if (wordTokenizer == null) {
        wordTokenizer = new UkrainianWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

}
