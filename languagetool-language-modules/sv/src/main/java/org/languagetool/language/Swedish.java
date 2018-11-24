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

import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Collections;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.rules.sv.CompoundRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.databroker.*;

/**
 * @deprecated this language is unmaintained in LT and might be removed in a future release if we cannot find contributors for it (deprecated since 3.6)
 */
@Deprecated
public class Swedish extends Language<SwedishResourceDataBroker> {

  public static final String LANGUAGE_ID = "sv";
  public static final String COUNTRY_ID = "SE";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public SwedishResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public SwedishResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultSwedishResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return null;
  }

  @Override
  public String getName() {
    return "Swedish";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"SE", "FI"};
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
    return new Contributor[] {new Contributor("Niklas Johansson")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createWordRepeatRule(messages),
            createMultipleWhitespaceRule(messages),
            createCompoundRule(messages)
    );
  }

  public CompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new CompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public HunspellRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new HunspellRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getHunspellDictionary(), getUseDataBroker().getSpellingIgnoreWords(), Collections.emptyList(), null);
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages));
  }

}
