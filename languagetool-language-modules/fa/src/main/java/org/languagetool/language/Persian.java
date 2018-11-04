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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.fa.*;
import org.languagetool.tokenizers.PersianWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;

import org.languagetool.databroker.PersianResourceDataBroker;
import org.languagetool.databroker.DefaultPersianResourceDataBroker;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Support for Persian.
 * @since 2.7
 */
public class Persian extends Language<PersianResourceDataBroker> {

  public static final String LANGUAGE_ID = "fa";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID);

  @Override
  public PersianResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public PersianResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultPersianResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override @Nullable
  public Language getDefaultLanguageVariant() {
    return null;
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
  public String getName() {
    return "Persian";
  }
/*
GTODO Clean up
  @Override
  public String getShortCode() {
    return "fa";
  }
*/
  @Override
  public String[] getCountries() {
    return new String[]{"IR", "AF"};
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
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Reza1615"),
        new Contributor("Alireza Eskandarpour Shoferi"),
        new Contributor("Ebrahim Byagowi")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    return Arrays.asList(
        createCommaWhitespaceRule(messages),
        createDoublePunctuationRule(messages),
        createMultipleWhitespaceRule(messages),
        createLongSentenceRule(messages, userConfig),
        // specific to Persian:
        createPersianCommaWhitespaceRule(messages),
        createPersianDoublePunctuationRule(messages),
        createPersianWordRepeatBeginningRule(messages),
        createPersianWordRepeatRule(messages),
        createSimpleReplaceRule(messages),
        createPersianSpaceBeforeRule(messages),
        createWordCoherencyRule(messages)
    );
  }

  public WordCoherencyRule createWordCoherencyRule(ResourceBundle messages) throws Exception {
      return new WordCoherencyRule(getUseMessages(messages), getUseDataBroker().getCoherencyMappings());
  }

  public PersianSpaceBeforeRule createPersianSpaceBeforeRule(ResourceBundle messages) throws Exception {
      return new PersianSpaceBeforeRule(getUseMessages(messages));
  }

  public SimpleReplaceRule createSimpleReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords());
  }

  public PersianWordRepeatRule createPersianWordRepeatRule(ResourceBundle messages) throws Exception {
      return new PersianWordRepeatRule(getUseMessages(messages));
  }

  public PersianWordRepeatBeginningRule createPersianWordRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new PersianWordRepeatBeginningRule(getUseMessages(messages));
  }

  public PersianDoublePunctuationRule createPersianDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new PersianDoublePunctuationRule(getUseMessages(messages));
  }

  public PersianCommaWhitespaceRule createPersianCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new PersianCommaWhitespaceRule(getUseMessages(messages));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages), null, null);
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages), this);
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

}
