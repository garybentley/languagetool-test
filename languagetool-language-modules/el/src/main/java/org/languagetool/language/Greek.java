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

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.el.MorfologikGreekSpellerRule;
import org.languagetool.rules.el.NumeralStressRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;

/**
 *
 * @author Panagiotis Minos (pminos@gmail.com)
 */
public class Greek extends Language<GreekResourceDataBroker> {

  public static final String LANGUAGE_ID = "el";
  public static final String COUNTRY_ID = "GR";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public GreekResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultGreekResourceDataBroker(this, getClass().getClassLoader());
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
    return "Greek";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"GR"};
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
            new Contributor("Panagiotis Minos")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createLongSentenceRule(messages, userConfig),
            createMorfologikSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createMultipleWhitespaceRule(messages),
            createWordRepeatBeginningRule(messages),
            createWordRepeatRule(messages),
            createNumeralStressRule(messages)
    );
  }

  public MorfologikGreekSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikGreekSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig),
                      getUseDataBroker().getSpellingIgnoreWords());
  }

  public NumeralStressRule createNumeralStressRule(ResourceBundle messages) throws Exception {
      return new NumeralStressRule(getUseMessages(messages));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule("EL_UNPAIRED_BRACKETS", getUseMessages(messages),
              Arrays.asList("[", "(", "{", "“", "\"", "«"),
              Arrays.asList("]", ")", "}", "”", "\"", "»"));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new CommaWhitespaceRule(getUseMessages(messages),
          Example.wrong("Το κόμμα χωρίζει προτάσεις<marker> ,</marker> όρους προτάσεων και φράσεις."),
          Example.fixed("Το κόμμα χωρίζει προτάσεις<marker>,</marker> όρους προτάσεων και φράσεις."));
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      int confWords = -1;
      if (userConfig != null) {
        confWords = userConfig.getConfigValueByID(LongSentenceRule.getRuleConfiguration().getRuleId());
      }
      return createLongSentenceRule(getUseMessages(messages), confWords);
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, int maxWords) throws Exception {
      return new LongSentenceRule(getUseMessages(messages), maxWords);
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
          Example.wrong("Η τελεία είναι σημείο στίξης. <marker>δείχνει</marker> το τέλος μίας πρότασης."),
          Example.fixed("Η τελεία είναι σημείο στίξης. <marker>Δείχνει</marker> το τέλος μίας πρότασης."));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public WordRepeatBeginningRule createWordRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new WordRepeatBeginningRule(getUseMessages(messages));
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
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
  public Synthesizer getSynthesizer() throws Exception {
      return getUseDataBroker().getSynthesizer();
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
