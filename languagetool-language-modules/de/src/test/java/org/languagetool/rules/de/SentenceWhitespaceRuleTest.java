/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class SentenceWhitespaceRuleTest {

    private SentenceWhitespaceRule rule;
    private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
      German german = new German();
      rule = german.createSentenceWhitespaceRule(null);
      lt = new JLanguageTool(german);
  }

  @Test
  public void testMatch() throws Exception {

    assertGood("Das ist ein Satz. Und hier der nächste.");
    assertGood("Das ist ein Satz! Und hier der nächste.");
    assertGood("Ist das ein Satz? Hier der nächste.");

    assertBad("Das ist ein Satz.Und hier der nächste.");
    assertBad("Das ist ein Satz!Und hier der nächste.");
    assertBad("Ist das ein Satz?Hier der nächste.");

    assertGood("Am 28. September.");
    assertBad("Am 28.September.");

    assertTrue(lt.check(rule, "Am 7.September 2014.").get(0).getMessage().contains("nach Ordnungszahlen"));
    assertTrue(lt.check(rule, "Im September.Dann der nächste Satz.").get(0).getMessage().contains("zwischen Sätzen"));
  }

  private void assertGood(String text) throws Exception {
    assertThat(lt.check(rule, text).size(), is(0));
  }

  private void assertBad(String text) throws Exception {
    assertThat(lt.check(rule, text).size(), is(1));
  }
}
