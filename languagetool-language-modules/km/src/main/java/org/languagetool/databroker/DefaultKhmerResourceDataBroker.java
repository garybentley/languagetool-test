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

import org.languagetool.language.Khmer;
import org.languagetool.UserConfig;
import org.languagetool.AnalyzedToken;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tagging.km.KhmerTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tokenizers.km.KhmerWordTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.spelling.hunspell.*;

public class DefaultKhmerResourceDataBroker extends DefaultResourceDataBroker implements KhmerResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s_%2$s";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/khmer.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String REPLACE_FILE_NAME = "%1$s/replace.txt";

    public static String COHERENCY_WORDS_FILE_NAME = "%1$s/coherency.txt";

    private Tagger tagger;
    private SentenceTokenizer sentenceTokenizer;
    private Tokenizer wordTokenizer;

    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private List<String> spellingIgnoreWords;
    private Hunspell.Dictionary hunspellDict;
    private List<Map<String, String>> coherencyWords;

    public DefaultKhmerResourceDataBroker(Khmer lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
     * Get the wikipedai words for Portuguese.
     *
     * @return A map of the words.
     */
    @Override
    public List<Map<String, String>> getCoherencyWords() throws Exception {
        if (coherencyWords == null) {
            String file = String.format(COHERENCY_WORDS_FILE_NAME, language.getLocale().getLanguage());
            coherencyWords = createWrongWords2FromRulesPath(file, getWordTokenizer());
        }
        return coherencyWords;
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
    public Tokenizer getWordTokenizer() {
      if (wordTokenizer == null) {
        wordTokenizer = new KhmerWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public Tagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new KhmerTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
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

}
