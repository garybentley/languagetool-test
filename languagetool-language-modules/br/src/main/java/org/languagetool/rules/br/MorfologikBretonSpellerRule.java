/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.br;

import java.util.Set;
import java.util.List;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;

import org.languagetool.language.Breton;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikBretonSpellerRule extends MorfologikSpellerRule {

  //GTODO private static final String RESOURCE_FILENAME = "/br/hunspell/br_FR.dict";

  private static final Pattern BRETON_TOKENIZING_CHARS = Pattern.compile("-");

  public MorfologikBretonSpellerRule(ResourceBundle messages, Breton language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords) throws Exception {
      super(messages, language, userConfig, dictionaries, ignoreWords, Collections.emptyList());
      this.setIgnoreTaggedWords();
  }
/*
GTODO Clean up
  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }
*/
  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_BR_FR";
  }

  @Override
  public Pattern tokenizingPattern() {
    return BRETON_TOKENIZING_CHARS;
  }

}
