/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sr.ekavian;

import org.languagetool.rules.AbstractSimpleReplaceRule;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.rules.patterns.CaseConverter;

/**
 * A rule that matches words which should not be used and suggests correct ones instead.
 * Serbian implementations. Loads the list of words from
 * <code>/sr/ekavian/replace-grammar.txt</code>.
 *
 * @author Zoltan Csala
 *
 * @since 4.0
 */
public class SimpleGrammarEkavianReplaceRule extends AbstractSimpleReplaceRule {

  // GTODO private static final Map<String, List<String>> wrongWords = load("/sr/ekavian/replace-grammar.txt");

  public SimpleGrammarEkavianReplaceRule(ResourceBundle messages, Map<String, List<String>> wrongWords, CaseConverter caseCon) {
      super(messages, wrongWords, caseCon);
  }

  @Override
  public final String getId() {
    return "SR_EKAVIAN_SIMPLE_GRAMMAR_REPLACE_RULE";
  }

  @Override
  public String getDescription() {
    return "Провера граматички погрешних речи или израза";
  }

  @Override
  public String getShort() {
    return "Граматички погрешна реч тј. израз";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Не каже се „" + tokenStr + "“ него „" + String.join(", ", replacements) + "“.";
  }
}
