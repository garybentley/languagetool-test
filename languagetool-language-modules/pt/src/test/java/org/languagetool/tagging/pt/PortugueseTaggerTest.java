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
package org.languagetool.tagging.pt;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.TestTools;
import org.languagetool.language.Portuguese;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.DefaultPortugueseResourceDataBroker;

public class PortugueseTaggerTest {

  private Portuguese lang;
  private PortugueseTagger tagger;
  private Tokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    lang = new Portuguese();
      DefaultPortugueseResourceDataBroker broker = new DefaultPortugueseResourceDataBroker(lang, lang.getClass().getClassLoader());
    tagger = broker.getTagger();
    tokenizer = lang.getWordTokenizer();
  }

  @Test
  public void testDictionary() throws Exception {
    TestTools.testTaggerDictionary(tagger.getDictionary(), lang);
  }

  @Test
  public void testTagger() throws Exception {
    TestTools.myAssert("Estes são os meus amigos.",
        "Estes/[este]DD0MP0|Estes/[este]PD0MP000 -- "
            + "são/[ser]VMIP3P0|são/[são]AQ0MS0|são/[são]NCMS000 -- "
            + "os/[o]DA0MP0|os/[o]PD0MP000|os/[o]PP3MPA00 -- "
            + "meus/[meu]AP0MP1S|meus/[meu]DP1MPS -- "
            + "amigos/[amigo]AQ0MP0|amigos/[amigo]NCMP000", tokenizer, tagger);
  }
}
