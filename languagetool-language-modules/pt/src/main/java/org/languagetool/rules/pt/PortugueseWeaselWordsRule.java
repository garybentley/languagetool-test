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
package org.languagetool.rules.pt;

import org.languagetool.language.Portuguese;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.tools.Tools;

import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.ResourceBundle;

import java.net.URL;

/**
 * A rule that matches known empty expressions.
 * Portuguese implementation. Loads the list of words from
 * <code>/pt/weaselwords.txt</code>.
 *
 * @author Tiago F. Santos
 * @since 3.9
 */
public class PortugueseWeaselWordsRule extends AbstractSimpleReplaceRule2 {

  public static final String PT_WEASELWORD_REPLACE = "PT_WEASELWORD_REPLACE";

  // GTODO private static final String FILE_NAME = "/pt/weaselwords.txt";
  // GTODO private static final Locale PT_LOCALE = new Locale("pt");  // locale used on case-conversion
/*
GTODO
  @Override
  public final String getFileName() {
    return FILE_NAME;
  }
*/
  public PortugueseWeaselWordsRule(ResourceBundle messages, List<Map<String, String>> weaselWords, CaseConverter caseCon) {
      super(messages, weaselWords, caseCon);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>Diz-se</marker> que programas gratuitos não têm qualidade."),
                   Example.fixed("<marker>XYZ</marker> dizem que programas gratuitos não têm qualidade. Por isso vendem programas pagos."));
  }

  @Override
  public final String getId() {
    return PT_WEASELWORD_REPLACE;
  }

  @Override
  public String getDescription() {
    return "Escrita avançada: Expressões evasivas";
  }

  @Override
  public String getShort() {
    return "Expressão evasiva";
  }

  @Override
  public String getSuggestion() {
    return " é uma expressão ambígua e evasiva. Reconsidere o seu uso, de acordo com o objetivo do seu texto. ";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://pt.wikipedia.org/wiki/Weasel_word");
  }
/*
GTODO
  @Override
  public Locale getLocale() {
    return PT_LOCALE;
  }
*/
}
