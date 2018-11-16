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
package org.languagetool.tagging.eo;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Esperanto;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.Tokenizer;

public class EsperantoTaggerTest {

  private Tagger tagger;
  private Tokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    Esperanto lang = new Esperanto();
    tagger = lang.getTagger();
    tokenizer = lang.getWordTokenizer();
  }

  @Test
  public void testTagger() throws Exception {
    TestTools.myAssert("Tio estas simpla testo",
        "Tio/[null]T nak np t o -- estas/[esti]V nt as -- simpla/[simpla]A nak np -- testo/[testo]O nak np", tokenizer, tagger);
    TestTools.myAssert("Mi malsategas",
        "Mi/[mi]R nak np -- malsategas/[malsategi]V nt as", tokenizer, tagger);
    TestTools.myAssert("Li malŝategas sin",
        "Li/[li]R nak np -- malŝategas/[malŝategi]V tr as -- sin/[si]R akz np", tokenizer, tagger);

    // An Esperanto Pangram i.e. it includes all letters of the Esperanto
    // alphabet to check that lemma gets transformed from x-system into Unicode.
    TestTools.myAssert("Sxajnas ke sagaca monahxo lauxtvocxe rifuzadis pregxi sur herbajxo",
        "Sxajnas/[ŝajni]V nt as -- " +
        "ke/[ke]_ -- " +
        "sagaca/[sagaca]A nak np -- " +
        "monahxo/[monaĥo]O nak np -- " +
        "lauxtvocxe/[laŭtvoĉe]E nak -- " +
        "rifuzadis/[rifuzadi]V tr is -- " +
        "pregxi/[preĝi]V nt i -- " +
        "sur/[sur]P kak -- " +
        "herbajxo/[herbaĵo]O nak np", tokenizer, tagger);
  }
}
