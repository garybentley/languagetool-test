/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.en.MorfologikCanadianSpellerRule;
import org.languagetool.rules.en.UnitConversionRuleImperial;

public class CanadianEnglish extends English {

    public static final String COUNTRY_ID = "CA";

    public static final Locale LOCALE = new Locale(English.LOCALE.getLanguage(), COUNTRY_ID);

    @Override
    public Locale getLocale() {
        return LOCALE;
    }

  @Override
  public String[] getCountries() {
    return new String[]{COUNTRY_ID};
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
    return "English (Canadian)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, altLanguages));
    rules.add(createMorfologikSpellerRule(messages, userConfig));
    rules.add(createUnitConversionRuleImperialRule(messages));
    return rules;
  }

  @Override
  public UnitConversionRuleImperial createUnitConversionRuleImperialRule(ResourceBundle messages) throws Exception {
      return super.createUnitConversionRuleImperialRule(messages);
  }

  public MorfologikCanadianSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikCanadianSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig),
                      getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getSpellingProhibitedWords(), getUseDataBroker().getSynthesizer());
  }

}
