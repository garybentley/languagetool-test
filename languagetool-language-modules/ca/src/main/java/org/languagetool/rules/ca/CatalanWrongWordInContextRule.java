/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Markus Brenneis
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

import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.WrongWordInContextRule;
import org.languagetool.rules.ContextWords;

public class CatalanWrongWordInContextRule extends WrongWordInContextRule {

  public CatalanWrongWordInContextRule(final ResourceBundle messages, List<ContextWords> wrongWords) {
    super(messages, wrongWords);
    setLocQualityIssueType(ITSIssueType.Grammar);
    setMatchLemmmas();
  }

  @Override
  protected String getCategoryString() {
    return "Z) Confusions";
  }

  @Override
  public String getId() {
    return "CATALAN_WRONG_WORD_IN_CONTEXT";
  }

  @Override
  public String getDescription() {
    return "Confusió segons el context (infligir/infringir, etc.)";
  }
/*
GTODO
  @Override
  protected String getFilename() {
    return "/ca/wrongWordInContext.txt";
  }
*/
  @Override
  protected String getMessageString() {
    return "¿Volíeu dir <suggestion>$SUGGESTION</suggestion> en lloc de '$WRONGWORD'?";
  }

  @Override
  protected String getShortMessageString() {
    return "Possible confusió";
  }

  @Override
  protected String getLongMessageString() {
    return "¿Volíeu dir <suggestion>$SUGGESTION</suggestion> (= $EXPLANATION_SUGGESTION) en lloc de '$WRONGWORD' (= $EXPLANATION_WRONGWORD)?";
  }

}
