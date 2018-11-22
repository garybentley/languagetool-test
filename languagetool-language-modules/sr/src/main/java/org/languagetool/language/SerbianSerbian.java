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
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.databroker.*;
import org.languagetool.rules.sr.ekavian.*;

/**
 * Support for Serbian language spoken in Serbia
 *
 * @author Zoltán Csala
 *
 * @since 4.0
 */
public class SerbianSerbian extends Serbian {

    public static final String COUNTRY_ID = "RS";

    public static final Locale LOCALE = new Locale(LANGUAGE_ID, COUNTRY_ID);

    @Override
    public Locale getLocale() {
        return LOCALE;
    }

    @Override
    public boolean isVariant() {
        return true;
    }

    @Override
    public SerbianResourceDataBroker getDefaultDataBroker() throws Exception {
        return new DefaultSerbianSerbianResourceDataBroker(this, getClass().getClassLoader());
    }

  @Override
  public String[] getCountries() {
    return new String[]{"RS"};
  }

  @Override
  public String getName() {
    return "Serbian (Serbia)";
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, altLanguages));
    messages = getUseMessages(messages);
    rules.add(createMorfologikSpellerRule(messages, userConfig));
    rules.add(createGrammarReplaceRule(messages));
    rules.add(createStyleReplaceRule(messages));
    return rules;
  }

  public MorfologikEkavianSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikEkavianSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getSpellingProhibitedWords());
  }

  public SimpleGrammarEkavianReplaceRule createGrammarReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleGrammarEkavianReplaceRule(getUseMessages(messages), getUseDataBroker().getGrammarWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public SimpleStyleEkavianReplaceRule createStyleReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleStyleEkavianReplaceRule(getUseMessages(messages), getUseDataBroker().getStyleWrongWords(), getUseDataBroker().getCaseConverter());
  }

}
