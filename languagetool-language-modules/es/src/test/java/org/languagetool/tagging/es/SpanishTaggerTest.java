/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.es;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.BaseTagger;

import java.io.IOException;

public class SpanishTaggerTest {

  private Spanish lang;
  private Tagger tagger;
  private Tokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    lang = new Spanish();
    tagger = lang.getTagger();
    tokenizer = lang.getWordTokenizer();
  }

  @Test
  public void testDictionary() throws Exception {
    if (tagger instanceof BaseTagger) {
        TestTools.testTaggerDictionary(((BaseTagger) tagger).getDictionary(), lang);
    }
  }

  @Test
  public void testTagger() throws Exception {
    TestTools.myAssert("Soy un hombre muy honrado.",
        "Soy/[ser]VSIP1S0 -- un/[uno]DI0MS0 -- hombre/[hombre]I|hombre/[hombre]NCMS000 -- muy/[muy]RG -- honrado/[honrar]VMP00SM", tokenizer, tagger);
    TestTools.myAssert("Tengo que ir a mi casa.",
        "Tengo/[tener]VMIP1S0 -- que/[que]CS|que/[que]PR0CN000 -- ir/[ir]VMN0000 -- a/[a]NCFS000|a/[a]SPS00 -- mi/[mi]DP1CSS|mi/[mi]NCMS000 -- casa/[casa]NCFS000|casa/[casar]VMIP3S0|casa/[casar]VMM02S0", tokenizer, tagger);
    TestTools.myAssert("blablabla","blablabla/[null]null", tokenizer, tagger);
  }
}
