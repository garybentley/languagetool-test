/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.junit.Test;
import org.junit.Before;
import org.languagetool.JLanguageTool;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FrenchCompoundAwareHunspellRuleTest {

    private French lang;
    private FrenchCompoundAwareHunspellRule rule;

    @Before
    public void setUp() throws Exception {
        lang = new French();
        rule = lang.createSpellerRule(null, null);
    }

  @Test
  public void testSpellcheck() throws Exception {
    final JLanguageTool langTool = new JLanguageTool(lang);
    List<RuleMatch> matches1 = langTool.check(rule, "Ca");
    assertThat(matches1.size(), is(1));
    assertThat(matches1.get(0).getSuggestedReplacements().get(0), is("Ça"));   // see #912
    List<RuleMatch> matches2 = langTool.check(rule, "Décu");
    assertThat(matches2.size(), is(1));
    assertThat(matches2.get(0).getSuggestedReplacements().get(0), is("Déçu"));   // see #912
  }

  @Test
  public void testRuleWithFrench() throws Exception {
    final JLanguageTool langTool = new JLanguageTool(lang);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Un test simple.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("Un test simpple.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Le cœur, la sœur.")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("LanguageTool")).length);

    // Tests with dash and apostrophes.
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Il arrive après-demain.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'Haÿ-les-Roses")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("L'Haÿ les Roses")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Aujourd'hui et jusqu'à demain.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Aujourd’hui et jusqu’à demain.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L'Allemagne et l'Italie.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("L’Allemagne et l’Italie.")).length);
    assertEquals(2, rule.match(langTool.getAnalyzedSentence("L’allemagne et l’italie.")).length);
  }

  @Test
  public void testImmunizedFrenchWord() throws Exception {
    //GTODO final HunspellRule rule = new HunspellRule(TestTools.getMessages("fr"), french, null);
    JLanguageTool langTool = new JLanguageTool(lang);

    // GTODO Why not test for a real immunized spelling?  i.e. grand faim
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("grand faim.")).length);

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("languageTool est génial.")).length);
/*
GTODO Clean up
    final French frenchWithDisambiguator = new French(){
      @Override
      public Disambiguator getDisambiguator() {
        return new TestFrenchDisambiguator();
      }
    };
    langTool = new JLanguageTool(frenchWithDisambiguator);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("languageTool est génial.")).length);
    */
  }

}
