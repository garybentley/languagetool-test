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
package org.languagetool.rules.nl;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Map;

import org.languagetool.rules.*;
import org.languagetool.rules.patterns.CaseConverter;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @since 2.7
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String DUTCH_SIMPLE_REPLACE_RULE = "NL_SIMPLE_REPLACE";

  public SimpleReplaceRule(ResourceBundle messages, List<Map<String, String>> wrongWords, CaseConverter caseCon) {
    super(messages, wrongWords, caseCon);
    setLocQualityIssueType(ITSIssueType.Misspelling);
    setCategory(new Category(new CategoryId("VERGISSINGEN"), "Vergissingen"));
    addExamplePair(Example.wrong("<marker>ofzo</marker>."),
                   Example.fixed("<marker>of zo</marker>."));
  }
/*
 GTODO Clean up
  @Override
  public String getFileName() {
    return "/nl/replace.txt";
  }
*/
  @Override
  public final String getId() {
    return DUTCH_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Snelle correctie van veel voorkomende vergissingen";
  }

  @Override
  public String getShort() {
    return "Vergissing";
  }

  @Override
  public String getSuggestion() {
    return " zou fout kunnen zijn. Misschien bedoelt u: ";
  }

  @Override
  public String getSuggestionsSeparator() {
    return ", ";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

}
