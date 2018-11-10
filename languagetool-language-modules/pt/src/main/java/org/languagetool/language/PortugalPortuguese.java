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
import org.languagetool.rules.pt.PostReformPortugueseCompoundRule;
import org.languagetool.rules.pt.PostReformPortugueseDashRule;
import org.languagetool.rules.pt.PortugalPortugueseReplaceRule;
import org.languagetool.rules.pt.PortugueseAgreementReplaceRule;

public class PortugalPortuguese extends Portuguese {

  public static final String COUNTRY_ID = "PT";
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
  public String getName() {
    return "Portuguese (Portugal)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"PT"};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages, userConfig, altLanguages));
    // GTODO This is already added in the super call.  rules.add(new PostReformPortugueseCompoundRule(messages));
    rules.add(createPostReformDashRule(messages));
    rules.add(createPortugalReplaceRule(messages));
    rules.add(createAgreementReplaceRule(messages));
    return rules;
  }

  public PortugalPortugueseReplaceRule createPortugalReplaceRule(ResourceBundle messages) throws Exception {
      return new PortugalPortugueseReplaceRule(getUseMessages(messages), getUseDataBroker().getPortugalWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public PortugueseAgreementReplaceRule createAgreementReplaceRule(ResourceBundle messages) throws Exception {
      return new PortugueseAgreementReplaceRule(getUseMessages(messages), getUseDataBroker().getAgreementWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public PostReformPortugueseDashRule createPostReformDashRule(ResourceBundle messages) throws Exception {
      return new PostReformPortugueseDashRule(getUseDataBroker().getPostReformCompoundPatternRules(PostReformPortugueseDashRule.MESSAGE));
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "PT_COMPOUNDS_POST_REFORM":         return  1;
      case "PORTUGUESE_OLD_SPELLING_INTERNAL": return -9;
    }
    return 0;
  }
}
