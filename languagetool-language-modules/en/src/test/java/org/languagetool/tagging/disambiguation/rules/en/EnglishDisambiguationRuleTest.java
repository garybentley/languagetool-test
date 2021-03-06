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
package org.languagetool.tagging.disambiguation.rules.en;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.AnalyzedSentence;

public class EnglishDisambiguationRuleTest extends DisambiguationRuleTest {

  private Tagger tagger;
  private Tokenizer tokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private Disambiguator disambiguator;
  private Disambiguator disamb2;

  @Before
  public void setUp() throws Exception {
      English lang = new English();
      tagger = lang.getTagger();
      tokenizer = lang.getWordTokenizer();
      sentenceTokenizer = lang.getSentenceTokenizer();
      disambiguator = lang.getDisambiguator();
      // This just acts as a pass through for the input.
      disamb2 = new AbstractDisambiguator() {
          @Override
          public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {
            return input;
          }
      };
  }

  @Test
  public void testChunker() throws Exception {
    TestTools.myAssert("I cannot have it.",
        "/[null]SENT_START I/[I]PRP  /[null]null cannot/[can]MD  /[null]null have/[have]VB  /[null]null it/[it]PRP ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("I cannot have it.",
        "/[null]SENT_START I/[I]PRP  /[null]null cannot/[can]MD  /[null]null have/[have]NN|have/[have]VB|have/[have]VBP  /[null]null it/[it]PRP ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("He is to blame.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null to/[to]IN|to/[to]TO  /[null]null blame/[blame]VB ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("He is to blame.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null to/[to]IN|to/[to]TO  /[null]null blame/[blame]JJ|blame/[blame]NN:UN|blame/[blame]VB|blame/[blame]VBP ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disamb2);
    TestTools.myAssert("He is well known.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null well/[well]RB  /[null]null known/[known]JJ ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disambiguator);
    TestTools.myAssert("He is well known.",
        "/[null]SENT_START He/[he]PRP  /[null]null is/[be]VBZ  /[null]null well/[well]NN|well/[well]RB|well/[well]UH|well/[well]VB|well/[well]VBP  /[null]null known/[know]VBN|known/[known]NN ./[null]null",
        tokenizer, sentenceTokenizer, tagger, disamb2);
  }

}
