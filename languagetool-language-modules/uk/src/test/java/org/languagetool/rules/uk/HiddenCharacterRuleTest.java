/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Andriy Rysin
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
package org.languagetool.rules.uk;

import org.junit.Test;
import org.junit.Ignore;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HiddenCharacterRuleTest {

  @Test @Ignore
  // GTODO Ignore this test since it breaks when trying to get the tagger and there is no tagger dictionary file.
  public void testRule() throws Exception {
    Ukrainian lang = new Ukrainian();
    final HiddenCharacterRule rule = lang.createHiddenCharacterRule(null);
    final JLanguageTool langTool = new JLanguageTool(lang);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("сміття")).length);

    //incorrect sentences:

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("смi\u00ADття"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("сміття"), matches[0].getSuggestedReplacements());
  }

}
