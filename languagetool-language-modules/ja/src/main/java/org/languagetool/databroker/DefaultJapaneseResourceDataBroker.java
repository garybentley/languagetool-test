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

import org.languagetool.language.Japanese;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ja.JapaneseWordTokenizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ja.JapaneseTagger;

public class DefaultJapaneseResourceDataBroker extends DefaultResourceDataBroker implements JapaneseResourceDataBroker {

    private JapaneseTagger tagger;
    private JapaneseWordTokenizer wordTokenizer;
    private SentenceTokenizer sentenceTokenizer;

    public DefaultJapaneseResourceDataBroker(Japanese lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public Tagger getTagger() throws Exception {
      if (tagger == null) {
        tagger = new JapaneseTagger();
      }
      return tagger;
    }

    @Override
    public Tokenizer getWordTokenizer() throws Exception {
        if (wordTokenizer == null) {
            wordTokenizer = new JapaneseWordTokenizer();
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
            sentenceTokenizer = getDefaultSentenceTokenizer();
        }
        return sentenceTokenizer;
    }

}
