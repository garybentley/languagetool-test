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
import org.languagetool.language.Tamil;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.language.tagging.TamilTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

public class DefaultTamilResourceDataBroker extends DefaultResourceDataBroker implements TamilResourceDataBroker {

    public static String WORD_TAGGER_DICT_FILE_NAME = "%1$s/tamil.dict";

    public static String COMPOUNDS_FILE_NAME = "%1$s/compounds.txt";

    private Tagger tagger;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private SentenceTokenizer sentenceTokenizer;

    public DefaultTamilResourceDataBroker(Tamil lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
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
        tagger = new TamilTagger(getWordTaggerDictionary(), getWordTagger(), getCaseConverter());
      }
      return tagger;
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getDefaultSentenceTokenizer();
    }

}
