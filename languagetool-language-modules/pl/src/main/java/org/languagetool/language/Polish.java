/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber & Marcin Miłkowski (http://www.languagetool.org)
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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.pl.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.pl.PolishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.databroker.*;

public class Polish extends Language<PolishResourceDataBroker> {

  public static final String LANGUAGE_ID = "pl";
  public static final String COUNTRY_ID = "PL";
  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  public PolishResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultPolishResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public Language getDefaultLanguageVariant() {
      return null;
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public String getName() {
    return "Polish";
  }

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public String[] getCountries() {
    return new String[]{"PL"};
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public WordTokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }


  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  @Override
  public Synthesizer getSynthesizer() throws Exception {
      return getUseDataBroker().getSynthesizer();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { Contributors.MARCIN_MILKOWSKI };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
        createCommaWhitespaceRule(messages),
        createUppercaseSentenceStartRule(messages),
        createWordRepeatRule(messages),
        createMultipleWhitespaceRule(messages),
        createSentenceWhitespaceRule(messages),
        // specific to Polish:
        createUnpairedBracketsRule(messages),
        createMorfologikSpellerRule(messages, userConfig),
        createPolishWordRepeatRule(messages),
        createCompoundRule(messages),
        createReplaceRule(messages),
        createDashRule(messages)
        );
  }

  public MorfologikPolishSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikPolishSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig),
                      getUseDataBroker().getTagger(), getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getCaseConverter());
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public PolishUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new PolishUnpairedBracketsRule(getUseMessages(messages));
  }

  public SimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public CompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new CompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public DashRule createDashRule(ResourceBundle messages) throws Exception {
      // GTODO Shoudl really use a value from messages here.
      return new DashRule(getUseDataBroker().getCompoundPatternRules("Błędne użycie myślnika zamiast łącznika. Poprawnie: "));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
  }

  public PolishWordRepeatRule createPolishWordRepeatRule(ResourceBundle messages) throws Exception {
      return new PolishWordRepeatRule(getUseMessages(messages));
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
