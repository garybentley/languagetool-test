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
package org.languagetool.rules;

import org.junit.Test;
import org.junit.Before;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Marcin Milkowski
 */
public class MultipleWhitespaceRuleTest {

  private MultipleWhitespaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
       lt = new JLanguageTool(TestTools.getTestLanguage());
       rule = new MultipleWhitespaceRule(TestTools.getEnglishMessages());
  }

  @Test
  public void testRule() throws Exception {
    List<RuleMatch> matches;

    // correct sentences:
    assertGood("This is a test sentence.");
    assertGood("This is a test sentence...");
    assertGood("\n\tThis is a test sentence...");
    assertGood("Multiple tabs\t\tare okay");
    assertGood("\n This is a test sentence...");
    assertGood("\n    This is a test sentence...");
    // Needs isParagraphStart creation. Excluding i = 1 will make the rule ignore multiple white spaces in middle senteces.
    // matches = rule.match(langTool.getAnalyzedSentence("    This is a test sentence..."));
    // assertEquals(0, matches.length);

    // incorrect sentences:
    matches = lt.check(rule, "This  is a test sentence.");
    assertEquals(1, matches.size());
    assertEquals(4, matches.get(0).getFromPos());
    assertEquals(6, matches.get(0).getToPos());
    matches = lt.check(rule, "\n   This  is a test sentence.");
    assertEquals(1, matches.size());
    assertEquals(8, matches.get(0).getFromPos());
    assertEquals(10, matches.get(0).getToPos());
    matches = lt.check(rule, "This is a test   sentence.");
    assertEquals(1, matches.size());
    assertEquals(14, matches.get(0).getFromPos());
    assertEquals(17, matches.get(0).getToPos());
    matches = lt.check(rule, "This is   a  test   sentence.");
    assertEquals(3, matches.size());
    assertEquals(7, matches.get(0).getFromPos());
    assertEquals(10, matches.get(0).getToPos());
    assertEquals(11, matches.get(1).getFromPos());
    assertEquals(13, matches.get(1).getToPos());
    assertEquals(17, matches.get(2).getFromPos());
    assertEquals(20, matches.get(2).getToPos());
    matches = lt.check(rule, "\t\t\t    \t\t\t\t  ");
    assertEquals(2, matches.size());
    //with non-breakable spaces
    matches = lt.check(rule, "This \u00A0is a test sentence.");
    assertEquals(1, matches.size());
    assertEquals(4, matches.get(0).getFromPos());
    assertEquals(6, matches.get(0).getToPos());
  }

  private void assertGood(String input) throws Exception {
    List<RuleMatch> ruleMatches = lt.check(rule, input);
    assertEquals(0, ruleMatches.size());
  }
/*
GTODO: Clean up
  public static MultipleWhitespaceRule getMultipleWhitespaceRule(JLanguageTool langTool) {
    for (Rule rule : langTool.getAllActiveRules()) {
      if (rule instanceof MultipleWhitespaceRule) {
        return (MultipleWhitespaceRule)rule;
      }
    }
    throw new RuntimeException("Rule not found: " + GenericUnpairedBracketsRule.class);
  }
*/

}
