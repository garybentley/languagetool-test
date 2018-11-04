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
import org.languagetool.language.German;
import org.languagetool.TestTools;
import org.languagetool.UserConfig;
import org.languagetool.language.German;
import org.languagetool.rules.Rule;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Fred Kruse
 */
public class GermanFillerWordsRuleTest {

  @Test
  public void testRule() throws Exception {
      German german = new German();
      GermanFillerWordsRule rule = german.createFillerWordsRule(null, null);
    JLanguageTool lt = new JLanguageTool(german);

    for (Rule r : lt.getAllRules()) {
          lt.disableRule(r.getId());
    }

    lt.addRule(rule);
    lt.enableRule(rule.getId());

    //  more than 8% filler words (default)
    assertEquals(1, lt.check(rule, "Der Satz enthält augenscheinlich ein Füllwort.").size());
    assertEquals(2, lt.check(rule, "Der Satz enthält augenscheinlich relativ viele Füllwörter.").size());
    //  less than 8% filler words - don't show them
    assertEquals(0, lt.check(rule, "Der Satz enthält augenscheinlich ein Füllwort, aber es sind nicht genug um angezeigt zu werden.").size());
    //  direct speach or citation - don't show filler words
    assertEquals(0, lt.check(rule, "»Der Satz enthält augenscheinlich ein Füllwort«").size());

    //  percentage set to zero - show all filler words
    Map<String, Integer> ruleValues = new HashMap<>();
    ruleValues.put("FILLER_WORDS_DE", 0);
    UserConfig userConfig = new UserConfig(ruleValues);
    rule = german.createFillerWordsRule(null, userConfig);
    assertEquals(1, lt.check(rule, "»Der Satz enthält augenscheinlich ein Füllwort«").size());
    assertEquals(1, lt.check(rule, "Der Satz enthält augenscheinlich ein Füllwort, aber es sind nicht genug um angezeigt zu werden.").size());
  }

}
