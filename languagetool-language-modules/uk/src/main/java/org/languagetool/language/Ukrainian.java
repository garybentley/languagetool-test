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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.uk.HiddenCharacterRule;
import org.languagetool.rules.uk.MissingHyphenRule;
import org.languagetool.rules.uk.MixedAlphabetsRule;
import org.languagetool.rules.uk.MorfologikUkrainianSpellerRule;
import org.languagetool.rules.uk.SimpleReplaceRule;
import org.languagetool.rules.uk.SimpleReplaceSoftRule;
import org.languagetool.rules.uk.SimpleReplaceRenamedRule;
import org.languagetool.rules.uk.TokenAgreementPrepNounRule;
import org.languagetool.rules.uk.TokenAgreementAdjNounRule;
import org.languagetool.rules.uk.TokenAgreementNounVerbRule;
import org.languagetool.rules.uk.UkrainianWordRepeatRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.*;

public class Ukrainian extends Language<UkranianResourceDataBroker> {

  private static final List<String> RULE_FILES = Arrays.asList(
      "grammar-spelling.xml",
      "grammar-grammar.xml",
      "grammar-barbarism.xml",
      "grammar-style.xml",
      "grammar-punctuation.xml"
      );
/*
  private Tagger tagger;
  private SRXSentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Synthesizer synthesizer;
  private Disambiguator disambiguator;
*/
  public Ukrainian() {
  }

  public static final String LANGUAGE_ID = "uk";
  public static final String COUNTRY_ID = "UA";

  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public UkranianResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public UkranianResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultUkranianResourceDataBroker(this, getClass().getClassLoader());
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
  public Pattern getIgnoredCharactersRegex() {
    return Pattern.compile("[\u00AD\u0301]");
  }

  @Override
  public String getName() {
    return "Ukrainian";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"UA"};
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
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Andriy Rysin"),
        new Contributor("Maksym Davydov")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
        createCommaWhitespaceRule(messages),

        // TODO: does not handle dot in abbreviations in the middle of the sentence, and also !.., ?..
        //            new UppercaseSentenceStartRule(messages),
        createMultipleWhitespaceRule(messages),
        createWordRepeatRule(messages),

        // TODO: does not handle !.. and ?..
        //            new DoublePunctuationRule(messages),
        createMorfologikSpellerRule(messages, userConfig),

        createMissingHyphenRule(messages),

        createTokenAgreementNounVerbRule(messages),
        createTokenAgreementAdjNounRule(messages),
        createTokenAgreementPrepNounRule(messages),

        createMixedAlphabetsRule(messages),

        createReplaceRule(messages),
        createReplaceSoftRule(messages),
        createReplaceRenamedRule(messages),

        createHiddenCharacterRule(messages)
    );
  }

  public TokenAgreementNounVerbRule createTokenAgreementNounVerbRule(ResourceBundle messages) throws Exception {
      return new TokenAgreementNounVerbRule(getUseMessages(messages), getUseDataBroker().getTokenAgreementNounVerbExceptionHelper());
  }

  public TokenAgreementPrepNounRule createTokenAgreementPrepNounRule(ResourceBundle messages) throws Exception {
      return new TokenAgreementPrepNounRule(getUseMessages(messages), getUseDataBroker().getCaseGovernmentHelper(), getUseDataBroker().getSynthesizer());
  }

  public TokenAgreementAdjNounRule createTokenAgreementAdjNounRule(ResourceBundle messages) throws Exception {
      return new TokenAgreementAdjNounRule(getUseMessages(messages), getUseDataBroker().getTokenAgreementAdjNounExceptionHelper(), getUseDataBroker().getSynthesizer());
  }

  public SimpleReplaceRenamedRule createReplaceRenamedRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRenamedRule(getUseMessages(messages), getUseDataBroker().getRenamedWrongWords());
  }

  public MissingHyphenRule createMissingHyphenRule(ResourceBundle messages) throws Exception {
      return new MissingHyphenRule(getUseMessages(messages), getUseDataBroker().getWordTagger(), getUseDataBroker().getDashPrefixes());
  }

  public MixedAlphabetsRule createMixedAlphabetsRule(ResourceBundle messages) throws Exception {
      return new MixedAlphabetsRule(getUseMessages(messages));
  }

  public HiddenCharacterRule createHiddenCharacterRule(ResourceBundle messages) throws Exception {
      return new HiddenCharacterRule(getUseMessages(messages));
  }

  public SimpleReplaceSoftRule createReplaceSoftRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceSoftRule(getUseMessages(messages), getUseDataBroker().getSoftWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public MorfologikUkrainianSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikUkrainianSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords());
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages),
              Example.wrong("Ми обідали борщем<marker> ,</marker> пловом і салатом."),
              Example.fixed("Ми обідали борщем<marker>,</marker> пловом і салатом"));
  }

  public UkrainianWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new UkrainianWordRepeatRule(getUseMessages(messages));
  }

/*
GTODO
  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    ResourceDataBroker dataBroker = getUseDataBroker();
    String dirBase = dataBroker.getRulesDir() + "/" + getShortCode() + "/";
    for (String ruleFile : RULE_FILES) {
      ruleFileNames.add(dirBase + ruleFile);
    }
    return ruleFileNames;
  }
*/
  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
