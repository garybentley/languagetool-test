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

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.nl.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.nl.DutchSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.nl.DutchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;
import org.languagetool.databroker.*;

public class Dutch extends Language<DutchResourceDataBroker> {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
  private Tokenizer wordTokenizer;

  public static final String LANGUAGE_ID = "nl";
  public static final String COUNTRY_ID = "NL";
  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  public DutchResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultDutchResourceDataBroker(this, getClass().getClassLoader());
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
  public boolean isVariant() {
      return false;
  }

  @Override
  public String getName() {
    return "Dutch";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"NL", "BE"};
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  public Synthesizer getSynthesizer() throws Exception {
      return getUseDataBroker().getSynthesizer();
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
    return new Contributor[] {
            new Contributor("OpenTaal", "http://www.opentaal.org"),
            new Contributor("TaalTik", "http://www.taaltik.nl")
    };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createUppercaseSentenceStartRule(messages),
            createMorfologikSpellerRule(messages, userConfig),
            createMultipleWhitespaceRule(messages),
            createCompoundRule(messages),
            createWrongWordInContextRule(messages),
            createWordCoherencyRule(messages),
            createReplaceRule(messages),
            createLongSentenceRule(messages, userConfig),
            createPreferredWordRule(messages)
    );
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      int confWords = -1;
      if (userConfig != null) {
        confWords = userConfig.getConfigValueByID(LongSentenceRule.getRuleConfiguration().getRuleId());
      }
      return createLongSentenceRule(messages, confWords);
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, int maxWords) throws Exception {
      return new LongSentenceRule(getUseMessages(messages), maxWords, true);
  }

  public CompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new CompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public PreferredWordRule createPreferredWordRule(ResourceBundle messages) throws Exception {
      return new PreferredWordRule(getUseMessages(messages), getUseDataBroker().getPreferredWordRules());
  }

  public WordCoherencyRule createWordCoherencyRule(ResourceBundle messages) throws Exception {
      return new WordCoherencyRule(getUseMessages(messages), getUseDataBroker().getCoherencyMappings());
  }

  public DutchWrongWordInContextRule createWrongWordInContextRule(ResourceBundle messages) throws Exception {
      return new DutchWrongWordInContextRule(getUseMessages(messages), getUseDataBroker().getWrongWordsInContext());
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public SimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
              Arrays.asList("[", "(", "{", "“", "‹", "“", "„", "\""),
              Arrays.asList("]", ")", "}", "”", "›", "”", "”", "\""));
  }

  public MorfologikDutchSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikDutchSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getSpellingProhibitedWords());
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case LongSentenceRule.RULE_ID: return -1;
    }
    return 0;
  }
}
