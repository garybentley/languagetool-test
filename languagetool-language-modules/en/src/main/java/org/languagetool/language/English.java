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
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.EnglishChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.en.*;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.en.EnglishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.databroker.EnglishResourceDataBroker;
import org.languagetool.databroker.DefaultEnglishResourceDataBroker;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Support for English - use the sub classes {@link BritishEnglish}, {@link AmericanEnglish},
 * etc. if you need spell checking.
 * Make sure to call {@link #close()} after using this (currently only relevant if you make
 * use of {@link EnglishConfusionProbabilityRule}).
 */
public class English extends Language<EnglishResourceDataBroker> implements AutoCloseable {

  public static final String LANGUAGE_ID = "en";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID);

  private static final Language AMERICAN_ENGLISH = new AmericanEnglish();

  /**
   * @deprecated use {@link AmericanEnglish} or {@link BritishEnglish} etc. instead -
   *  they have rules for spell checking, this class doesn't (deprecated since 3.2)
   */
  @Deprecated
  public English() {
  }

  @Override
  public EnglishResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public EnglishResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultEnglishResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return AMERICAN_ENGLISH;
  }

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public String getName() {
    return "English";
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  /**
   * Get the compound rule data for English, we defer to the data broker.
   *
   * @return The compound rule data.
   */
  public CompoundRuleData getCompounds() throws Exception {
      return getUseDataBroker().getCompounds();
  }

  /**
   * @since 2.3
   */
  @Override
  public Chunker getChunker() throws Exception {
      return getUseDataBroker().getChunker();
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
  public WordTokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public LanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  @Override
  public Word2VecModel getWord2VecModel() throws Exception {
      return getUseDataBroker().getWord2VecModel();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Mike Unwalla"), Contributors.MARCIN_MILKOWSKI, Contributors.DANIEL_NABER };
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
        createUppercaseSentenceStartRule(messages),
        createMultipleWhitespaceRule(messages),
        createSentenceWhitespaceRule(messages),
        createWhiteSpaceBeforeParagraphEndRule(messages),
        createWhiteSpaceAtBeginOfParagraphRule(messages),
        createEmptyLineRule(messages),
        createLongSentenceRule(messages, userConfig),
        createLongParagraphRule(messages, userConfig),
        //new OpenNMTRule(),     // commented out because of #903
        createParagraphRepeatBeginningRule(messages),
        createPunctuationMarkAtParagraphEnd(messages),
        // specific to English:
        createUnpairedBracketsRule(messages),
        createWordRepeatRule(messages),
        createAvsAnRule(messages),
        createWordRepeatBeginningRule(messages),
        createCompoundRule(messages),
        createContractionSpellingRule(messages),
        createWrongWordInContextRule(messages),
        createDashRule(messages),
        createWordCoherencyRule(messages)
        // GTODO Add new readability rules (update to base)...
    );
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new CommaWhitespaceRule(messages,
              Example.wrong("We had coffee<marker> ,</marker> cheese and crackers and grapes."),
              Example.fixed("We had coffee<marker>,</marker> cheese and crackers and grapes."));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      // GTODO Shouldn't this be using items from the messages?
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
              Example.wrong("This house is old. <marker>it</marker> was built in 1950."),
              Example.fixed("This house is old. <marker>It</marker> was built in 1950."));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public WhiteSpaceBeforeParagraphEnd createWhiteSpaceBeforeParagraphEndRule(ResourceBundle messages) throws Exception {
      return new WhiteSpaceBeforeParagraphEnd(getUseMessages(messages), this);
  }

  public WhiteSpaceAtBeginOfParagraph createWhiteSpaceAtBeginOfParagraphRule(ResourceBundle messages) throws Exception {
      return new WhiteSpaceAtBeginOfParagraph(getUseMessages(messages));
  }

  public EmptyLineRule createEmptyLineRule(ResourceBundle messages) throws Exception {
      return new EmptyLineRule(getUseMessages(messages), this);
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

  public LongParagraphRule createLongParagraphRule(ResourceBundle messages, int maxWords) throws Exception {
      return new LongParagraphRule(getUseMessages(messages), this, maxWords);
  }

  public LongParagraphRule createLongParagraphRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      int confWords = -1;
      if (userConfig != null) {
         confWords = userConfig.getConfigValueByID(LongParagraphRule.getRuleConfiguration().getRuleId());
      }
      return createLongParagraphRule(messages, confWords);
  }

  public ParagraphRepeatBeginningRule createParagraphRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new ParagraphRepeatBeginningRule(getUseMessages(messages), this);
  }

  public PunctuationMarkAtParagraphEnd createPunctuationMarkAtParagraphEnd(ResourceBundle messages) throws Exception {
      return new PunctuationMarkAtParagraphEnd(getUseMessages(messages), this);
  }

  public EnglishUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new EnglishUnpairedBracketsRule(getUseMessages(messages), this);
  }

  public EnglishWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new EnglishWordRepeatRule(getUseMessages(messages));
  }

  public AvsAnRule createAvsAnRule(ResourceBundle messages) throws Exception {
      return new AvsAnRule(getUseMessages(messages), getUseDataBroker().getRequiresAWords(), getUseDataBroker().getRequiresANWords());
  }

  public EnglishWordRepeatBeginningRule createWordRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new EnglishWordRepeatBeginningRule(getUseMessages(messages));
  }

  public CompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new CompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public ContractionSpellingRule createContractionSpellingRule(ResourceBundle messages) throws Exception {
      return new ContractionSpellingRule(getUseMessages(messages), getUseDataBroker().getContractionWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public EnglishDashRule createDashRule(ResourceBundle messages) throws Exception {
      return new EnglishDashRule(getUseDataBroker().getCompoundPatternRules("A dash was used instead of a hyphen. Did you mean: "));
  }

  public EnglishWrongWordInContextRule createWrongWordInContextRule(ResourceBundle messages) throws Exception {
      return new EnglishWrongWordInContextRule(getUseMessages(messages), getUseDataBroker().getWrongWordsInContext());
  }

  public WordCoherencyRule createWordCoherencyRule(ResourceBundle messages) throws Exception {
      return new WordCoherencyRule(getUseMessages(messages), getUseDataBroker().getCoherencyMappings());
  }

  protected UnitConversionRuleImperial createUnitConversionRuleImperialRule(ResourceBundle messages) throws Exception {
      return new UnitConversionRuleImperial(getUseMessages(messages));
  }

  public EnglishConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new EnglishConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  public EnglishNgramProbabilityRule createNgramProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new EnglishNgramProbabilityRule(getUseMessages(messages), languageModel, this);
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
    return Arrays.<Rule>asList(
        createConfusionProbabilityRule(messages, languageModel),
        createNgramProbabilityRule(messages, languageModel)
    );
  }

  @Override
  public List<NeuralNetworkRule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception {
      return getUseDataBroker().createNeuralNetworkRules(messages, word2vecModel);
  }

  /**
   * Closes the language model, if any.
   * @since 2.7
   */
  @Override
  public void close() throws Exception {
      getUseDataBroker().close();
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "TWO_CONNECTED_MODAL_VERBS": return -5;
      case "CONFUSION_RULE":            return -10;
      case LongSentenceRule.RULE_ID:    return -997;
      case LongParagraphRule.RULE_ID:   return -998;
    }
    return 0;
  }
}
