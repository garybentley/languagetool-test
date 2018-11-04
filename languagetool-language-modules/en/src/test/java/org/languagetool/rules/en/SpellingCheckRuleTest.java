/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
import org.junit.Before;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.en.MorfologikAmericanSpellerRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SpellingCheckRuleTest {

    // GTODO Really this is only checking the American rule...
  private AmericanEnglish lang;
  private MorfologikAmericanSpellerRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
      lang = new AmericanEnglish();
      rule = lang.createMorfologikSpellerRule(null, null);
      lt = new JLanguageTool(lang);
  }

  @Test
  public void testIgnoreSuggestionsWithMorfologik() throws Exception {
    assertThat(lt.check(rule, "This is anArtificialTestWordForLanguageTool.").size(), is(0));   // no error, as this word is in ignore.txt
    assertThat(lt.check(rule, "How an ab initio calculation works.").size(), is(0));   // As a multi-word entry in spelling.txt "ab initio" must be accepted
    assertThat(lt.check(rule, "Test adjoint").size(), is(0));   // in spelling.txt
    assertThat(lt.check(rule, "Test Adjoint").size(), is(0));   // in spelling.txt (lowercase)

    List<RuleMatch> matches2 = lt.check(rule, "This is a real typoh.");
    assertThat(matches2.size(), is(1));
    // GTODO: What is this asserting?  Why do we care what the rule id is?
    assertThat(matches2.get(0).getRule().getId(), is("MORFOLOGIK_RULE_EN_US"));

    List<RuleMatch> matches3 = lt.check(rule, "This is anotherArtificialTestWordForLanguageTol.");  // note the typo
    assertThat(matches3.size(), is(1));
    assertThat(matches3.get(0).getSuggestedReplacements().toString(), is("[anotherArtificialTestWordForLanguageTool]"));
  }

  @Test
  public void testIgnorePhrases() throws Exception {
    assertThat(lt.check(rule, "A test with myfoo mybar").size(), is(2));
    rule.acceptPhrases(Arrays.asList("myfoo mybar", "Myy othertest"));
    /*
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule instanceof SpellingCheckRule) {
          System.out.println("-=--GOT RULE: " + rule.getId());
        ((SpellingCheckRule) rule).acceptPhrases(Arrays.asList("myfoo mybar", "Myy othertest"));
      } else {
        lt.disableRule(rule.getId());
      }
    }
    */
    assertThat(lt.check(rule, "A test with myfoo mybar").size(), is(0));
    assertThat(lt.check(rule, "A test with myfoo and mybar").size(), is(2));  // the words on their own are not ignored
    assertThat(lt.check(rule, "myfoo mybar here").size(), is(0));
    assertThat(lt.check(rule, "Myfoo mybar here").size(), is(0));
    assertThat(lt.check(rule, "MYfoo mybar here").size(), is(2));

    assertThat(lt.check(rule, "Myy othertest is okay").size(), is(0));
    assertThat(lt.check(rule, "And Myy othertest is okay").size(), is(0));
    assertThat(lt.check(rule, "But Myy Othertest is not okay").size(), is(2));
    assertThat(lt.check(rule, "But myy othertest is not okay").size(), is(2));
  }

  @Test
  public void testIsUrl() throws Exception {
    MySpellCheckingRule rule = new MySpellCheckingRule(new AmericanEnglish());
    rule.test();
  }

  static class MySpellCheckingRule extends SpellingCheckRule {
    MySpellCheckingRule(AmericanEnglish lang) throws Exception {
      super(lang.getMessageBundle(), lang, null, Collections.emptyList(), Collections.emptyList());
    }
    @Override public String getId() { return null; }
    @Override public String getDescription() { return null; }
    @Override public RuleMatch[] match(AnalyzedSentence sentence) throws Exception { return null; }
    void test() throws Exception {
      assertTrue(isUrl("http://www.test.de"));
      assertTrue(isUrl("http://www.test-dash.com"));
      assertTrue(isUrl("https://www.test-dash.com"));
      assertTrue(isUrl("ftp://www.test-dash.com"));
      assertTrue(isUrl("http://www.test-dash.com/foo/path-dash"));
      assertTrue(isUrl("http://www.test-dash.com/foo/öäü-dash"));
      assertTrue(isUrl("http://www.test-dash.com/foo/%C3%B-dash"));
      assertTrue(isUrl("www.languagetool.org"));
      assertFalse(isUrl("languagetool.org"));  // currently not detected
      assertTrue(isEMail("martin.mustermann@test.de"));
      assertTrue(isEMail("martin.mustermann@test.languagetool.de"));
      assertTrue(isEMail("martin-mustermann@test.com"));
      assertFalse(isEMail("@test.de"));
      assertFalse(isEMail("f.test@test"));
      assertFalse(isEMail("f@t.t"));
    }
  }
}
