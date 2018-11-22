/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sr.ekavian;

import org.languagetool.language.SerbianSerbian;
import org.languagetool.UserConfig;
import org.languagetool.rules.Example;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import morfologik.stemming.Dictionary;

import java.util.List;
import java.util.Set;
import java.util.ResourceBundle;


/** @since 4.0 */
public class MorfologikEkavianSpellerRule extends MorfologikSpellerRule {

  public static final String RULE_ID = "MORFOLOGIK_RULE_SR_EKAVIAN";

  // GTODO private static final String BASE_DICTIONARY_PATH = "/sr/dictionary/ekavian/";

  public MorfologikEkavianSpellerRule(ResourceBundle messages, SerbianSerbian language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords, List<String> prohibitedWords) throws Exception {
    super(messages, language, userConfig, dictionaries, ignoreWords, prohibitedWords);
      addExamplePair(
            Example.wrong("Изгубила све сам <marker>бткие</marker>, ал' још водим рат."),
            Example.fixed("Изгубила све сам <marker>битке</marker>, ал' још водим рат.")
    );
  }
/*
GTODO
  @Override
  public String getFileName() {
    return BASE_DICTIONARY_PATH + "serbian.dict";
  }
*/
  @Override
  public String getId() {
    return RULE_ID;
  }
/*
GTODO
  @Override
  public String getSpellingFileName() {
    return BASE_DICTIONARY_PATH + "spelling.txt";
  }

  @Override
  // File with ignored words
  public String getIgnoreFileName() {
    return BASE_DICTIONARY_PATH + "ignored.txt";
  }

  @Override
  public String getProhibitFileName() {
    return BASE_DICTIONARY_PATH + "prohibit.txt";
  }
  */
}
