/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import morfologik.stemming.Dictionary;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.tl.MorfologikTagalogSpellerRule;
import org.languagetool.language.tokenizers.TagalogWordTokenizer;
import org.languagetool.rules.*;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;

/**
 * @author Nathaniel Oco
 * @deprecated this language is unmaintained in LT and might be removed in a future release if we cannot find contributors for it (deprecated since 3.6)
 */
@Deprecated
public class Tagalog extends Language<TagalogResourceDataBroker> {
/*
  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;
  private Tagger tagger;
*/
  public static final String LANGUAGE_ID = "tl";
  public static final String COUNTRY_ID = "PH";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public TagalogResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public TagalogResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultTagalogResourceDataBroker(this, getClass().getClassLoader());
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
    return "Tagalog";
  }

  @Override
  public String[] getCountries() {
    return new String[] {"PH"};
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Nathaniel Oco", "http://www.dlsu.edu.ph/research/centers/adric/nlp/"),
            new Contributor("Allan Borra", "http://www.dlsu.edu.ph/research/centers/adric/nlp/faculty/borra.asp")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createUppercaseSentenceStartRule(messages),
            createMultipleWhitespaceRule(messages),
            // specific to Tagalog:
            createMorfologikSpellerRule(messages, userConfig)
    );
  }

  public MorfologikTagalogSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikTagalogSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords());
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

}
