/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.language.Demo;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class MultiThreadedJLanguageToolTest {

  @Test
  public void testCheck() throws Exception {
    MultiThreadedJLanguageTool lt1 = new MultiThreadedJLanguageTool(TestTools.getTestLanguage());
    lt1.setCleanOverlappingMatches(false);
    List<String> ruleMatchIds1 = getRuleMatchIds(lt1);
    assertEquals(9, ruleMatchIds1.size());
    lt1.shutdown();

    JLanguageTool lt2 = new JLanguageTool(TestTools.getTestLanguage());
    lt2.setCleanOverlappingMatches(false);
    List<String> ruleMatchIds2 = getRuleMatchIds(lt2);
    assertEquals(ruleMatchIds1, ruleMatchIds2);
  }

  @Test
  public void testShutdownException() throws Exception {
    MultiThreadedJLanguageTool tool = new MultiThreadedJLanguageTool(TestTools.getTestLanguage());
    getRuleMatchIds(tool);
    tool.shutdown();
    try {
      getRuleMatchIds(tool);
      fail("should have been rejected as the thread pool has been shut down");
    } catch (RejectedExecutionException ignore) {}
  }

  @Test
  public void testTextAnalysis() throws Exception {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(TestTools.getTestLanguage());
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText("This is a sentence. And another one.");
    System.out.println ("----> " + analyzedSentences);
    assertThat(analyzedSentences.size(), is(2));
    assertThat(analyzedSentences.get(0).getTokens().length, is(10));
    assertThat(analyzedSentences.get(0).getTokensWithoutWhitespace().length, is(6));  // sentence start has its own token
    assertThat(analyzedSentences.get(1).getTokens().length, is(7));
    assertThat(analyzedSentences.get(1).getTokensWithoutWhitespace().length, is(5));
    lt.shutdown();
  }

  @Test
  public void testConfigurableThreadPoolSize() throws Exception {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(TestTools.getTestLanguage());
    assertEquals(Runtime.getRuntime().availableProcessors(), lt.getThreadPoolSize());
    lt.shutdown();
  }

  private List<String> getRuleMatchIds(JLanguageTool langTool) throws Exception {
    String input = "A small toast. No error here. Foo go bar. First goes last there, please!";
    List<RuleMatch> matches = langTool.check(input);
    List<String> ruleMatchIds = new ArrayList<>();
    for (RuleMatch match : matches) {
      ruleMatchIds.add(match.getRule().getId());
    }
    return ruleMatchIds;
  }

  @Test
  public void testTwoRulesOnly() throws Exception {
    MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(new TestLanguage() {
      @Override
      public synchronized List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }

      @Override
      public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) {
        // less rules than processors (depending on the machine), should at least not crash
        return Arrays.asList(
                new UppercaseSentenceStartRule(messages, this),
                new MultipleWhitespaceRule(messages, this)
        );
      }
    });
    assertThat(lt.check("my test  text").size(), is(2));
    lt.shutdown();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalThreadPoolSize1() throws Exception {
    new MultiThreadedJLanguageTool(TestTools.getTestLanguage(), 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIllegalThreadPoolSize2() throws Exception {
    new MultiThreadedJLanguageTool(TestTools.getTestLanguage(), null, 0, null);
  }
}
