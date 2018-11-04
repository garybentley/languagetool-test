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
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.Rule;

import static org.junit.Assert.assertEquals;

/**
 * @author Fred Kruse
 */
public class GermanParagraphRepeatBeginningRuleTest {

  @Test
  public void testRule() throws Exception {
      German lang = new German();
      GermanParagraphRepeatBeginningRule rule = lang.createParagraphRepeatBeginningRule(null);
    JLanguageTool lt = new JLanguageTool(lang);

    for (Rule r : lt.getAllRules()) {
      lt.disableRule(r.getId());
    }

    lt.addRule(rule);
    lt.enableRule(rule.getId());

    assertEquals(2, lt.check("Der Hund spazierte über die Straße.\n\nDer Hund ignorierte den Verkehr.").size());
    assertEquals(0, lt.check("Der Hund spazierte über die Straße.\n\nDas Tier ignorierte den Verkehr.").size());
    assertEquals(2, lt.check("Peter spazierte über die Straße.\n\nPeter ignorierte den Verkehr.").size());
    assertEquals(0, lt.check("Peter spazierte über die Straße.\n\nDer Junge ignorierte den Verkehr.").size());
    assertEquals(2, lt.check("»Peter spazierte über die Straße.«\n\n»Peter ignorierte den Verkehr.«").size());
  }

}
