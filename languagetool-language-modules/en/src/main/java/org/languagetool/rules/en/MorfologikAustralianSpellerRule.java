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

package org.languagetool.rules.en;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.List;
import java.util.Set;

import org.languagetool.language.AustralianEnglish;
import org.languagetool.UserConfig;

import morfologik.stemming.Dictionary;
import org.languagetool.synthesis.Synthesizer;

public final class MorfologikAustralianSpellerRule extends AbstractEnglishSpellerRule {

    // GTODO: Clean up
  // GTODO private static final String RESOURCE_FILENAME = "/en/hunspell/en_AU.dict";
  // GTODO private static final String LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT = "en/hunspell/spelling_en-AU.txt";

  public MorfologikAustralianSpellerRule(ResourceBundle messages,
                                         AustralianEnglish language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords, List<String> prohibitedWords, Synthesizer synthesizer) throws Exception {
    super(messages, language, userConfig, dictionaries, ignoreWords, prohibitedWords, synthesizer);
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
    return "MORFOLOGIK_RULE_EN_AU";
  }
/*
GTODO Clean up
  @Override
  public String getLanguageVariantSpellingFileName() {
    return LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT;
  }
  */
}
