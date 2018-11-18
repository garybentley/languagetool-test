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
import org.languagetool.rules.*;
import org.languagetool.rules.gl.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.gl.GalicianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Collections;
import java.util.Locale;

public class Galician extends Language<GalicianResourceDataBroker> {
/*
  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
*/
  public static final String LANGUAGE_ID = "gl";
  public static final String COUNTRY_ID = "ES";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public GalicianResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultGalicianResourceDataBroker(this, getClass().getClassLoader());
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
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public String getName() {
    return "Galician";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"};
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
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

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Susana Sotelo Docío"),
            new Contributor("Tiago F. Santos (4.0)", "https://github.com/TiagoSantos81")
    };
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
            createMultipleWhitespaceRule(messages),
            createLongSentenceRule(messages, userConfig),
            createLongParagraphRule(messages, userConfig),
            createSentenceWhitespaceRule(messages),
            createWhiteSpaceBeforeParagraphEndRule(messages),
            createWhiteSpaceAtBeginOfParagraphRule(messages),
            createEmptyLineRule(messages),
            createParagraphRepeatBeginningRule(messages),
            createPunctuationMarkAtParagraphEndRule(messages),
            // Specific to Galician:
            createReplaceRule(messages),
            createCastWordsRule(messages),
            createRedundancyRule(messages),
            createWordinessRule(messages),
            createBarbarismsRule(messages),
            createWikipediaRule(messages)
    );
  }

  public HunspellRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new HunspellRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getHunspellDictionary(), getUseDataBroker().getSpellingIgnoreWords(), Collections.emptyList(), null);
  }

  public CastWordsRule createCastWordsRule(ResourceBundle messages) throws Exception {
      return new CastWordsRule(getUseMessages(messages), getUseDataBroker().getCastWords(), getUseDataBroker().getCaseConverter());
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages),
          Example.wrong("Tomamos café<marker> ,</marker> queixo, bolachas e uvas."),
          Example.fixed("Tomamos café<marker>,</marker> queixo, bolachas e uvas."));
  }

  public PunctuationMarkAtParagraphEnd createPunctuationMarkAtParagraphEndRule(ResourceBundle messages) throws Exception {
      return new PunctuationMarkAtParagraphEnd(getUseMessages(messages), this);
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
              Arrays.asList("[", "(", "{", "“", "«", "»", "‘", "\"", "'"),
              Arrays.asList("]", ")", "}", "”", "»", "«", "’", "\"", "'"));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
                Example.wrong("Esta casa é vella. <marker>foi</marker> construida en 1950."),
                Example.fixed("Esta casa é vella. <marker>Foi</marker> construida en 1950."));
  }

  public GalicianRedundancyRule createRedundancyRule(ResourceBundle messages) throws Exception {
      return new GalicianRedundancyRule(getUseMessages(messages), getUseDataBroker().getRedundancyWords(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
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

  public ParagraphRepeatBeginningRule createParagraphRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new ParagraphRepeatBeginningRule(getUseMessages(messages), this);
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
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

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
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

  public GalicianWikipediaRule createWikipediaRule(ResourceBundle messages) throws Exception {
      return new GalicianWikipediaRule(getUseMessages(messages), getUseDataBroker().getWikipediaWords(), getUseDataBroker().getCaseConverter());
  }

  public GalicianBarbarismsRule createBarbarismsRule(ResourceBundle messages) throws Exception {
      return new GalicianBarbarismsRule(getUseMessages(messages), getUseDataBroker().getBarbarismsWords(), getUseDataBroker().getCaseConverter());
  }

  public GalicianWordinessRule createWordinessRule(ResourceBundle messages) throws Exception {
      return new GalicianWordinessRule(getUseMessages(messages), getUseDataBroker().getWordinessWords(), getUseDataBroker().getCaseConverter());
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      // case "FRAGMENT_TWO_ARTICLES":     return 50;
      case "DEGREE_MINUTES_SECONDS":    return 30;
      // case "INTERJECTIONS_PUNTUATION":  return 20;
      // case "CONFUSION_POR":             return 10;
      // case "HOMOPHONE_AS_CARD":         return  5;
      // case "TODOS_FOLLOWED_BY_NOUN_PLURAL":    return  3;
      // case "TODOS_FOLLOWED_BY_NOUN_SINGULAR":  return  2;
      case "UNPAIRED_BRACKETS":         return -5;
      // case "PROFANITY":                 return -6;
      case "GL_BARBARISM_REPLACE":      return -10;
      case "GL_SIMPLE_REPLACE":         return -11;
      case "GL_REDUNDANCY_REPLACE":     return -12;
      case "GL_WORDINESS_REPLACE":      return -13;
      // case "GL_CLICHE_REPLACE":         return -17;
      // case "CHILDISH_LANGUAGE":         return -25;
      // case "ARCHAISMS":                 return -26;
      // case "INFORMALITIES":             return -27;
      // case "PUFFERY":                   return -30;
      // case "BIASED_OPINION_WORDS":      return -31;
      // case "WEAK_WORDS":                return -32;
      // case "PT_AGREEMENT_REPLACE":      return -35;
      case "GL_WIKIPEDIA_COMMON_ERRORS":return -45;
      case "HUNSPELL_RULE":             return -50;
      // case "NO_VERB":                   return -52;
      // case "CRASE_CONFUSION":           return -55;
      // case "FINAL_STOPS":               return -75;
      // case "T-V_DISTINCTION":           return -100;
      // case "T-V_DISTINCTION_ALL":       return -101;
      case "REPEATED_WORDS":            return -210;
      case "REPEATED_WORDS_3X":         return -211;
      case "TOO_LONG_SENTENCE_20":      return -997;
      case "TOO_LONG_SENTENCE_25":      return -998;
      case "TOO_LONG_SENTENCE_30":      return -999;
      case "TOO_LONG_SENTENCE_35":      return -1000;
      case "TOO_LONG_SENTENCE_40":      return -1001;
      case "TOO_LONG_SENTENCE_45":      return -1002;
      case "TOO_LONG_SENTENCE_50":      return -1003;
      case "TOO_LONG_SENTENCE_60":      return -1004;
      // case "CACOPHONY":                 return -2000;
    }
    return 0;
  }
}
