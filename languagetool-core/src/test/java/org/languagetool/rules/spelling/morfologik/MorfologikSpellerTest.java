/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.morfologik;

import org.junit.Test;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import morfologik.stemming.Dictionary;
import org.languagetool.TestTools;
import org.languagetool.TestLanguage;

public class MorfologikSpellerTest {

  private Dictionary dictionary;

  @Before
  public void setUp() throws Exception {
      TestLanguage lang = TestTools.getTestLanguage();
      dictionary = lang.getUseDataBroker().getMorfologikBinaryDictionaryFromResourcePath(String.format("/%1$s/spelling/test.dict", lang.getLocale().getLanguage()));
  }

  @Test
  public void testIsMisspelled() throws Exception {
    // We expect a binary dictionary file.
    MorfologikSpeller speller = new MorfologikSpeller(dictionary, 1);
    assertTrue(speller.convertsCase());

    assertFalse(speller.isMisspelled("wordone"));
    assertFalse(speller.isMisspelled("Wordone"));
    assertFalse(speller.isMisspelled("wordtwo"));
    assertFalse(speller.isMisspelled("Wordtwo"));
    assertFalse(speller.isMisspelled("Uppercase"));
    assertFalse(speller.isMisspelled("Häuser"));

    assertTrue(speller.isMisspelled("Hauser"));
    assertTrue(speller.isMisspelled("wordones"));
    assertTrue(speller.isMisspelled("nosuchword"));
  }

  @Test
  public void testGetSuggestions() throws Exception {
    MorfologikSpeller spellerDist1 = new MorfologikSpeller(dictionary, 1);
    MorfologikSpeller spellerDist2 = new MorfologikSpeller(dictionary, 2);

    assertThat(spellerDist1.getSuggestions("wordone").toString(), is("[]"));
    assertThat(spellerDist1.getSuggestions("wordonex").toString(), is("[wordone]"));
    assertThat(spellerDist2.getSuggestions("wordone").toString(), is("[]"));
    assertThat(spellerDist2.getSuggestions("wordonex").toString(), is("[wordone]"));

    assertThat(spellerDist1.getSuggestions("wordonix").toString(), is("[]"));
    assertThat(spellerDist2.getSuggestions("wordonix").toString(), is("[wordone]"));

    assertThat(spellerDist2.getSuggestions("wordoxix").toString(), is("[]"));
  }
}
