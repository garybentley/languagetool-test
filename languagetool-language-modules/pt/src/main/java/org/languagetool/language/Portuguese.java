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
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.pt.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;

import java.util.Locale;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Post-spelling-reform Portuguese.
 */
public class Portuguese extends Language<PortugueseResourceDataBroker> {

  private static final Language PORTUGAL_PORTUGUESE = new PortugalPortuguese();

  public static final String LANGUAGE_ID = "pt";
  public static final Locale LOCALE = new Locale(LANGUAGE_ID);

    public PortugueseResourceDataBroker getDefaultDataBroker() throws Exception {
        return new DefaultPortugueseResourceDataBroker(this, getClass().getClassLoader());
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
  public String getName() {
    return "Portuguese";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"", "CV", "GW", "MO", "ST", "TL"};
  }

  @Override
  public Language getDefaultLanguageVariant() {
      // GTODO Why is Portugal the default rather than Brazil?  What is this based on?
    return PORTUGAL_PORTUGUESE;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Marco A.G. Pinto", "http://www.marcoagpinto.com/"),
            new Contributor("Tiago F. Santos (3.6+)", "https://github.com/TiagoSantos81"),
            new Contributor("Matheus Poletto (pt-BR)", "https://github.com/MatheusPoletto")
    };
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  /**
   * @since 3.6
   */
  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  /**
   * @since 3.6
   */
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
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createUnpairedBracketsRule(messages),
            createSpellerRule(messages, userConfig),
            createLongSentenceRule(messages, userConfig),
            createLongParagraphRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createMultipleWhitespaceRule(messages),
            createSentenceWhitespaceRule(messages),
            createWhiteSpaceBeforeParagraphEndRule(messages),
            createWhiteSpaceAtBeginOfParagraphRule(messages),
            createEmptyLineRule(messages),
            createParagraphRepeatBeginningRule(messages),
            createPunctuationMarkAtParagraphEndRule(messages),
            //Specific to Portuguese:
            createPostReformCompoundRule(messages),
            createReplaceRule(messages),
            createBarbarismsRule(messages),
            createClicheRule(messages),
            createFillerWordsRule(messages, userConfig),
            createRedundancyRule(messages),
            createWordinessRule(messages),
            createWeaselWordsRule(messages),
            createWikipediaRule(messages),
            createWordRepeatRule(messages),
            createWordRepeatBeginningRule(messages),
            createAccentuationCheckRule(messages),
            createWrongWordInContextRule(messages),
            createWordCoherencyRule(messages),
            createUnitConversionRule(messages)
    );
  }

  public HunspellRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new HunspellRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getHunspellDictionary(), getUseDataBroker().getSpellingIgnoreWords(), Collections.emptyList(), null);
  }

  public PostReformPortugueseCompoundRule createPostReformCompoundRule(ResourceBundle messages) throws Exception {
      return new PostReformPortugueseCompoundRule(getUseMessages(messages), getUseDataBroker().getPostReformCompoundRuleData());
  }

  public PortugueseUnitConversionRule createUnitConversionRule(ResourceBundle messages) throws Exception {
      return new PortugueseUnitConversionRule(getUseMessages(messages));
  }

  public PortugueseWordCoherencyRule createWordCoherencyRule(ResourceBundle messages) throws Exception {
      return new PortugueseWordCoherencyRule(getUseMessages(messages), getUseDataBroker().getCoherencyMappings());
  }

  public PortugueseWrongWordInContextRule createWrongWordInContextRule(ResourceBundle messages) throws Exception {
      return new PortugueseWrongWordInContextRule(getUseMessages(messages), getUseDataBroker().getWrongWordsInContext());
  }

  public PortugueseAccentuationCheckRule createAccentuationCheckRule(ResourceBundle messages) throws Exception {
      return new PortugueseAccentuationCheckRule(getUseMessages(messages), getUseDataBroker().getVerbToNounAccentWords(), getUseDataBroker().getVerbToAdjectiveAccentWords());
  }

  public PortugueseBarbarismsRule createBarbarismsRule(ResourceBundle messages) throws Exception {
      return new PortugueseBarbarismsRule(getUseMessages(messages), getUseDataBroker().getBarbarismsWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseFillerWordsRule createFillerWordsRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new PortugueseFillerWordsRule(getUseMessages(messages), this, userConfig);
  }

  public PortugueseWikipediaRule createWikipediaRule(ResourceBundle messages) throws Exception {
      return new PortugueseWikipediaRule(getUseMessages(messages), getUseDataBroker().getWikipediaWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseClicheRule createClicheRule(ResourceBundle messages) throws Exception {
      return new PortugueseClicheRule(getUseMessages(messages), getUseDataBroker().getClicheWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseRedundancyRule createRedundancyRule(ResourceBundle messages) throws Exception {
      return new PortugueseRedundancyRule(getUseMessages(messages), getUseDataBroker().getRedundancyWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseWeaselWordsRule createWeaselWordsRule(ResourceBundle messages) throws Exception {
      return new PortugueseWeaselWordsRule(getUseMessages(messages), getUseDataBroker().getWeaselWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseWordinessRule createWordinessRule(ResourceBundle messages) throws Exception {
      return new PortugueseWordinessRule(getUseMessages(messages), getUseDataBroker().getWordinessWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new PortugueseReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages),
          Example.wrong("Tomamos café<marker> ,</marker> queijo, bolachas e uvas."),
          Example.fixed("Tomamos café<marker>,</marker> queijo, bolachas e uvas."));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
                Arrays.asList("[", "(", "{", "\"", "“" /*, "«", "'", "‘" */),
                Arrays.asList("]", ")", "}", "\"", "”" /*, "»", "'", "’" */));
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

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
          Example.wrong("Esta casa é velha. <marker>foi</marker> construida em 1950."),
          Example.fixed("Esta casa é velha. <marker>Foi</marker> construida em 1950."));

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

  public PunctuationMarkAtParagraphEnd createPunctuationMarkAtParagraphEndRule(ResourceBundle messages) throws Exception {
      return new PunctuationMarkAtParagraphEnd(getUseMessages(messages), this);
  }

  public ParagraphRepeatBeginningRule createParagraphRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new ParagraphRepeatBeginningRule(getUseMessages(messages), this);
  }

  public EmptyLineRule createEmptyLineRule(ResourceBundle messages) throws Exception {
      return new EmptyLineRule(getUseMessages(messages), this);
  }

  public PortugueseWordRepeatBeginningRule createWordRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new PortugueseWordRepeatBeginningRule(getUseMessages(messages));
  }

  public PortugueseWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new PortugueseWordRepeatRule(getUseMessages(messages));
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  /** @since 3.6 */
  @Override
  public synchronized LanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  /** @since 3.6 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.<Rule>asList(
            createConfusionProbabilityRule(messages, languageModel)
    );
  }

  public PortugueseConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new PortugueseConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  /** @since 4.0 */
  @Override
  public synchronized Word2VecModel getWord2VecModel() throws Exception {
      return getUseDataBroker().getWord2VecModel();
  }

  /** @since 4.0 */
  @Override
  public List<NeuralNetworkRule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception {
      return getUseDataBroker().createNeuralNetworkRules(messages, word2vecModel);
    // GTODO return NeuralNetworkRuleCreator.createRules(messages, this, word2vecModel);
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "FRAGMENT_TWO_ARTICLES":     return 50;
      case "DEGREE_MINUTES_SECONDS":    return 30;
      case "INTERJECTIONS_PUNTUATION":  return 20;
      case "CONFUSION_POR":             return 10;
      case "HOMOPHONE_AS_CARD":         return  5;
      case "TODOS_FOLLOWED_BY_NOUN_PLURAL":    return  3;
      case "TODOS_FOLLOWED_BY_NOUN_SINGULAR":  return  2;
      case "UNPAIRED_BRACKETS":         return -5;
      case "PROFANITY":                 return -6;
      case "PT_BARBARISMS_REPLACE":     return -10;
      case "PT_PT_SIMPLE_REPLACE":      return -11;
      case "PT_REDUNDANCY_REPLACE":     return -12;
      case "PT_WORDINESS_REPLACE":      return -13;
      case "PT_CLICHE_REPLACE":         return -17;
      case "CHILDISH_LANGUAGE":         return -25;
      case "ARCHAISMS":                 return -26;
      case "INFORMALITIES":             return -27;
      case "PUFFERY":                   return -30;
      case "BIASED_OPINION_WORDS":      return -31;
      case "WEAK_WORDS":                return -32;
      case "PT_AGREEMENT_REPLACE":      return -35;
      case "HUNSPELL_RULE":             return -50;
      case "NO_VERB":                   return -52;
      case "CRASE_CONFUSION":           return -55;
      case "FINAL_STOPS":               return -75;
      case "EU_NÓS_REMOVAL":            return -90;
      case "T-V_DISTINCTION":           return -100;
      case "T-V_DISTINCTION_ALL":       return -101;
      case "REPEATED_WORDS":            return -210;
      case "REPEATED_WORDS_3X":         return -211;
      case "PT_WIKIPEDIA_COMMON_ERRORS":return -500;
      case "FILLER_WORDS_PT":           return -990;
      case LongSentenceRule.RULE_ID:    return -997;
      case LongParagraphRule.RULE_ID:   return -998;
      case "CACOPHONY":                 return -1500;
      case "UNKNOWN_WORD":              return -2000;
    }
    return 0;
  }
}
