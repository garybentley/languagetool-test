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
import java.io.*;

import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.language.Icelandic;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;

public class DefaultIcelandicResourceDataBroker extends DefaultResourceDataBroker implements IcelandicResourceDataBroker {

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s_%2$s";

    private SentenceTokenizer sentenceTokenizer;
    private Hunspell.Dictionary hunspellDict;

    public DefaultIcelandicResourceDataBroker(Icelandic lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public Hunspell.Dictionary getHunspellDictionary() throws Exception {
        if (hunspellDict == null) {
            String fileName = String.format(HUNSPELL_BASE_FILE_NAME_PREFIX, language.getLocale().getLanguage(), language.getLocale().getCountry());
            hunspellDict = createHunspellDictionaryFromResourcePath(fileName);
        }
        return hunspellDict;
    }

    /**
     * Get the sentence tokenizer.
     *
     * @return The sentence tokenizer.
     */
    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        if (sentenceTokenizer == null) {
            sentenceTokenizer = getDefaultSentenceTokenizer();
        }
        return sentenceTokenizer;
    }

}
