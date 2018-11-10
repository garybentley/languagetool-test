/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.br.TopoReplaceRule;
import org.languagetool.rules.br.MorfologikBretonSpellerRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.br.BretonTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.databroker.*;

/**
 * @author Dominique Pelle
 */
public class Breton extends Language<BretonResourceDataBroker> {

  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private Disambiguator disambiguator;

  public static final String LANGUAGE_ID = "br";
  public static final String COUNTRY_ID = "FR";
  public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

    public BretonResourceDataBroker getDefaultDataBroker() throws Exception {
        return new DefaultBretonResourceDataBroker(this, getClass().getClassLoader());
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
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public String getName() {
    return "Breton";
  }

/*
 GTODO Clean up
  @Override
  public String getShortCode() {
    return "br";
  }
*/
  @Override
  public String[] getCountries() {
    return new String[] {"FR"};
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
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        Contributors.DOMINIQUE_PELLE, new Contributor("Fulup Jakez")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createMorfologikSpellerRule(messages, userConfig),
            createUppercaseSentenceStartRule(messages),
            createMultipleWhitespaceRule(messages),
            createSentenceWhitespaceRule(messages),
            createTopoReplaceRule(messages)
    );
  }

  public TopoReplaceRule createTopoReplaceRule(ResourceBundle messages) throws Exception {
      return new TopoReplaceRule(getUseMessages(messages), getUseDataBroker().getWrongTopographicalWords(), getUseDataBroker().getWordTokenizer(), getUseDataBroker().getCaseConverter());

  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this);
  }

  public DoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new DoublePunctuationRule(getUseMessages(messages));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages));
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages));
  }

  public MorfologikBretonSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikBretonSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords());
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

}
