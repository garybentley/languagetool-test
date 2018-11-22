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
import java.util.Collections;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.km.KhmerSimpleReplaceRule;
import org.languagetool.rules.km.KhmerUnpairedBracketsRule;
import org.languagetool.rules.km.KhmerWordRepeatRule;
import org.languagetool.rules.km.KhmerSpaceBeforeRule;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;

public class Khmer extends Language<KhmerResourceDataBroker> {

  public static final String LANGUAGE_ID = "km";
  public static final String COUNTRY_ID = "KH";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public KhmerResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public KhmerResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultKhmerResourceDataBroker(this, getClass().getClassLoader());
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
    return "Khmer";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"KH"};
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
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Nathan Wells")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
      createSpellerRule(messages, userConfig),
      // specific to Khmer:
      createReplaceRule(messages),
      createWordRepeatRule(messages),
      createUnpairedBracketsRule(messages),
      createSpaceBeforeRule(messages)
    );
  }

  public HunspellRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new HunspellRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getHunspellDictionary(), getUseDataBroker().getSpellingIgnoreWords(), Collections.emptyList(), null);
  }

  public KhmerUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new KhmerUnpairedBracketsRule(getUseMessages(messages));
  }

  public KhmerWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new KhmerWordRepeatRule(getUseMessages(messages));
  }

  public KhmerSpaceBeforeRule createSpaceBeforeRule(ResourceBundle messages) throws Exception {
      return new KhmerSpaceBeforeRule(getUseMessages(messages));
  }

  public KhmerSimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new KhmerSimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getCoherencyWords(), getUseDataBroker().getCaseConverter());
  }

}
