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

import morfologik.stemming.Dictionary;

import org.languagetool.language.Esperanto;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.eo.EsperantoWordTokenizer;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.MorfologikTagger;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.eo.EsperantoTagger;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.spelling.hunspell.*;

public class DefaultEsperantoResourceDataBroker extends DefaultResourceDataBroker implements EsperantoResourceDataBroker {

    public static String MANUAL_WORD_TAGGER_FILE_NAME = "%1$s/manual-tagger.txt";

    public static String PLAIN_TEXT_BASE_SPELLING_FILE_NAME = "%1$s/hunspell/spelling.txt";

    public static String IGNORE_WORDS_FILE_NAME = "%1$s/hunspell/ignore.txt";

    public static String TRANSITIVE_VERBS_FILE_NAME = "%1$s/verb-tr.txt";

    public static String INTRANSITIVE_VERBS_FILE_NAME = "%1$s/verb-ntr.txt";

    public static String NON_PARTICIPLES_FILE_NAME = "/eo/root-ant-at.txt";

    public static String HUNSPELL_BASE_FILE_NAME_PREFIX = "%1$s";

    private static final StringProcessor<String> taggerProc = new StringProcessor<String>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.charAt(0) == '#';
        }
        @Override
        public String getProcessed(String line) {
            return line.trim();
        }
    };

    private SentenceTokenizer sentenceTokenizer;
    private WordTokenizer wordTokenizer;
    private EsperantoTagger tagger;
    private Dictionary wordTaggerDictionary;
    private WordTagger wordTagger;
    private Set<String> transitiveVerbs;
    private Set<String> intransitiveVerbs;
    private Set<String> nonParticiples;
    private Hunspell.Dictionary hunspellDict;

    public DefaultEsperantoResourceDataBroker(Esperanto lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    @Override
    public Set<String> getTransitiveVerbs() throws Exception {
        if (transitiveVerbs == null) {
            transitiveVerbs = new LinkedHashSet(loadWordsFromRulesPath(String.format(TRANSITIVE_VERBS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, taggerProc));
        }
        return transitiveVerbs;
    }

    @Override
    public Set<String> getIntransitiveVerbs() throws Exception {
        if (intransitiveVerbs == null) {
            intransitiveVerbs = new LinkedHashSet(loadWordsFromRulesPath(String.format(INTRANSITIVE_VERBS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, taggerProc));
        }
        return intransitiveVerbs;
    }

    @Override
    public Set<String> getNonParticiples() throws Exception {
        if (nonParticiples == null) {
            nonParticiples = new LinkedHashSet(loadWordsFromRulesPath(String.format(NON_PARTICIPLES_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, taggerProc));
        }
        return nonParticiples;
    }

    @Override
    public Hunspell.Dictionary getHunspellDictionary() throws Exception {
        if (hunspellDict == null) {
            String fileName = String.format(HUNSPELL_BASE_FILE_NAME_PREFIX, language.getLocale().getLanguage());            
            hunspellDict = createHunspellDictionaryFromResourcePath(fileName);
        }
        return hunspellDict;
    }

    @Override
    public WordTagger getWordTagger() throws Exception {
        if (wordTagger == null) {
            Path path = getResourceDirPath(String.format(MANUAL_WORD_TAGGER_FILE_NAME, language.getLocale().getLanguage()));
            try (InputStream is = Files.newInputStream(path)) {
                wordTagger = new ManualTagger(is);
            } catch(Exception e) {
                throw new IOException(String.format("Unable to create word tagger from path: %1$s", path), e);
            }
        }
        return wordTagger;
    }

    @Override
    public List<String> getSpellingIgnoreWords() throws Exception {
        return loadSpellingIgnoreWordsFromResourcePath(String.format(PLAIN_TEXT_BASE_SPELLING_FILE_NAME, language.getLocale().getLanguage()), String.format(IGNORE_WORDS_FILE_NAME, language.getLocale().getLanguage()));
    }

    @Override
    public Tokenizer getWordTokenizer() {
      if (wordTokenizer == null) {
        wordTokenizer = new EsperantoWordTokenizer();
      }
      return wordTokenizer;
    }

    @Override
    public Tagger getTagger() throws Exception {
        if (tagger == null) {
            tagger = new EsperantoTagger(getTransitiveVerbs(), getIntransitiveVerbs(), getNonParticiples(), getWordTagger());
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
        if (sentenceTokenizer == null) {
            sentenceTokenizer = getDefaultSentenceTokenizer();
        }
        return sentenceTokenizer;
    }

}
