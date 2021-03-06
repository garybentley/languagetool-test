/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.tl;

import org.junit.Test;
import org.junit.Before;
import org.languagetool.TestTools;
import org.languagetool.language.Tagalog;
import org.languagetool.tokenizers.SentenceTokenizer;

public class TagalogSRXSentenceTokenizerTest {

  private SentenceTokenizer stokenizer;
  private Tagalog lang;

  @Before
  public void setUp() throws Exception {
      lang = new Tagalog();
      stokenizer = lang.getSentenceTokenizer();
  }

  @Test
  public void testTokenize() {
    testSplit("Ang Linux ay isang operating system kernel para sa mga operating system na humahalintulad sa Unix. ",
              "Isa ang Linux sa mga pinaka-prominanteng halimbawa ng malayang software at pagsasagawa ng open source; " +
              "madalas, malayang mapapalitan, gamitin, at maipamahagi ninuman ang " +
              "lahat ng pinag-ugatang source code (pinagmulang kodigo).");
  }

  private void testSplit(String... sentences) {
    TestTools.testSplit(stokenizer, sentences);
  }

}
