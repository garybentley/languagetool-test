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
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.languagetool.language.Persian;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.PersianWordTokenizer;
import org.languagetool.tagging.disambiguation.Disambiguator;

public class DefaultPersianResourceDataBroker extends DefaultResourceDataBroker implements PersianResourceDataBroker {

    public static String COHERENCY_WORD_LIST_FILE_NAME = "/%1$s/coherency.txt";
    public static String REPLACE_FILE_NAME = "/%1$s/replace.txt";

    private Map<String, List<String>> wrongWords;
    private PersianWordTokenizer wordTokenizer;
    private Map<String, String> coherencyMappings;

    public DefaultPersianResourceDataBroker(Persian lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
     * Get the wrong words for a locale, files in use are:
     *   - /rules/en/en-<locale.countrycode>/replace.txt
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
    public Map<String, String> getCoherencyMappings() throws Exception {
        if (coherencyMappings == null) {
            String fileName = String.format(COHERENCY_WORD_LIST_FILE_NAME, language.getLocale().getLanguage());
            coherencyMappings = createCoherencyMappingsFromRulesPath(fileName);
        }
        return coherencyMappings;
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

    @Override
    public PersianWordTokenizer getWordTokenizer() {
        if (wordTokenizer == null) {
            wordTokenizer = new PersianWordTokenizer();
        }
        return wordTokenizer;
    }

    /**
     * Persian doesn't have a disambiguator, return null.
     *
     * @return null.
     */
    @Override @Nullable
    public Disambiguator getDisambiguator() throws Exception {
        return null;
    }
}
