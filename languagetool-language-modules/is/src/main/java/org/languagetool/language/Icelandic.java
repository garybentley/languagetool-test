/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
import java.util.Locale;
import java.util.Collections;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.databroker.*;
import org.languagetool.rules.spelling.hunspell.*;

/**
 * @author Anton Karl Ingason
 * @deprecated this language is unmaintained in LT and might be removed in a future release if we cannot find contributors for it (deprecated since 3.6)
 */
@Deprecated
public class Icelandic extends Language<IcelandicResourceDataBroker> {

  public static final String LANGUAGE_ID = "is";
  public static final String COUNTRY_ID = "IS";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public IcelandicResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultIcelandicResourceDataBroker(this, getClass().getClassLoader());
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
  public Language getDefaultLanguageVariant() {
      return null;
  }

  @Override
  public String getName() {
    return "Icelandic";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"IS"};
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Anton Karl Ingason")};
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
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createWordRepeatRule(messages),
            createMultipleWhitespaceRule(messages)
    );
  }

  public HunspellNoSuggestionRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      Hunspell.Dictionary hdic = getUseDataBroker().getHunspellDictionary();
      List ignoreWords = getUseDataBroker().getSpellingIgnoreWords();
      return new HunspellNoSuggestionRule(getUseMessages(messages), this, userConfig, hdic, ignoreWords, Collections.emptyList());
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

}
