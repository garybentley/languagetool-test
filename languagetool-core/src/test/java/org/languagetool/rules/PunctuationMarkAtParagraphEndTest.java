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
package org.languagetool.rules;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Fred Kruse
 */
public class PunctuationMarkAtParagraphEndTest {

  private PunctuationMarkAtParagraphEnd rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
      lt = new JLanguageTool(TestTools.getTestLanguage());
      rule = new PunctuationMarkAtParagraphEnd(TestTools.getEnglishMessages(), TestTools.getTestLanguage());
  }

  @Test
  public void testRule() throws Exception {
    check(0, "This is a test sentence.");
    check(0, "This is a test headline");
    check(1, "This is a test sentence. And this is a second test sentence");
    check(1, "\"This is a test sentence. And this is a second test sentence");
    check(0, "This is a test sentence. And this is a second test sentence.");
    check(0, "B. v. – Beschluss vom");
    check(1, "This is a test sentence.\nAnd this is a second test sentence. Here is a quotation mark missing");
    check(0, "This is a test sentence.\nAnd this is a second test sentence. Here is a quotation mark missing.");
  }

  private void check(int v, String str) throws Exception {
      assertEquals(v, lt.check(rule, str).size());
  }

}
