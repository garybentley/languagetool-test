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
import org.languagetool.language.Tagalog;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.tl.TagalogTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.language.tokenizers.TagalogWordTokenizer;

public class DefaultTagalogResourceDataBroker extends DefaultResourceDataBroker implements TagalogResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/tagalog.dict";

    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For tl_PH this would become: tl/hunspell/tl_PH.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    private Tagger tagger;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private SentenceTokenizer sentenceTokenizer;
    private Tokenizer wordTokenizer;
    private Set<Dictionary> dictionaries;

    public DefaultTagalogResourceDataBroker(Tagalog lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

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
        tagger = new TagalogTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
    }

    @Override
    public Tokenizer getWordTokenizer() throws Exception {
        if (wordTokenizer == null) {
            wordTokenizer = new TagalogWordTokenizer();
        }
        return wordTokenizer;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

}
