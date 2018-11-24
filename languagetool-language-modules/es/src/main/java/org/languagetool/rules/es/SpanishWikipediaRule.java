/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.es;

import org.languagetool.language.Spanish;
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
 * Spanish implementations. Loads the list of words from
 * <code>/es/wikipedia.txt</code>.
 *
 * @author (adapted from Portuguese version by David Méndez)
 * @since 4.1
 */
public class SpanishWikipediaRule extends AbstractSimpleReplaceRule2 {

  public static final String WIKIPEDIA_COMMON_ERRORS = "ES_WIKIPEDIA_COMMON_ERRORS";
  // GTODO private static final String FILE_NAME = "/es/wikipedia.txt";
  // GTODO private static final Locale ES_LOCALE = new Locale("es");  // locale used on case-conversion

  public SpanishWikipediaRule(ResourceBundle messages, List<Map<String, String>> wikipediaWords, CaseConverter caseCon) throws Exception {
    super(messages, wikipediaWords, caseCon);
    super.setCategory(Categories.WIKIPEDIA.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>a basto</marker>"),
                   Example.fixed("<marker>abasto</marker>"));
  }

  @Override
  public final String getId() {
    return WIKIPEDIA_COMMON_ERRORS;
  }

  @Override
  public String getDescription() {
    return "Errores frecuentes en los artículos de la Wikipedia";
  }

  @Override
  public String getShort() {
    return "Error gramatical u ortográfico";
  }

  @Override
  public String getSuggestion() {
    return " es una expresión errónea. Pruebe a utilizar ";
  }

  @Override
  public String getSuggestionsSeparator() {
    return " o ";
  }

  @Override
  public URL getUrl() {
    return Tools.getUrl("https://es.wikipedia.org/wiki/Wikipedia:Lista_de_errores_ortogr%C3%A1ficos_comunes/M%C3%A1quinas");
  }

}
