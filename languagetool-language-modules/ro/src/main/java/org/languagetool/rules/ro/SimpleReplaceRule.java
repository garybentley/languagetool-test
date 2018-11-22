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
package org.languagetool.rules.ro;

import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.patterns.CaseConverter;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests correct ones instead.
 * Romanian implementations. Loads the list of words from
 * <code>/ro/replace.txt</code>.
 *
 * @author Ionuț Păduraru
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule2 {

  public static final String ROMANIAN_SIMPLE_REPLACE_RULE = "RO_SIMPLE_REPLACE";

  // GTODO private static final String FILE_NAME = "/ro/replace.txt";
  // GTODO private static final Locale RO_LOCALE = new Locale("ro");  // locale used on case-conversion

  public SimpleReplaceRule(ResourceBundle messages, List<Map<String, String>> wordinessWords, CaseConverter caseCon) throws Exception {
    super(messages, wordinessWords, caseCon);
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public final String getId() {
    return ROMANIAN_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Cuvinte sau grupuri de cuvinte incorecte sau ieșite din uz";
  }

  @Override
  public String getShort() {
    return "Cuvânt incorect sau ieșit din uz";
  }

  @Override
  public String getSuggestion() {
    return " este incorect sau ieșit din uz, folosiți ";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " sau ";
  }

}
