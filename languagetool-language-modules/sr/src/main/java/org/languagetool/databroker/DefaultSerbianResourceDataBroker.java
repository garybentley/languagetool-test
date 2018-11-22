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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;

import org.languagetool.language.Serbian;
import org.languagetool.UserConfig;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.sr.SerbianHybridDisambiguator;

public abstract class DefaultSerbianResourceDataBroker extends DefaultResourceDataBroker implements SerbianResourceDataBroker {

    public static String BARBARISMS_GRAMMAR_RULES_FILE_NAME = "%1$s/grammar-barbarism.xml";

    public static String LOGICAL_GRAMMAR_RULES_FILE_NAME = "%1$s/grammar-logical.xml";

    public static String PUNCTUATION_GRAMMAR_RULES_FILE_NAME = "%1$s/grammar-punctuation.xml";

    public static String SPELLING_GRAMMAR_RULES_FILE_NAME = "%1$s/grammar-spelling.xml";

    public static String STYLE_GRAMMAR_RULES_FILE_NAME = "%1$s/grammar-style.xml";

    private SentenceTokenizer sentenceTokenizer;
    private List<AbstractPatternRule> patternRules;
    private Disambiguator disambiguator;

    public DefaultSerbianResourceDataBroker(Serbian lang, ClassLoader classLoader) throws Exception {
        super(lang, classLoader);
    }

    /**
     * Get the disambiguator.  We override to return a specialization, however we still use {@link super.getDisambiguator()} to get the
     * xml rule based disambiguator.
     *
     * @return The disambiguator.
     */
    @Override
    public Disambiguator getDisambiguator() throws Exception {
        if (disambiguator == null) {
          disambiguator = new SerbianHybridDisambiguator(createMultiWordChunkerFromResourcePath(false), super.getDisambiguator());
        }
        return disambiguator;
    }

    @Override
    public List<AbstractPatternRule> getPatternRules() throws Exception {
        if (patternRules == null) {
            List<AbstractPatternRule> rules = super.getPatternRules();

            // Get our extra rules.
            String file = String.format(BARBARISMS_GRAMMAR_RULES_FILE_NAME, language.getLocale().getLanguage());
            if (rulesDirPathExists(file)) {
                rules.addAll(getPatternRules(getRulesDirPath(file), getRuleFilterCreator()));
            }

            file = String.format(LOGICAL_GRAMMAR_RULES_FILE_NAME, language.getLocale().getLanguage());
            if (rulesDirPathExists(file)) {
                rules.addAll(getPatternRules(getRulesDirPath(file), getRuleFilterCreator()));
            }

            file = String.format(PUNCTUATION_GRAMMAR_RULES_FILE_NAME, language.getLocale().getLanguage());
            if (rulesDirPathExists(file)) {
                rules.addAll(getPatternRules(getRulesDirPath(file), getRuleFilterCreator()));
            }

            file = String.format(SPELLING_GRAMMAR_RULES_FILE_NAME, language.getLocale().getLanguage());
            if (rulesDirPathExists(file)) {
                rules.addAll(getPatternRules(getRulesDirPath(file), getRuleFilterCreator()));
            }

            file = String.format(STYLE_GRAMMAR_RULES_FILE_NAME, language.getLocale().getLanguage());
            if (rulesDirPathExists(file)) {
                rules.addAll(getPatternRules(getRulesDirPath(file), getRuleFilterCreator()));
            }

            patternRules = rules;
        }
        return patternRules;
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

}
