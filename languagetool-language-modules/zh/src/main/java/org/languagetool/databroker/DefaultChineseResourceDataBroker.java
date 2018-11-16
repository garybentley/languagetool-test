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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;
import java.io.InputStream;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import org.ictclas4j.segment.SegTag;

import cn.com.cjf.CJFBeanFactory;
import cn.com.cjf.ChineseJF;

import org.languagetool.language.Chinese;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.zh.ChineseTagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.zh.ChineseWordTokenizer;
import org.languagetool.tokenizers.zh.ChineseSentenceTokenizer;
import org.languagetool.languagemodel.LuceneLanguageModel;

public class DefaultChineseResourceDataBroker extends DefaultResourceDataBroker implements ChineseResourceDataBroker {

    /**
     * The filename to use for the base hunspell binary dictionary info.  The locale language and country values are replaced in the filename.
     * For en_GB this would become: /en/hunspell/en_GB.info
     */
    public static String PLAIN_TEXT_SPELLING_INFO_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.info";

    /**
     * The filename to use for the base hunspell binary dictionary.  The locale language and country values are replaced in the filename.
     * For ru_RU this would become: ru/hunspell/ru_RU.dict
     */
    public static String BINARY_DICT_FILE_NAME = "%1$s/hunspell/%1$s_%2$s.dict";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    private ChineseSentenceTokenizer sentenceTokenizer;
    private Set<Dictionary> dictionaries;
    private SegTag segTag;
    private ChineseJF chineseJF;
    private ChineseWordTokenizer wordTokenizer;
    private ChineseTagger tagger;
    private LuceneLanguageModel languageModel;

    public DefaultChineseResourceDataBroker(Chinese lang, ClassLoader classLoader) throws Exception {
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

    public SegTag getSegTag() throws Exception {
        if (segTag == null) {
            Path coreDictPath = getResourceDirPath("/zh/coreDict.dct");
            Path bigramDictPath = getResourceDirPath("/zh/BigramDict.dct");
            Path personTaggerDctPath = getResourceDirPath("/zh/nr.dct");
            Path personTaggerCtxPath = getResourceDirPath("/zh/nr.ctx");
            Path transPersonTaggerDctPath = getResourceDirPath("/zh/tr.dct");
            Path transPersonTaggerCtxPath = getResourceDirPath("/zh/tr.ctx");
            Path placeTaggerDctPath = getResourceDirPath("/zh/ns.dct");
            Path placeTaggerCtxPath = getResourceDirPath("/zh/ns.ctx");
            Path lexTaggerCtxPath = getResourceDirPath("/zh/lexical.ctx");
            try (InputStream coreDictIn = Files.newInputStream(coreDictPath);
                 InputStream bigramDictIn = Files.newInputStream(bigramDictPath);
                 InputStream personTaggerDctIn = Files.newInputStream(personTaggerDctPath);
                 InputStream personTaggerCtxIn = Files.newInputStream(personTaggerCtxPath);
                 InputStream transPersonTaggerDctIn = Files.newInputStream(transPersonTaggerDctPath);
                 InputStream transPersonTaggerCtxIn = Files.newInputStream(transPersonTaggerCtxPath);
                 InputStream placeTaggerDctIn = Files.newInputStream(placeTaggerDctPath);
                 InputStream placeTaggerCtxIn = Files.newInputStream(placeTaggerCtxPath);
                 InputStream lexTaggerCtxIn = Files.newInputStream(lexTaggerCtxPath);
            ) {
              segTag = new SegTag(1, coreDictIn, bigramDictIn, personTaggerDctIn, personTaggerCtxIn,
                      transPersonTaggerDctIn, transPersonTaggerCtxIn, placeTaggerDctIn, placeTaggerCtxIn,
                      lexTaggerCtxIn);
            }
        }
        return segTag;
    }

    @Override
    public Tagger getTagger() throws Exception {
        if (tagger == null) {
            tagger = new ChineseTagger();
        }
        return tagger;
    }

    public ChineseJF getChineseJF() throws Exception {
        if (chineseJF == null) {
          chineseJF = CJFBeanFactory.getChineseJF();
        }
        return chineseJF;
    }

    /**
     * Get the word tokenizer.
     */
    @Override
    public Tokenizer getWordTokenizer() throws Exception {
        if (wordTokenizer == null) {
            wordTokenizer = new ChineseWordTokenizer(getChineseJF(), getSegTag());
        }
        return wordTokenizer;
    }

    /**
     * Get the sentence tokenizer.
     *
     * @return The sentence tokenizer.
     */
    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        if (sentenceTokenizer == null) {
            sentenceTokenizer = new ChineseSentenceTokenizer();
        }
        return sentenceTokenizer;
    }

    @Override
    public LuceneLanguageModel getLanguageModel() throws Exception {
        if (languageModel == null) {
            languageModel = createLanguageModelFromResourcePath();
        }
        return languageModel;
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
