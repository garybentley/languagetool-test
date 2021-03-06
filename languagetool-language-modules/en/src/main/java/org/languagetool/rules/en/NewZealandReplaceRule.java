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
package org.languagetool.rules.en;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Marcin Miłkowski
 */
public class NewZealandReplaceRule extends AbstractSimpleReplaceRule {

  public static final String NEW_ZEALAND_SIMPLE_REPLACE_RULE = "EN_NZ_SIMPLE_REPLACE";

  //private Map<String, List<String>> wrongWords;
  //private static final Locale EN_NZ_LOCALE = new Locale("en-NZ");

/*
GTODO: Clean up.
  @Override
  protected Map<String, List<String>> getWrongWords() {
      return dataBroker.getWrongWords();

      GTODO: Clean up
      if (wrongWords == null) {
          wrongWords = load("/en/en-NZ/replace.txt", dataBroker);
      }
    return wrongWords;

  }
*/
  public NewZealandReplaceRule(ResourceBundle messages, Map<String, List<String>> wrongWords, CaseConverter caseCon) {
    super(messages, wrongWords, caseCon);
    setLocQualityIssueType(ITSIssueType.LocaleViolation);
    addExamplePair(Example.wrong("A <marker>sidewalk</marker> is a path along the side of a road."),
                   Example.fixed("A <marker>footpath</marker> is a path along the side of a road."));
  }

  @Override
  public final String getId() {
    return NEW_ZEALAND_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "English words easily confused in New Zealand English";
  }

  @Override
  public String getShort() {
    return "Not a New Zealand English word";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " is a non-standard expression, in New Zealand English it is more common to use: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

}
