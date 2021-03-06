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
package org.languagetool.tagging.nl;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Dutch;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.nl.DutchWordTokenizer;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.Tagger;

public class DutchTaggerTest {

  private Dutch lang;
  private Tagger tagger;
  private Tokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    lang = new Dutch();
    tagger = lang.getDefaultDataBroker().getTagger();
    tokenizer = lang.getWordTokenizer();
  }

  @Test
  public void testDictionary() throws Exception {
      if (tagger instanceof BaseTagger) {
          TestTools.testTaggerDictionary(((BaseTagger) tagger).getDictionary(), lang);
      } else {
          System.out.println("testDictionary test not run due to Tagger not being an instance of: " + BaseTagger.class.getName());
      }
  }

  @Test
  public void testTagger() throws Exception {
    TestTools.myAssert("Dit is een Nederlandse zin om het programma'tje te testen.",
        "Dit/[null]null -- is/[is]ZNW:EKV:DE_|is/[zijn]WKW:TGW:3EP -- een/[een]GET|een/[een]ZNW:EKV:DE_ -- Nederlandse/[Nederlands]BNW:STL:VRB|Nederlandse/[Nederlandse]ZNW:EKV:DE_ -- zin/[zin]ZNW:EKV:DE_|zin/[zinnen]WKW:TGW:1EP -- om/[null]null -- het/[null]null -- programma'tje/[null]null -- te/[te]VRZ -- testen/[test]ZNW:MRV:DE_|testen/[testen]WKW:TGW:INF", tokenizer, tagger);
    TestTools.myAssert("zwijnden", "zwijnden/[zwijnen]WKW:VLT:INF", tokenizer, tagger);
  }
}
