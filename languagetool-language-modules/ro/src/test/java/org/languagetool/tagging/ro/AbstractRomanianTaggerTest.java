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
package org.languagetool.tagging.ro;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.TestTools;
import org.languagetool.language.Romanian;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.BaseTagger;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Root class for RomanianTagger tests.
 * Provides convenient methods to find specific lemma/pos
 *
 * @author Ionuț Păduraru
 */
public abstract class AbstractRomanianTaggerTest {

  protected Romanian lang;
  protected Tagger tagger;
  protected Tokenizer tokenizer;

  @Before
  public void setUp() throws Exception {
    lang = new Romanian();
    tagger = lang.getTagger();
    tokenizer = lang.getWordTokenizer();
  }

  @Test
  public void testDictionary() throws Exception {
    if (tagger instanceof BaseTagger) {
        TestTools.testTaggerDictionary(((BaseTagger) tagger).getDictionary(), lang);
    } else {
        System.out.println("Unable to test tagger dictionary, tagger is instanceof: " + tagger.getClass().getName());
    }
  }

  /**
   * Verify if <code>inflected</code> contains the specified lemma and pos
   *
   * @param inflected input word, inflected form
   * @param lemma expected lemma
   * @param posTag expected tag for lemma
   */
  protected void assertHasLemmaAndPos(String inflected, String lemma,
                                      String posTag) throws Exception {
    final List<AnalyzedTokenReadings> tags = tagger.tag(Arrays.asList(inflected));
    final StringBuilder allTags = new StringBuilder();
    boolean found = false;
    for (AnalyzedTokenReadings analyzedTokenReadings : tags) {
      for (AnalyzedToken token : analyzedTokenReadings) {
        final String crtLemma = token.getLemma();
        final String crtPOSTag = token.getPOSTag();
        allTags.append(String.format("[%s/%s]", crtLemma, crtPOSTag));
        found = (lemma == null || lemma.equals(crtLemma))
                && (posTag == null || posTag.equals(crtPOSTag));
        if (found) {
          break;
        }
      }
      if (found) {
        break;
      }
    }
    assertTrue(String.format("Lemma and POS not found for word [%s]! "
            + "Expected [%s/%s]. Actual: %s", inflected, lemma, posTag,
            allTags.toString()), found);
  }

}
