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

import morfologik.stemming.Dictionary;

import org.languagetool.UserConfig;
import org.languagetool.language.Swedish;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sv.SwedishTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.spelling.hunspell.*;

public class DefaultSwedishResourceDataBroker extends DefaultResourceDataBroker implements SwedishResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/swedish.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s_%2$s";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";

    private Tagger tagger;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private SentenceTokenizer sentenceTokenizer;
    private Hunspell.Dictionary hunspellDict;
    private CompoundRuleData compounds;

    public DefaultSwedishResourceDataBroker(Swedish lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public CompoundRuleData getCompounds() throws Exception {
        if (compounds == null) {
            compounds = createCompoundRuleDataFromResourcePaths(String.format(COMPOUNDS_FILE_NAME, language.getLocale().getLanguage()));
        }
        return compounds;
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
        tagger = new SwedishTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

}
