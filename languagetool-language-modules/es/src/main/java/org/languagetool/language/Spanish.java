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

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.es.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;

import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Spanish extends Language<SpanishResourceDataBroker> {

  public static final String LANGUAGE_ID = "es";
  public static final String COUNTRY_ID = "ES";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public SpanishResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public SpanishResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultSpanishResourceDataBroker(this, getClass().getClassLoader());
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
    return "Spanish";
  }

  @Override
  public String[] getCountries() {
    return new String[]{
            "ES", "", "MX", "GT", "CR", "PA", "DO",
            "VE", "PE", "AR", "EC", "CL", "UY", "PY",
            "BO", "SV", "HN", "NI", "PR", "US", "CU"
    };
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
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
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Juan Martorell", "http://languagetool-es.blogspot.com/")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUnpairedBracketsRule(messages),
            createMorfologikSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createWordRepeatRule(messages),
            createMultipleWhitespaceRule(messages),
            createWikipediaRule(messages)
    );
  }

  public MorfologikSpanishSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikSpanishSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords());
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
              Arrays.asList("[", "(", "{", "“", "«", "»", "¿", "¡"),
              Arrays.asList("]", ")", "}", "”", "»", "«", "?", "!"));
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

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
  }

  public SpanishWikipediaRule createWikipediaRule(ResourceBundle messages) throws Exception {
      return new SpanishWikipediaRule(getUseMessages(messages), getUseDataBroker().getWikipediaWords(), getUseDataBroker().getCaseConverter());
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
    return Arrays.asList(
            createConfusionProbabilityRule(messages, languageModel)
    );
  }

  public SpanishConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new SpanishConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
