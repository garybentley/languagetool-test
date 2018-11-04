/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.junit.Before;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;

public class DuUpperLowerCaseRuleTest {

  private DuUpperLowerCaseRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
      German lang = new German();
      rule = lang.createDuUpperLowerCaseRule(null);
      lt = new JLanguageTool(lang);
  }

  @Test
  public void testRule() throws Exception {
    assertErrors("Du bist noch jung.", 0);
    assertErrors("Du bist noch jung, du bist noch fit.", 0);
    assertErrors("Aber du bist noch jung, du bist noch fit.", 0);
    assertErrors("Aber du bist noch jung, dir ist das egal.", 0);

    assertErrors("Aber Du bist noch jung, du bist noch fit.", 1);
    assertErrors("Aber Du bist noch jung, dir ist das egal.", 1);
    assertErrors("Aber Du bist noch jung. Und dir ist das egal.", 1);

    assertErrors("Aber du bist noch jung. Und Du bist noch fit.", 1);
    assertErrors("Aber du bist noch jung, Dir ist das egal.", 1);
    assertErrors("Aber du bist noch jung. Und Dir ist das egal.", 1);

    assertErrors("Aber du bist noch jung, sagt euer Vater oft.", 0);
    assertErrors("Aber Du bist noch jung, sagt Euer Vater oft.", 0);
    assertErrors("Aber Du bist noch jung, sagt euer Vater oft.", 1);
    assertErrors("Aber du bist noch jung, sagt Euer Vater oft.", 1);

    assertErrors("Könnt Ihr Euch das vorstellen???", 0);
    assertErrors("Könnt ihr euch das vorstellen???", 0);
    assertErrors("Aber Samstags geht ihr Sohn zum Sport. Stellt Euch das mal vor!", 0);
    assertErrors("Könnt Ihr euch das vorstellen???", 1);
    assertErrors("Wie geht es euch? Herr Meier, wie war ihr Urlaub?", 0);
    assertErrors("Wie geht es Euch? Herr Meier, wie war Ihr Urlaub?", 0);
    assertErrors("Wie geht es euch? Herr Meier, wie war Ihr Urlaub?", 1);
    assertErrors("Wie geht es Euch? Herr Meier, wie war ihr Urlaub?", 1);

    assertErrors("\"Du sagtest, du würdest es schaffen!\"", 0);
    assertErrors("Egal, was du tust: Du musst dein Bestes geben.", 0);
    assertErrors("Was auch immer du tust: ICH UND DU KÖNNEN ES SCHAFFEN.", 0);
  }

  private void assertErrors(String input, int expectedMatches) throws Exception {
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    RuleMatch[] matches = rule.match(Collections.singletonList(sentence));
    assertThat("Expected " + expectedMatches + ", got " + matches.length + ": " + sentence.getText() + " -> " + Arrays.toString(matches),
               matches.length, is(expectedMatches));
  }

}
