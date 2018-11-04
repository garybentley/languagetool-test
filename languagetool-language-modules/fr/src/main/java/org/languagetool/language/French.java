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
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import morfologik.stemming.Dictionary;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.fr.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.fr.FrenchHybridDisambiguator;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.databroker.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.rules.spelling.hunspell.Hunspell;

public class French extends Language<FrenchResourceDataBroker> implements AutoCloseable {

    public static final String LANGUAGE_ID = "fr";
    public static final String COUNTRY_ID = "FR";

    public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

  @Override
  public FrenchResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public FrenchResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultFrenchResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override @Nullable
  public Language getDefaultLanguageVariant() {
    return null;
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
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public String getName() {
    return "French";
  }
/*
GTODO Clean up
  @Override
  public String getShortCode() {
    return "fr";
  }
*/
  @Override
  public String[] getCountries() {
    return new String[]{"FR", "", "BE", "CH", "CA", "LU", "MC", "CM",
            "CI", "HT", "ML", "SN", "CD", "MA", "RE"};
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
  public WordTokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        Contributors.DOMINIQUE_PELLE
    };
  }

  // GTODO Need to remove the
  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createGenericUnpairedBracketsRule(messages),
            // very fast, but no suggestions:
            //new HunspellNoSuggestionRule(messages, this, Example.wrong("Le <marker>chein</marker> noir"), Example.fixed("Le <marker>chien</marker> noir")),
            // slower than HunspellNoSuggestionRule but with suggestions:
            createSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createMultipleWhitespaceRule(messages),
            createSentenceWhitespaceRule(messages),
            // specific to French:
            createCompoundRule(messages),
            createQuestionWhitespaceRule(messages)
    );
  }

  public FrenchCompoundAwareHunspellRule createSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      Set<Dictionary> dicts = getUseDataBroker().getDictionaries(userConfig);
      MorfologikMultiSpeller speller = new MorfologikMultiSpeller(dicts, userConfig, 2);
      Hunspell.Dictionary hdic = getUseDataBroker().getHunspellDictionary();
      List ignoreWords = getUseDataBroker().getSpellingIgnoreWords();
      List prohibWords = getUseDataBroker().getSpellingProhibitedWords();
      return new FrenchCompoundAwareHunspellRule(getUseMessages(messages), this, speller, userConfig, hdic, ignoreWords, prohibWords);
  }

  public QuestionWhitespaceRule createQuestionWhitespaceRule(ResourceBundle messages) throws Exception {
      return new QuestionWhitespaceRule(getUseMessages(messages));
  }

  public CompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new CompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(messages, this);
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public GenericUnpairedBracketsRule createGenericUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
            Arrays.asList("[", "(", "{" /*"«", "‘"*/),
            Arrays.asList("]", ")", "}"
                /*"»", French dialog can contain multiple sentences. */
                /*"’" used in "d’arm" and many other words */));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
    return Arrays.<Rule>asList(
            createConfusionProbabilityRule(messages, languageModel)
    );
  }

  public FrenchConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new FrenchConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  /**
   * Closes the language model, if any.
   * @since 3.1
   */
  @Override
  public void close() throws Exception {
      getUseDataBroker().close();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
