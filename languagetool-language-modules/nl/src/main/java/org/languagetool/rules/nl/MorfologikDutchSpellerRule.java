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

package org.languagetool.rules.nl;

import java.util.List;
import java.util.Set;

import morfologik.stemming.Dictionary;

import org.languagetool.language.Dutch;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.util.ResourceBundle;

public final class MorfologikDutchSpellerRule extends MorfologikSpellerRule {

  public MorfologikDutchSpellerRule(ResourceBundle messages, Dutch language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords, List<String> prohibitedWords) throws Exception {
      super(messages, language, userConfig, dictionaries, ignoreWords, prohibitedWords);
  }
/*
GTODO CLean up
  @Override
  public String getFileName() {
    return "/nl/spelling/nl_NL.dict";
  }
*/
  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_NL_NL";
  }
/*
GTODO Clean up
  @Override
  protected String getIgnoreFileName() {
    return "/nl/spelling/ignore.txt";
  }

  @Override
  public String getSpellingFileName() {
    return "/nl/spelling/spelling.txt";
  }

  @Override
  protected String getProhibitFileName() {
    return "/nl/spelling/prohibit.txt";
  }
*/
}
