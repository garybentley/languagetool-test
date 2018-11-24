/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import morfologik.stemming.Dictionary;

import java.util.List;
import java.util.Set;
import java.util.Collections;

import java.util.ResourceBundle;

/**
 * @since 2.8
 */
public class MorfologikSpanishSpellerRule extends MorfologikSpellerRule {

    public MorfologikSpanishSpellerRule(ResourceBundle messages, Spanish language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords) throws Exception {
      super(messages, language, userConfig, dictionaries, ignoreWords, Collections.emptyList());
  }
/*
GTODO
  @Override
  public String getFileName() {
    return "/es/hunspell/es_ES.dict";
  }
*/
  @Override
  public final String getId() {
    return "MORFOLOGIK_RULE_ES";
  }
}
