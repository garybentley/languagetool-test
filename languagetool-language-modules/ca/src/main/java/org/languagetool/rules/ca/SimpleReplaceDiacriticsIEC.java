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

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.patterns.CaseConverter;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Dicritics with IEC rules.
 *
 * Catalan implementations. Loads the
 * relevant word forms from <code>rules/ca/replace_diacritics_iec.txt</code>.
 *
 * @author Jaume Ortolà
 */
public class SimpleReplaceDiacriticsIEC extends AbstractSimpleReplaceRule {

/*
GTODO
  @Override
  protected Map<String, List<String>> getWrongWords() {
      if (wrongWords == null) {
           wrongWords = load("/ca/replace_diacritics_iec.txt", dataBroker);
      }
    return wrongWords;
  }
*/
  public SimpleReplaceDiacriticsIEC(final ResourceBundle messages, Map<String, List<String>> wrongWords, CaseConverter caseCon) {
    super(messages, wrongWords, caseCon);
    super.setCategory(new Category(new CategoryId("DIACRITICS_IEC"), "Z) Accents diacrítics segons l'IEC"));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    super.setDefaultOff();
    this.setCheckLemmas(false);
  }

  @Override
  public final String getId() {
    return "CA_SIMPLEREPLACE_DIACRITICS_IEC";
  }

 @Override
  public String getDescription() {
    return "Diacrítics eliminats per l'IEC.";
  }

  @Override
  public String getShort() {
    return "Sobra l'accent (IEC 2017)";
  }

  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Sobra l'accent diacrític (IEC 2017).";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

}
