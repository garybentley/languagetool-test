/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Ebrahim Byagowi <ebrahim@gnu.org>
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
package org.languagetool.rules.fa;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Persian;
import org.languagetool.rules.TextLevelRule;
import org.languagetool.rules.patterns.PatternRuleTest;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class WordCoherencyRuleTest extends PatternRuleTest {

  @Test
  public void testRules() throws Exception {
    Persian lang = new Persian();
    JLanguageTool langTool = new JLanguageTool(lang);
    TextLevelRule rule = lang.createWordCoherencyRule(null);
    assertThat(rule.match(Collections.singletonList(langTool.getAnalyzedSentence("این یک اتاق است."))).length, is(0));
    assertThat(rule.match(Collections.singletonList(langTool.getAnalyzedSentence("این یک اتاق است. این یک اطاق است."))).length, is(1));
  }

}
