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
package org.languagetool.rules.de;

import org.junit.Test;
import org.junit.Before;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WiederVsWiderRuleTest {

  private WiederVsWiderRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
      German german = new German();
      rule = german.createWiederVsWiderRule(null);
      lt = new JLanguageTool(german);
  }

  @Test
  public void testRule() throws Exception {
    assertGood("Das spiegelt wider, wie es wieder läuft.");
    assertGood("Das spiegelt die Situation gut wider.");
    assertGood("Das spiegelt die Situation.");
    assertGood("Immer wieder spiegelt das die Situation.");
    assertGood("Immer wieder spiegelt das die Situation wider.");
    assertGood("Das spiegelt wieder wider, wie es läuft.");

    assertBad("Das spiegelt wieder, wie es wieder läuft.");
    assertBad("Sie spiegeln das Wachstum der Stadt wieder.");
    assertBad("Das spiegelt die Situation gut wieder.");
    assertBad("Immer wieder spiegelt das die Situation wieder.");
    assertBad("Immer wieder spiegelte das die Situation wieder.");
  }

  private void assertGood(String text) throws Exception {
    assertEquals(0, rule.match(lt.getAnalyzedSentence(text)).length);
  }

  private void assertBad(String text) throws Exception {
    assertEquals(1, rule.match(lt.getAnalyzedSentence(text)).length);
  }

}
