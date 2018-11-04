/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Marcin Miłkowski (www.languagetool.org)
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

package org.languagetool.rules.bitext;

import org.junit.Test;
import org.languagetool.FakeLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.TestLanguage;
import org.languagetool.rules.RuleMatch;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.xx.DemoChunker;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DifferentLengthRuleTest {

  @Test
  public void testRule() throws Exception {
    DifferentLengthRule rule = new DifferentLengthRule();
    RuleMatch[] matches;
    JLanguageTool srcLangTool = new JLanguageTool(new TestLanguage()
    {
        @Override
        public Tagger getTagger() {
            return new DemoTagger();
        }

        @Override
        public Chunker getChunker() {
            return new DemoChunker();
        }
    });
    JLanguageTool trgLangTool = new JLanguageTool(new TestLanguage()
    {
        @Override
        public Tagger getTagger() {
            return new DemoTagger();
        }

        @Override
        public Disambiguator getDisambiguator() {
            return new DemoDisambiguator();
        }

    });

    // GTODO JLanguageTool trgLangTool = new JLanguageTool(TestTools.getDemoLanguage());
    // GTODO JLanguageTool srcLangTool = new JLanguageTool(new FakeLanguage());
    rule.setSourceLanguage(srcLangTool.getLanguage());
    // GTODO TestTools.getDemoLanguage());
    // correct sentences:
    matches = rule.match(
        srcLangTool.getAnalyzedSentence("This is a test sentence."),
        trgLangTool.getAnalyzedSentence("To zdanie testowe."));
    assertEquals(0, matches.length);

    matches = rule.match(
        srcLangTool.getAnalyzedSentence("Click this button."),
        trgLangTool.getAnalyzedSentence("Kliknij ten przycisk."));
    assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(
        srcLangTool.getAnalyzedSentence("Open a file, and check if it is corrupt."),
        trgLangTool.getAnalyzedSentence("Otwórz plik."));
    assertEquals(1, matches.length);
  }

}
