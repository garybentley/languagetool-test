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
package org.languagetool.rules.pl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.rules.AbstractCompoundRuleTest;
import org.languagetool.rules.RuleMatch;

import static org.junit.Assert.assertEquals;

public class CompoundRuleTest extends AbstractCompoundRuleTest {

  @Before
  public void setUp() throws Exception {
    Polish language = new Polish();
    lt = new JLanguageTool(language);
    rule = language.createCompoundRule(null);
  }

  @Test
  public void testRule() throws Exception {
    // correct sentences:
    check(0, "Nie róbmy nic na łapu-capu.");
    check(0, "Jedzmy kogel-mogel.");
    // incorrect sentences:
    check(1, "bim bom", new String[]{"bim-bom"});
  }

/*
 GTODO This should test the data not the file...
  @Test
  public void testCompoundFile() throws IOException {
    final MorfologikPolishSpellerRule spellRule =
        new MorfologikPolishSpellerRule (TestTools.getMessages("pl"), new Polish(), null);
    final InputStream   file = JLanguageTool.getDataBroker().getFromResourceDirAsStream("/pl/compounds.txt");
    try (Scanner scanner = new Scanner(file, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;     // ignore comments
        }
        if (line.endsWith("+")) {
          line = removeLastCharacter(line);
          line = line.replace('-', ' ');
          final RuleMatch[] ruleMatches =
              spellRule.match(lt.getAnalyzedSentence(line));
          assertEquals("The entry: " + line + " is not found in the spelling dictionary!",
              0, ruleMatches.length);
        } else if (line.endsWith("*")) {
          line = removeLastCharacter(line);
          final RuleMatch[] ruleMatches =
              spellRule.match(lt.getAnalyzedSentence(line));
          assertEquals("The entry: " + line + " is not found in the spelling dictionary!",
              0, ruleMatches.length);
        } else {
          assertEquals("The entry: " + line + " is not found in the spelling dictionary!",
              0, spellRule.match(lt.getAnalyzedSentence(line)).length);
          assertEquals("The entry: " + line.replace("-", "") + " is not found in the spelling dictionary!",
              0, spellRule.match(lt.getAnalyzedSentence(line.replace("-", ""))).length);
        }
      }
    }
  }
*/
  private String removeLastCharacter(String str) {
    return str.substring(0, str.length() - 1);
  }


}
