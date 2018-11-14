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
import java.util.regex.Pattern;

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ru.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ru.RussianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ru.RussianHybridDisambiguator;
import org.languagetool.tagging.ru.RussianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.databroker.*;

public class Russian extends Language<RussianResourceDataBroker> {

    public static final String LANGUAGE_ID = "ru";
    public static final String COUNTRY_ID = "RU";

    public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

    @Override
    public RussianResourceDataBroker getUseDataBroker() throws Exception {
        return super.getUseDataBroker();
    }

    @Override
    public RussianResourceDataBroker getDefaultDataBroker() throws Exception {
        return new DefaultRussianResourceDataBroker(this, getClass().getClassLoader());
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
  public Pattern getIgnoredCharactersRegex() {
    return Pattern.compile("[\u00AD\u0301\u0300]");
  }

  @Override
  public String getName() {
    return "Russian";
  }
/*
 GTODO
  @Override
  public String getShortCode() {
    return "ru";
  }
*/
  @Override
  public String[] getCountries() {
    return new String[] {"RU"};
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
            new Contributor("Yakov Reztsov", "http://myooo.ru/content/view/83/43/")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createUppercaseSentenceStartRule(messages),
            createMorfologikSpellerRule(messages, userConfig),
            createWordRepeatRule(messages),
            createMultipleWhitespaceRule(messages),
            // specific to Russian :
            createUnpairedBracketsRule(messages),
            createCompoundRule(messages),
            createReplaceRule(messages),
            createWordCoherencyRule(messages),
            createRussianWordRepeatRule(messages),
            createVerbConjugationRule(messages)
            // GTODO Removed since it causes an out of heap space error -> createDashRule(messages)
    );
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages),
              Example.wrong("Не род<marker> ,</marker> а ум поставлю в воеводы."),
              Example.fixed("Не род<marker>,</marker> а ум поставлю в воеводы."));
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
              Example.wrong("Закончилось лето. <marker>дети</marker> снова сели за школьные парты."),
              Example.fixed("Закончилось лето. <marker>Дети</marker> снова сели за школьные парты."));
  }

  public RussianUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new RussianUnpairedBracketsRule(getUseMessages(messages));
  }

  public RussianVerbConjugationRule createVerbConjugationRule(ResourceBundle messages) throws Exception {
      return new RussianVerbConjugationRule(getUseMessages(messages));
  }

  public MorfologikRussianSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikRussianSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig),
                      getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getSpellingProhibitedWords());
  }

  public RussianDashRule createDashRule(ResourceBundle messages) throws Exception {
      return new RussianDashRule(getUseDataBroker().getCompoundPatternRules(RussianDashRule.MESSAGE));
  }

  public RussianWordCoherencyRule createWordCoherencyRule(ResourceBundle messages) throws Exception {
      return new RussianWordCoherencyRule(getUseMessages(messages), getUseDataBroker().getCoherencyMappings());
  }

  public RussianCompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new RussianCompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public WordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new WordRepeatRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public RussianWordRepeatRule createRussianWordRepeatRule(ResourceBundle messages) throws Exception {
      return new RussianWordRepeatRule(getUseMessages(messages));
  }

  public RussianSimpleReplaceRule createReplaceRule(ResourceBundle messages) throws Exception {
      return new RussianSimpleReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongWords(), getUseDataBroker().getCaseConverter());
  }

  /** @since 3.1 */
  @Override
  public LanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
    return Arrays.<Rule>asList(
            createConfusionProbabilityRule(messages, languageModel)
    );
  }

  public RussianConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new RussianConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  /** @since 3.3 */
  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }
}
