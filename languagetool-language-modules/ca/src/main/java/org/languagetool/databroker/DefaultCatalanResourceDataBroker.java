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
import org.languagetool.language.Polish;
import org.languagetool.synthesis.pl.PolishSynthesizer;
import org.languagetool.tagging.disambiguation.pl.PolishHybridDisambiguator;
import org.languagetool.tagging.pl.PolishTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tokenizers.pl.PolishWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.CompoundRuleData;

public class DefaultCatalanResourceDataBroker extends DefaultResourceDataBroker implements CatalanResourceDataBroker {
/*
    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/polish.dict";
    public static String STEMMER_DICT_FILE_NAME = "%1$s/polish_synth.dict";
    public static String SYNTHESIZER_WORD_TAGS_FILE_NAME = "%1$s/polish_tags.txt";
    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";
    public static String REPLACE_FILE_NAME = "%1$s/replace.txt";
*/
    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
//    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

//    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.dict
     */
//    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";
/*
    private PolishTagger tagger;
    private PolishWordTokenizer wordTokenizer;
    private PolishHybridDisambiguator disambiguator;
    private PolishSynthesizer synthesizer;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private IStemmer istemmer;
    private CompoundRuleData compounds;
    private Map<String, List<String>> wrongWords;
    private Set<Dictionary> dictionaries;
*/
    public DefaultCatalanResourceDataBroker(Catalan lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
     * Get the wrong words for Polish.
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
    public List<PatternRule> getCompoundPatternRules(String message) throws Exception {
        return loadCompoundPatternRulesFromResourcePath(String.format(COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, message);
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
    public PolishSynthesizer getSynthesizer() throws Exception {
        if (synthesizer == null) {
            synthesizer = new PolishSynthesizer(getIStemmer(), getSynthesizerWordTags());
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
    public CatalanTagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new PolishTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
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
    public PolishHybridDisambiguator getDisambiguator() throws Exception {
        if (disambiguator == null) {
          disambiguator = new PolishHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
        }
        return disambiguator;
    }

    @Override
    public PolishWordTokenizer getWordTokenizer() throws Exception {
      if (wordTokenizer == null) {
        wordTokenizer = new PolishWordTokenizer();
        wordTokenizer.setTagger(getTagger());
      }
      return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }
/*
GTODO
    public Disambiguator getChunker() {
        if (chunker == null) {
            chunker = new MultiWordChunker("/pl/multiwords.txt", this);
        }
        return chunker;
    }
*/
}
