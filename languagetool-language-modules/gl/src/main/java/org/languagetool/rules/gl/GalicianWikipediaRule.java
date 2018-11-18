/* LanguageTool, a natural language style checker
 * Copyright (C) 2005-2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.gl;

import org.languagetool.language.Galician;
import org.languagetool.rules.AbstractSimpleReplaceRule2;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.tools.Tools;
import org.languagetool.rules.patterns.CaseConverter;

import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import java.net.URL;

/**
 * A rule that matches common Wikipedia errors.
 * Galician implementations. Loads the list of words from
 * <code>/gl/wikipedia.txt</code>.
 *
 * @author Tiago F. Santos
 * @since 4.0
 */
public class GalicianWikipediaRule extends AbstractSimpleReplaceRule2 {

  public static final String WIKIPEDIA_COMMON_ERRORS = "GL_WIKIPEDIA_COMMON_ERRORS";

  // GTODO private static final String FILE_NAME = "/gl/wikipedia.txt";
  // GTODO private static final Locale GL_LOCALE = new Locale("gl");// locale used on case-conversion

  public GalicianWikipediaRule(ResourceBundle messages, List<Map<String, String>> wikipediaWords, CaseConverter caseCon) {
    super(messages, wikipediaWords, caseCon);
    super.setCategory(Categories.WIKIPEDIA.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Grammar);
    addExamplePair(Example.wrong("<marker>a efectos de</marker>"),
                   Example.fixed("<marker>para os efectos de</marker>"));
  }

  @Override
  public final String getId() {
    return WIKIPEDIA_COMMON_ERRORS;
  }

  @Override
  public String getDescription() {
    return "Erros frecuentes nos artigos da Wikipedia";
  }

  @Override
  public String getShort() {
    return "Erro gramatical ou de normativa";
  }

  @Override
  public String getSuggestion() {
    return " é un erro. Considere utilizar ";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " ou ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://gl.wikipedia.org/wiki/Wikipedia:Erros_de_ortograf%C3%ADa_e_desviaci%C3%B3ns");
  }

}
