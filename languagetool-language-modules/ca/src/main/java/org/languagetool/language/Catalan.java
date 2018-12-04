/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.ca.AccentuationCheckRule;
import org.languagetool.rules.ca.CatalanUnpairedBracketsRule;
import org.languagetool.rules.ca.CatalanUnpairedExclamationMarksRule;
import org.languagetool.rules.ca.CatalanUnpairedQuestionMarksRule;
import org.languagetool.rules.ca.CatalanWordRepeatRule;
import org.languagetool.rules.ca.CatalanWrongWordInContextDiacriticsRule;
import org.languagetool.rules.ca.CatalanWrongWordInContextRule;
import org.languagetool.rules.ca.ComplexAdjectiveConcordanceRule;
import org.languagetool.rules.ca.MorfologikCatalanSpellerRule;
import org.languagetool.rules.ca.ReflexiveVerbsRule;
import org.languagetool.rules.ca.ReplaceOperationNamesRule;
import org.languagetool.rules.ca.SimpleReplaceRule;
import org.languagetool.rules.ca.SimpleReplaceBalearicRule;
import org.languagetool.rules.ca.SimpleReplaceDNVRule;
import org.languagetool.rules.ca.SimpleReplaceDiacriticsIEC;
import org.languagetool.rules.ca.SimpleReplaceDiacriticsTraditional;
import org.languagetool.rules.ca.SimpleReplaceVerbsRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ca.CatalanHybridDisambiguator;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.ca.CatalanWordTokenizer;
import org.languagetool.databroker.*;

public class Catalan extends Language<CatalanResourceDataBroker> {

    public static final String LANGUAGE_ID = "ca";
    public static final String COUNTRY_ID = "ES";
    public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  private static final Language DEFAULT_CATALAN = new Catalan();
/*
  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
*/
  public CatalanResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultCatalanResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public Language getDefaultLanguageVariant() {
      return null;
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
    return "Catalan";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"}; // "AD", "FR", "IT"
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Ricard Roca"), new Contributor("Jaume Ortolà") };
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
            createLongSentenceRule(messages, userConfig),
            // specific to Catalan:
            createWordRepeatRule(messages),
            // GTODO Removed for now until missing file issues are resolved createMorfologikSpellerRule(messages, userConfig),
            createUnpairedQuestionMarksRule(messages),
            createUnpairedExclamationMarksRule(messages),
            createAccentuationCheckRule(messages),
            createComplexAdjectiveConcordanceRule(messages),
            createWrongWordInContextRule(messages),
            createWrongWordInContextDiacriticsRule(messages),
            createReflexiveVerbsRule(messages),
            createVerbsRule(messages),
            createBalearicRule(messages),
            createReplaceRule(messages),
            createOperationNamesRule(messages),
            createDNVRule(messages), // can be removed here after updating dictionaries
            createDiacriticsIECRule(messages),
            createDiacriticsTraditionalRule(messages)
    );
  }

  public CatalanUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new CatalanUnpairedBracketsRule(getUseMessages(messages));
  }

  public MorfologikCatalanSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikCatalanSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig),
                      getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getTagger());
  }

  public CatalanWrongWordInContextRule createWrongWordInContextRule(ResourceBundle messages) throws Exception {
      return new CatalanWrongWordInContextRule(messages, getUseDataBroker().getWrongWordsInContext());
  }

  public CatalanWrongWordInContextDiacriticsRule createWrongWordInContextDiacriticsRule(ResourceBundle messages) throws Exception {
      return new CatalanWrongWordInContextDiacriticsRule(messages, getUseDataBroker().getDiacriticWrongWordsInContext());
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages),
          Example.wrong("A parer seu<marker> ,</marker> no era veritat."),
          Example.fixed("A parer seu<marker>,</marker> no era veritat."));
    }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
          Example.wrong("Preus de venda al públic. <marker>han</marker> pujat molt."),
          Example.fixed("Preus de venda al públic. <marker>Han</marker> pujat molt."));
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

  public ComplexAdjectiveConcordanceRule createComplexAdjectiveConcordanceRule(ResourceBundle messages) throws Exception {
      return new ComplexAdjectiveConcordanceRule(getUseMessages(messages));
  }

  public AccentuationCheckRule createAccentuationCheckRule(ResourceBundle messages) throws Exception {
      return new AccentuationCheckRule(getUseMessages(messages), getUseDataBroker().getAccentuationRelevantWords1(), getUseDataBroker().getAccentuationRelevantWords2());
  }

  public CatalanUnpairedExclamationMarksRule createUnpairedExclamationMarksRule(ResourceBundle messages) throws Exception {
      return new CatalanUnpairedExclamationMarksRule(getUseMessages(messages));
  }

  public CatalanUnpairedQuestionMarksRule createUnpairedQuestionMarksRule(ResourceBundle messages) throws Exception {
      return new CatalanUnpairedQuestionMarksRule(getUseMessages(messages));
  }

  public CatalanWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new CatalanWordRepeatRule(getUseMessages(messages));
  }

  public ReflexiveVerbsRule createReflexiveVerbsRule(ResourceBundle messages) throws Exception {
      return new ReflexiveVerbsRule(getUseMessages(messages), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceDiacriticsTraditional createDiacriticsTraditionalRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceDiacriticsTraditional(getUseMessages(messages), getUseDataBroker().getDiactriticsTraditionalWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public SimpleReplaceBalearicRule createBalearicRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceBalearicRule(getUseMessages(messages), getUseDataBroker().getBalearicWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceDNVRule createDNVRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceDNVRule(getUseMessages(messages), getUseDataBroker().getDNVWrongWords(), getUseDataBroker().getSynthesizer(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceVerbsRule createVerbsRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceVerbsRule(getUseMessages(messages), getUseDataBroker().getVerbsWrongWords(),
                getUseDataBroker().getTagger(), getUseDataBroker().getSynthesizer(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceDiacriticsIEC createDiacriticsIECRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceDiacriticsIEC(getUseMessages(messages), getUseDataBroker().getDiactriticsIECWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public ReplaceOperationNamesRule createOperationNamesRule(ResourceBundle messages) throws Exception {
      messages = getUseMessages(messages);
      return new ReplaceOperationNamesRule(messages, getUseDataBroker().getOperationNameWrongWords(), getUseDataBroker().getSynthesizer(), getUseDataBroker().getCaseConverter());
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
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  @Override
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "CA_SIMPLE_REPLACE_BALEARIC": return 100;
      case "CONFUSIONS_ACCENT": return 20;
      case "DIACRITICS": return 20;
      case "ACCENTUATION_CHECK": return 10;
      case "CONCORDANCES_DET_NOM": return 5;
      case "REGIONAL_VERBS": return -10;
      case "FALTA_ELEMENT_ENTRE_VERBS": return -10;
      case "FALTA_COMA_FRASE_CONDICIONAL": return -20;
      case "SUBSTANTIUS_JUNTS": return -25;
      case "MUNDAR": return -50;
      case "MORFOLOGIK_RULE_CA_ES": return -100;
      case "NOMBRES_ROMANS": return -400;
      case "UPPERCASE_SENTENCE_START": return -500;
    }
    return 0;
  }
}
