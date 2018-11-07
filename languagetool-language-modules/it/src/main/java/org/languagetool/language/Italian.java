/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.it.ItalianConfusionProbabilityRule;
import org.languagetool.rules.it.ItalianWordRepeatRule;
import org.languagetool.rules.it.MorfologikItalianSpellerRule;
import org.languagetool.databroker.*;

import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SentenceTokenizer;

import org.languagetool.tagging.disambiguation.Disambiguator;

public class Italian extends Language<ItalianResourceDataBroker> {

    public static final String LANGUAGE_ID = "it";
    public static final String COUNTRY_ID = "IT";
    public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  public ItalianResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultItalianResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public Language getDefaultLanguageVariant() {
      return null;
  }

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public String getName() {
    return "Italian";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"IT", "CH"};
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
  public Contributor[] getMaintainers() {
    Contributor contributor = new Contributor("Paolo Bianchini");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createWhitespaceBeforePunctuationRule(messages),
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createMorfologikSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createWordRepeatRule(messages),
            createMultipleWhitespaceRule(messages)
    );
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public ItalianWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new ItalianWordRepeatRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public MorfologikItalianSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikItalianSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords());
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
              Arrays.asList("[", "(", "{", "»", "«" /*"‘"*/),
              Arrays.asList("]", ")", "}", "«", "»" /*"’"*/));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public WhitespaceBeforePunctuationRule createWhitespaceBeforePunctuationRule(ResourceBundle messages) throws Exception {
      return new WhitespaceBeforePunctuationRule(getUseMessages(messages));
  }

  /** @since 3.1 */
  @Override
  public LanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.<Rule>asList(
            createConfusionProbabilityRule(messages, languageModel)
    );
  }

  public ItalianConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new ItalianConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
