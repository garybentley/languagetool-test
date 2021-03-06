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
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Locale;

import org.languagetool.UserConfig;
import org.languagetool.Language;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.LongSentenceRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
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
import org.languagetool.rules.ca.SimpleReplaceBalearicRule;
import org.languagetool.rules.ca.SimpleReplaceDNVColloquialRule;
import org.languagetool.rules.ca.SimpleReplaceDNVRule;
import org.languagetool.rules.ca.SimpleReplaceDNVSecondaryRule;
import org.languagetool.rules.ca.SimpleReplaceDiacriticsIEC;
import org.languagetool.rules.ca.SimpleReplaceDiacriticsTraditional;
import org.languagetool.rules.ca.SimpleReplaceRule;
import org.languagetool.rules.ca.SimpleReplaceVerbsRule;

public class ValencianCatalan extends Catalan {

    public static final String VARIANT_ID = "valencia";
    public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID, VARIANT_ID);

    @Override
    public Locale getLocale() {
        return LOCALE;
    }

    @Override
    public Language getDefaultLanguageVariant() {
        return null;
    }

    @Override
    public boolean isVariant() {
        return true;
    }

  @Override
  public String getName() {
    return "Catalan (Valencian)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"ES"};
  }

  @Override
  public String getVariant() {
    return "valencia";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, altLanguages));
      /*
      GTODO CLean up, all these rules are in our super call
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                Example.wrong("A parer seu<marker> ,</marker> no era veritat."),
                Example.fixed("A parer seu<marker>,</marker> no era veritat.")),
            new DoublePunctuationRule(messages),
            new CatalanUnpairedBracketsRule(messages, this),
            new UppercaseSentenceStartRule(messages, this,
                Example.wrong("Preus de venda al públic. <marker>han</marker> pujat molt."),
                Example.fixed("Preus de venda al públic. <marker>Han</marker> pujat molt.")),
            new MultipleWhitespaceRule(messages, this),
            new LongSentenceRule(messages, userConfig),
            // specific to Catalan:
            new CatalanWordRepeatRule(messages, this),
            new MorfologikCatalanSpellerRule(messages, this, userConfig),
            new CatalanUnpairedQuestionMarksRule(messages, this),
            new CatalanUnpairedExclamationMarksRule(messages, this),
            new AccentuationCheckRule(messages),
            new ComplexAdjectiveConcordanceRule(messages),
            new CatalanWrongWordInContextRule(messages, getUseDataBroker()),
            new CatalanWrongWordInContextDiacriticsRule(messages, getUseDataBroker ()),
            new ReflexiveVerbsRule(messages),
            new SimpleReplaceVerbsRule(messages, this),
            new SimpleReplaceBalearicRule(messages, getUseDataBroker()),
            new SimpleReplaceRule(messages, getUseDataBroker()),
            new ReplaceOperationNamesRule(messages, this),
            // Valencian DNV
            new SimpleReplaceDNVRule(messages, this),
            new SimpleReplaceDiacriticsIEC(messages, getUseDataBroker()),
            new SimpleReplaceDiacriticsTraditional(messages, getUseDataBroker()),
            */
            /*
GTODO - need to sort missing file issues before Catalan can be used
Also, a number of classes are called "Catalan[X]" but actually use valencia type resources but the default
variant (in Catalan) is Catalan.
*/
            // This rule uses CatalanTagger which needs to know whether the language is a variant...
            // DEALT WITH rules.add(new SimpleReplaceVerbsRule(messages, this));

            rules.add(createDNVColloquialRule(messages));
            rules.add(createDNVSecondaryRule(messages));
    return rules;
  }

  public SimpleReplaceDNVSecondaryRule createDNVSecondaryRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceDNVSecondaryRule(getUseMessages(messages), getUseDataBroker().getDNVSecondaryWrongWords(), getUseDataBroker().getSynthesizer(), getUseDataBroker().getCaseConverter());
  }

  public SimpleReplaceDNVColloquialRule createDNVColloquialRule(ResourceBundle messages) throws Exception {
      return new SimpleReplaceDNVColloquialRule(getUseMessages(messages), getUseDataBroker().getDNVColloquialWrongWords(), getUseDataBroker().getSynthesizer(), getUseDataBroker().getCaseConverter());
  }

  @Override
  public List<String> getDefaultEnabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_VALENCIANS",
        "EXIGEIX_ACCENTUACIO_VALENCIANA", "EXIGEIX_POSSESSIUS_U",
        "EXIGEIX_VERBS_EIX", "EXIGEIX_VERBS_ISC", "PER_PER_A_INFINITIU");
    return Collections.unmodifiableList(rules);
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    List<String> rules = Arrays.asList("EXIGEIX_VERBS_CENTRAL",
        "EXIGEIX_ACCENTUACIO_GENERAL", "EXIGEIX_POSSESSIUS_V",
        "EVITA_PRONOMS_VALENCIANS", "EVITA_DEMOSTRATIUS_EIXE", "VOCABULARI_VALENCIA",
        "EXIGEIX_US");
    return Collections.unmodifiableList(rules);
  }

}
