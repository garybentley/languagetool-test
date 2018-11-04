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
import org.languagetool.language.AustrianGerman;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.tokenizers.Tokenizer;
import morfologik.stemming.Dictionary;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.rules.spelling.hunspell.Hunspell;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.CompoundWordTokenizer;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.List;

/**
 * @since 3.9
 */
public class AustrianGermanSpellerRule extends GermanSpellerRule {

    public static final String RULE_ID = "AUSTRIAN_GERMAN_SPELLER_RULE";
    public static final String ALT_RULE_ID = createAltRuleId(AustrianGerman.LOCALE, GermanSpellerRule.RULE_ID);

/*
GTODO Clean up
  private final CachingWordListLoader wordListLoader = new CachingWordListLoader();
  private static final String LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT = "de/hunspell/spelling-de-AT.txt";
*/
  public AustrianGermanSpellerRule(ResourceBundle messages, AustrianGerman language, MorfologikMultiSpeller morfoSpeller, UserConfig userConfig,
                Hunspell.Dictionary hunspellDict, Tagger tagger, Synthesizer synthesizer, Tokenizer strictTokenizer,
                CompoundWordTokenizer nonStrictTokenizer, List<String> ignoreWords, List<String> prohibitedWords, LineExpander lineExpander) throws Exception {
      super(messages, language, morfoSpeller, userConfig, hunspellDict, tagger, synthesizer, strictTokenizer, nonStrictTokenizer, ignoreWords, prohibitedWords, lineExpander);
  }
/*
GTODO Clean up
  public AustrianGermanSpellerRule(ResourceBundle messages, German language) {
    this(messages, language, null);
  }
*/
  /**
   * @since 4.2
   */
   /*
   GTODO Clean up
  public AustrianGermanSpellerRule(ResourceBundle messages, German language, UserConfig userConfig) {
    super(messages, language, userConfig, LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT);
  }
*/
  @Override
  public String getId() {
    return RULE_ID;
  }
/*
GTODO Clean up
  @Override
  protected void init() throws IOException {
    super.init();
    for (String ignoreWord : wordListLoader.loadWords("/de/hunspell/spelling-de-AT.txt")) {
      addIgnoreWords(ignoreWord);
    }
  }
*/
/*
GTODO Clean up
  @Override
  public String getLanguageVariantSpellingFileName() {
    return LANGUAGE_SPECIFIC_PLAIN_TEXT_DICT;
  }
  */
}
