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
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.patterns.CaseConverter;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead.
 *
 *
 * @author Jaume Ortolà
 */
public class SimpleReplaceBalearicRule extends AbstractSimpleReplaceRule {

/*
GTODO
  @Override
  protected Map<String, List<String>> getWrongWords() {
      if (wrongWords == null) {
          wrongWords = load("/ca/replace_balearic.txt", dataBroker);
      }
    return wrongWords;
  }
*/
  public SimpleReplaceBalearicRule(final ResourceBundle messages, Map<String, List<String>> wrongWords, CaseConverter caseCon) {
    super(messages, wrongWords, caseCon);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    this.setCheckLemmas(false);
    //this.setIgnoreTaggedWords();
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_BALEARIC";
  }

 @Override
  public String getDescription() {
    return "Suggeriments per a formes balears (autentic/autèntic)";
  }

  @Override
  public String getShort() {
    return "Possible error ortogràfic.";
  }

  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
    return "Possible error ortogràfic (forma verbal vàlida en la variant balear).";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

}
