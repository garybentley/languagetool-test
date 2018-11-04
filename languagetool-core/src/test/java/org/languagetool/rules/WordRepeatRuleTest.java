/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.TestLanguage;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class WordRepeatRuleTest {

  private JLanguageTool lt;
  private WordRepeatRule rule;

  @Before
  public void setUp() throws Exception {
      lt = new JLanguageTool(TestTools.getTestLanguage());
      rule = new WordRepeatRule(TestTools.getTestLanguage().getMessageBundle());
  }

  @Test
  public void test() throws Exception {
    assertGood("A test");
    assertGood("A test.");
    assertGood("A test...");
    assertGood("1 000 000 years");
    assertGood("010 020 030");

    assertBad("A A test");
    assertBad("A a test");
    assertBad("This is is a test");
  }

  private void assertGood(String s) throws Exception {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(0));
  }

  private void assertBad(String s) throws Exception {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertThat(matches.length, is(1));
  }

}
