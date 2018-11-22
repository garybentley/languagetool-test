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

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.sr.jekavian.MorfologikJekavianSpellerRule;
import org.languagetool.rules.sr.jekavian.SimpleGrammarJekavianReplaceRule;
import org.languagetool.rules.sr.jekavian.SimpleStyleJekavianReplaceRule;
import org.languagetool.databroker.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Class modelling Serbian Jekavian dialect
 *
 * @since 4.0
 */
public abstract class JekavianSerbian extends Serbian {

    @Override
    public boolean isVariant() {
        return true;
    }

  @Override
  public SerbianResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultJekavianSerbianResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>(super.getRelevantRules(messages, userConfig, altLanguages));
    messages = getUseMessages(messages);
    // Rules specific for Jekavian Serbian
    rules.add(createMorfologikSpellerRule(messages, userConfig));
    rules.add(createGrammarReplaceRule(messages));
    rules.add(createStyleReplaceRule(messages));
    return rules;
  }

  public MorfologikJekavianSpellerRule createMorfologikSpellerRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new MorfologikJekavianSpellerRule(getUseMessages(messages), this, userConfig, getUseDataBroker().getDictionaries(userConfig), getUseDataBroker().getSpellingIgnoreWords(), getUseDataBroker().getSpellingProhibitedWords());
  }

  public SimpleGrammarJekavianReplaceRule createGrammarReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleGrammarJekavianReplaceRule(getUseMessages(messages), getUseDataBroker().getGrammarWrongWords(), getUseDataBroker().getCaseConverter());
  }

  public SimpleStyleJekavianReplaceRule createStyleReplaceRule(ResourceBundle messages) throws Exception {
      return new SimpleStyleJekavianReplaceRule(getUseMessages(messages), getUseDataBroker().getStyleWrongWords(), getUseDataBroker().getCaseConverter());
  }

}
