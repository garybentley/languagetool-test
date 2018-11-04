/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.languagemodel.*;
import org.languagetool.rules.de.GermanyGermanSpellerRule;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.rules.spelling.hunspell.Hunspell;
import org.languagetool.tokenizers.Tokenizer;
import morfologik.stemming.Dictionary;
import org.languagetool.rules.de.GermanLineExpander;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;
import java.util.Locale;

public class GermanyGerman extends German {

    public static final String COUNTRY_ID = "DE";
    public static final String LANGUAGE_ID = "de";
    public static final Locale LOCALE = new Locale(German.LOCALE.getLanguage(), COUNTRY_ID);

  @Override
  public String[] getCountries() {
    return new String[]{COUNTRY_ID};
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return null;
  }

  @Override
  public boolean isVariant() {
      return true;
  }

  @Override
  public String getName() {
    return "German (Germany)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, altLanguages));
    rules.add(createSpellerRule(messages, userConfig));
    return rules;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      List<Rule> rules = new ArrayList<>(super.getRelevantLanguageModelRules(messages, languageModel));

      if (languageModel instanceof BaseLanguageModel) {
          // GTODO It looks like user allowed spellings will be missed here.
          rules.add (createProhibitedCompoundRule(messages, (BaseLanguageModel) languageModel, createSpellerRule(messages, null)));
      }

      return rules;
  }

  public GermanyGermanSpellerRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      Set<Dictionary> dicts = getUseDataBroker().getDictionaries(userConfig);
      MorfologikMultiSpeller speller = new MorfologikMultiSpeller(dicts, userConfig, 2);
      Hunspell.Dictionary hdic = getUseDataBroker().getHunspellDictionary();
      List ignoreWords = getUseDataBroker().getSpellingIgnoreWords();
      List prohibWords = getUseDataBroker().getSpellingProhibitedWords();
      return new GermanyGermanSpellerRule(getUseMessages(messages), this, speller, userConfig, hdic,
            getUseDataBroker().getTagger(), getUseDataBroker().getSynthesizer(), getUseDataBroker().getStrictCompoundTokenizer(), getUseDataBroker().getNonStrictCompoundSplitter(),
            ignoreWords, prohibWords, new GermanLineExpander());
  }

}
