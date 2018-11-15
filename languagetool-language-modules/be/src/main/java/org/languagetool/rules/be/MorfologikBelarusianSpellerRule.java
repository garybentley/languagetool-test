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

package org.languagetool.rules.be;

import java.util.Set;
import java.util.Collections;
import java.util.ResourceBundle;

import morfologik.stemming.Dictionary;

import org.languagetool.language.Belarusian;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

public final class MorfologikBelarusianSpellerRule extends MorfologikSpellerRule {

  // GTODO private static final String RESOURCE_FILENAME = "/be/hunspell/be_BY.dict";

  public MorfologikBelarusianSpellerRule(ResourceBundle messages, Belarusian language, UserConfig userConfig, Set<Dictionary> dictionaries) throws Exception {
    super(messages, language, userConfig, dictionaries, Collections.emptyList(), Collections.emptyList());
  }
/*
GTODO
  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }
*/
  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_BE_BY";
  }

}
