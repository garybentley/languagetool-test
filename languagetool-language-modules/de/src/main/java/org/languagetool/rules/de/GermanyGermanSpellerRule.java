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
package org.languagetool.rules.de;

import org.languagetool.UserConfig;
import org.languagetool.language.SwissGerman;
import org.languagetool.rules.spelling.CachingWordListLoader;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.tagging.Tagger;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.language.GermanyGerman;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.List;

public class GermanyGermanSpellerRule extends GermanSpellerRule {

    public static final String RULE_ID = "GERMANY_GERMAN_SPELLER_RULE";
    public static final String ALT_RULE_ID = createAltRuleId(GermanyGerman.LOCALE, GermanSpellerRule.RULE_ID);

  public GermanyGermanSpellerRule(ResourceBundle messages, GermanyGerman language, MorfologikMultiSpeller morfoSpeller, UserConfig userConfig,
                Hunspell.Dictionary hunspellDict, Tagger tagger, Synthesizer synthesizer, Tokenizer strictTokenizer,
                CompoundWordTokenizer nonStrictTokenizer, List<String> ignoreWords, List<String> prohibitedWords, LineExpander lineExpander) throws Exception {
      super(messages, language, morfoSpeller, userConfig, hunspellDict, tagger, synthesizer, strictTokenizer, nonStrictTokenizer, ignoreWords, prohibitedWords, lineExpander);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

}
