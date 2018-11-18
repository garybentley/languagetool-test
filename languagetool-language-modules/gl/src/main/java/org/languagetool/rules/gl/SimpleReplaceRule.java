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
package org.languagetool.rules.gl;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * A rule that matches words or phrases which should not be used and suggests
 * correct ones instead.
 *
 * @author Susana Sotelo
 *
 * Based on pl/SimpleReplaceRule.java
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  public static final String GL_SIMPLE_REPLACE_RULE = "GL_SIMPLE_REPLACE";

  //private Map<String, List<String>> wrongWords;
  //private static final Locale GL_LOCALE = new Locale("gl");
/*
GTODO
  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }
*/
  public SimpleReplaceRule(ResourceBundle messages, Map<String, List<String>> wrongWords, CaseConverter caseCon) {
      super(messages, wrongWords, caseCon);
  }
/*
GTODO
  public SimpleReplaceRule(ResourceBundle messages, ResourceDataBroker dataBroker) throws IOException {
    super(messages, dataBroker);
    wrongWords = load("/gl/words.txt", dataBroker);
  }
*/
  @Override
  public final String getId() {
    return GL_SIMPLE_REPLACE_RULE;
  }

  @Override
  public String getDescription() {
    return "Corrección de erros léxicos (barbarismos).";
  }

  @Override
  public String getShort() {
    return "Erros léxicos";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " non existe en galego. Talvez quería vostede dicir: "
        + String.join(", ", replacements) + ".";
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }
/*
GTODO
  @Override
  public Locale getLocale() {
    return GL_LOCALE;
  }
*/
}
