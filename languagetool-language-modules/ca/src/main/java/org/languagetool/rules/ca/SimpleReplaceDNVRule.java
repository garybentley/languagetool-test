/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.synthesis.Synthesizer;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches lemmas found only in DNV (AVL dictionary) and suggests
 * alternative words.
 *
 * Catalan implementations. Loads the
 * relevant lemmas from <code>rules/ca/replace_dnv.txt</code>.
 *
 * @author Jaume Ortolà
 */
public class SimpleReplaceDNVRule extends AbstractSimpleReplaceLemmasRule {

  public SimpleReplaceDNVRule(final ResourceBundle messages, Map<String, List<String>> wrongWords, Synthesizer synthesizer, CaseConverter caseCon) {
    super(messages, wrongWords, synthesizer, caseCon);
    super.setCategory(Categories.REGIONALISMS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Style);
    // GTODO wrongLemmas = load("/ca/replace_dnv.txt", language.getUseDataBroker());
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_DNV";
  }

 @Override
  public String getDescription() {
    return "Detecta paraules admeses només per l'AVL i proposa suggeriments de canvi";
  }

  @Override
  public String getShort() {
    return "Paraula admesa només pel DNV (AVL).";
  }

  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
	  return "Paraula admesa pel DNV (AVL), però no per altres diccionaris.";
  }

}
