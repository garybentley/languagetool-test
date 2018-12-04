/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.UserConfig;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.de.LongSentenceRule;

import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Simple German (see e.g. <a href="https://de.wikipedia.org/wiki/Leichte_Sprache">Wikipedia</a>)
 * that only support rules specific to this variant, not the other German rules.
 */
public class SimpleGerman extends GermanyGerman {

    public static final Locale LOCALE = new Locale(German.LANGUAGE_ID, GermanyGerman.COUNTRY_ID, "x-simple-language");

  @Override
  public String getName() {
    return "Simple German";
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Annika Nietzio")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    List<Rule> rules = new ArrayList<>();
    LongSentenceRule lengthRule = createLongSentenceRule(messages, userConfig, 12, true);
    rules.add(lengthRule);
    return rules;
  }

  @Override
  public LanguageModel getLanguageModel() throws Exception {
    return null;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
    return Collections.emptyList();
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case LongSentenceRule.RULE_ID: return 10;
    }
    return super.getPriorityForId(id);
  }

}
