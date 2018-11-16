/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.UserConfig;
import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.pt.PreReformPortugueseCompoundRule;
import org.languagetool.rules.pt.PreReformPortugueseDashRule;

/**
 * @since 3.6
 */
public class MozambiquePortuguese extends Portuguese {

    public static final String COUNTRY_ID = "MZ";
    public static final Locale LOCALE = new Locale(Portuguese.LANGUAGE_ID, COUNTRY_ID);

    @Override
    public boolean isVariant() {
        return true;
    }

    @Override
    public Locale getLocale() {
        return LOCALE;
    }

  @Override
  public String[] getCountries() {
    return new String[]{"MZ"};
  }

  @Override
  public String getName() {
    return "Portuguese (Moçambique preAO)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, altLanguages));
    rules.add(createSpellerRule(messages, userConfig));
    rules.add(createPreReformCompoundRule(messages));
    rules.add(createPreReformDashRule(messages));
    return rules;
  }

  public PreReformPortugueseDashRule createPreReformDashRule(ResourceBundle messages) throws Exception {
      return new PreReformPortugueseDashRule(getUseDataBroker().getPreReformCompoundPatternRules(PreReformPortugueseDashRule.MESSAGE));
  }

  public PreReformPortugueseCompoundRule createPreReformCompoundRule(ResourceBundle messages) throws Exception {
      return new PreReformPortugueseCompoundRule(getUseMessages(messages), getUseDataBroker().getPreReformCompoundRuleData());
  }

}
