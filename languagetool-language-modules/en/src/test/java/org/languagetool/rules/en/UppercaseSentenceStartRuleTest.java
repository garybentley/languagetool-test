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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.English;
import org.languagetool.rules.UppercaseSentenceStartRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testRule() throws Exception {
    English lang = new English();
    UppercaseSentenceStartRule rule = lang.createUppercaseSentenceStartRule(null);
    JLanguageTool lt = new JLanguageTool(lang);
    assertEquals(0, lt.check(rule, "In Nov. next year.").size());
    assertEquals(0, lt.check(rule, "www.languagetool.org is a website.").size());
    assertEquals(0, lt.check(rule, "Languagetool.org is a website.").size());
    assertEquals(1, lt.check(rule, "languagetool.org is a website.").size());
    assertEquals(1, lt.check(rule, "a sentence.").size());
    assertEquals(1, lt.check(rule, "a sentence!").size());
  }

}
