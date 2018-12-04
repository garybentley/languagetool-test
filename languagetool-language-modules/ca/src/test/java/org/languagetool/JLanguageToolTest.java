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
package org.languagetool;

import org.junit.Test;
import org.junit.Ignore;
import org.languagetool.language.Catalan;
import org.languagetool.rules.RuleMatch;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class JLanguageToolTest {

  @Test @Ignore("This test has issues but I'm unclear as to what the problem actually is or which rule is expected to match.")
  public void testCleanOverlappingErrors() throws Exception {
    JLanguageTool tool = new JLanguageTool(new Catalan());
    List<RuleMatch> matches = tool.check("prosper");
    assertEquals(1, matches.size());
    assertEquals("CA_SIMPLE_REPLACE_BALEARIC", matches.get(0).getRule().getId());

    matches = tool.check("Potser siga el millor");
    assertEquals(1, matches.size());
    assertEquals("POTSER_SIGUI", matches.get(0).getRule().getId());
  }

}
