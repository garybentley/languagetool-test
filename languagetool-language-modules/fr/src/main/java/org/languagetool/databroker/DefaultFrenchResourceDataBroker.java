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

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.nio.charset.*;

import morfologik.stemming.IStemmer;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.Dictionary;
import org.jetbrains.annotations.Nullable;
import org.languagetool.UserConfig;
import org.languagetool.language.French;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tagging.disambiguation.fr.FrenchHybridDisambiguator;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.rules.patterns.RuleFilterCreator;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.fr.FrenchPartialPosTagFilter;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.rules.ConfusionSet;

public class DefaultFrenchResourceDataBroker extends DefaultResourceDataBroker implements FrenchResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For fr_FR this would become: fr/hunspell/fr_FR.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s_%2$s";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For fr_FR this would become: fr/hunspell/fr_FR.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String COHERENCY_WORD_LIST_FILE_NAME = "/%1$s/coherency.txt";
    public static String REPLACE_FILE_NAME = "/%1$s/replace.txt";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "/%1$s/french_tags.txt";
    public static String STEMMER_DICT_FILE_NAME = "/%1$s/french_synth.dict";
    public static String COMPOUNDS_FILE_NAME = "/%1$s/compounds.txt";
    public static String WORD_TAGGER_DICT_FILE_NAME = "/%1$s/french.dict";
    public static String WORD_TAGGER_ADDED_WORDS_FILE_NAME = "/en/added.txt";
    public static String WORD_TAGGER_REMOVED_WORDS_FILE_NAME = "/en/removed.txt";

    public static String PROHIBITED_WORDS_FILE_NAME = "%1$s/hunspell/prohibit.txt";
    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    private FrenchTagger tagger;
    private FrenchSynthesizer synthesizer;
    private FrenchHybridDisambiguator disambiguator;
    private LuceneLanguageModel languageModel;
    private IStemmer istemmer;
    private CompoundRuleData compounds;
    private MultiWordChunker chunker;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private RuleFilterCreator ruleFilterCreator;
    private Hunspell.Dictionary hunspellDict;
    private Set<Dictionary> dictionaries;

    public DefaultFrenchResourceDataBroker(French lang, ClassLoader classLoader) throws Exception {
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
    public Hunspell.Dictionary getHunspellDictionary() throws Exception {
        if (hunspellDict == null) {
            String fileName = String.format(HUNSPELL_BASE_FILE_NAME_PREFIX, language.getLocale().getLanguage(), language.getLocale().getCountry());
            hunspellDict = createHunspellDictionaryFromResourcePath(fileName);
        }
        return hunspellDict;
    }

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public List<String> getSpellingProhibitedWords() throws Exception {
        return loadSpellingProhibitedWordsFromResourcePath(String.format(PROHIBITED_WORDS_FILE_NAME, language.getLocale().getLanguage()));
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
                    if (FrenchPartialPosTagFilter.class.isAssignableFrom(aClass)) {
                        return new FrenchPartialPosTagFilter(getTagger(), getDisambiguator());
                    }
                    return super.getFilter(className);
                }
            };
        }
        return ruleFilterCreator;
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
    public FrenchSynthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new FrenchSynthesizer(getIStemmer(), getSynthesizerWordTags());
        }
        return synthesizer;
    }

    @Override
    public synchronized LuceneLanguageModel getLanguageModel() throws Exception {
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
    public SRXSentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

    /**
     * Get the disambiguator.  We override to return a specialization, however we still use {@link super.getDisambiguator()} to get the
     * xml rule based disambiguator.
     *
     * @return The disambiguator.
     */
    @Override
    public FrenchHybridDisambiguator getDisambiguator() throws Exception {
        if (disambiguator == null) {
          disambiguator = new FrenchHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
        }
        return disambiguator;
    }

    /**
     * Get the tagger.
     *
     * @return The tagger.
     */
    @Override
    public FrenchTagger getTagger() throws Exception {
        if (tagger == null) {
              tagger = new FrenchTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
        }
        return tagger;
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
