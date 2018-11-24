/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.tagging.TamilTagger;
import org.languagetool.rules.*;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.databroker.*;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class Tamil extends Language<TamilResourceDataBroker> {
/*
  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
*/
  public static final String LANGUAGE_ID = "ta";
  public static final String COUNTRY_ID = "IN";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public TamilResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public TamilResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultTamilResourceDataBroker(this, getClass().getClassLoader());
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
    return "Tamil";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"IN"};
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Elanjelian Venugopal")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
        createCommaWhitespaceRule(messages),
        createDoublePunctuationRule(messages),
        createMultipleWhitespaceRule(messages),
        createLongSentenceRule(messages, userConfig),
        createSentenceWhitespaceRule(messages)
    );
  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      int confWords = -1;
      if (userConfig != null) {
        confWords = userConfig.getConfigValueByID(LongSentenceRule.getRuleConfiguration().getRuleId());
      }
      return createLongSentenceRule(messages, confWords);
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, int maxWords) throws Exception {
      return new LongSentenceRule(getUseMessages(messages), maxWords);
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

}
