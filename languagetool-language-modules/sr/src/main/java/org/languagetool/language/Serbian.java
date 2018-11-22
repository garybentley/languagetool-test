/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.*;
import org.languagetool.rules.sr.ekavian.MorfologikEkavianSpellerRule;
import org.languagetool.rules.sr.ekavian.SimpleGrammarEkavianReplaceRule;
import org.languagetool.rules.sr.ekavian.SimpleStyleEkavianReplaceRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sr.EkavianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.sr.SerbianHybridDisambiguator;
import org.languagetool.tagging.sr.EkavianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.databroker.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Support for Serbian language
 *
 * Attributes common to all Serbian dialects
 *
 * @author Zoltán Csala
 *
 * @since 4.0
 */
public abstract class Serbian extends Language<SerbianResourceDataBroker> {

  private static final Language SERBIA_SERBIAN = new SerbianSerbian();
/*
  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
*/
  public Serbian() {
  }

  public static final String LANGUAGE_ID = "sr";

  @Override
  public SerbianResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public String getName() {
    return "Serbian";
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return SERBIA_SERBIAN;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
            new Contributor("Золтан Чала (Csala, Zoltán)")
    };
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
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  // Rules common for all pronunciations of Serbian language
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
      createCommaWhitespaceRule(messages),
      createDoublePunctuationRule(messages),
      createUnpairedBracketsRule(messages),
      createUppercaseSentenceStartRule(messages),
      createMultipleWhitespaceRule(messages),
      createSentenceWhitespaceRule(messages),
      createWordRepeatRule(messages)
    );
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new CommaWhitespaceRule(getUseMessages(messages),
      Example.wrong("Није шија<marker> ,</marker> него врат."),
      Example.fixed("Није шија<marker>,</marker> него врат."));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
              Arrays.asList("[", "(", "{", "„", "„", "\""),
              Arrays.asList("]", ")", "}", "”", "“", "\""));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
          Example.wrong("Почела је школа. <marker>ђаци</marker> су поново сели у клупе."),
          Example.fixed("Почела је школа. <marker>Ђаци</marker> су поново сели у клупе."));
  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

/*
GTODO
  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    // Load all grammar*.xml files
    ResourceDataBroker dataBroker = getUseDataBroker();
    final String shortCode = getShortCode();
    final String dirBase = dataBroker.getRulesDir();

    for (final String ruleFile : RULE_FILES) {
      final String rulePath = shortCode + "/" + ruleFile;
      if (dataBroker.ruleFileExists(rulePath)) {
        ruleFileNames.add(dirBase + "/" + rulePath);
      }
    }
    return ruleFileNames;
  }
*/
}
