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

package org.languagetool.rules.uk;

import java.util.Set;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Collections;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;

import org.languagetool.*;
import org.languagetool.UserConfig;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.spelling.morfologik.MorfologikMultiSpeller;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.tagging.uk.IPOSTag;

public final class MorfologikUkrainianSpellerRule extends MorfologikSpellerRule {

  private static final String ABBREVIATION_CHAR = ".";
  // GTODO private static final String RESOURCE_FILENAME = "/uk/hunspell/uk_UA.dict";
  private static final Pattern UKRAINIAN_LETTERS = Pattern.compile(".*[а-яіїєґА-ЯІЇЄҐ].*");
  private static final Pattern DO_NOT_SUGGEST_SPACED_PATTERN = Pattern.compile(
        "(авіа|авто|анти|аудіо|відео|водо|гідро|екстра|квазі|кіно|лже|мета|моно|мото|псевдо|пост|радіо|стерео|супер|ультра|фото) .*");


  public MorfologikUkrainianSpellerRule(ResourceBundle messages, Ukrainian language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords) throws Exception {
      super(messages, language, userConfig, dictionaries, ignoreWords, Collections.emptyList());
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_UK_UA";
  }

  @Override
  protected boolean isMisspelled(MorfologikMultiSpeller speller, String word) {
    if( word.endsWith("-") )
      return true;

    if( word.endsWith("²") || word.endsWith("³") ) {
      word = word.substring(0, word.length() - 1);
    }

    return super.isMisspelled(speller, word);
  }

  @Override
  protected boolean ignoreToken(AnalyzedTokenReadings[] tokens, int idx) throws Exception {
    String word = tokens[idx].getToken();

    // don't check words that don't have Ukrainian letters
    if( ! UKRAINIAN_LETTERS.matcher(word).matches() )
      return true;

    if( super.ignoreToken(tokens, idx) )
      return true;

    if( idx < tokens.length - 1 && tokens[idx+1].getToken().equals(ABBREVIATION_CHAR) ) {
      if( super.ignoreWord(word + ABBREVIATION_CHAR) ) {
        return true;
      }
      if( word.matches("[А-ЯІЇЄҐ]") ) {  //TODO: only do this for initials when last name is followed
        return true;
      }
    }

    if( word.contains("-") || word.contains("\u2011") || word.endsWith(".")
            || word.equalsIgnoreCase("раза") ) {
      return hasGoodTag(tokens[idx]);
    }

    return false;
  }

  private boolean hasGoodTag(AnalyzedTokenReadings tokens) {
    for (AnalyzedToken analyzedToken : tokens) {
      String posTag = analyzedToken.getPOSTag();
      if( posTag != null
            && ! posTag.equals(JLanguageTool.SENTENCE_START_TAGNAME)
            && ! posTag.equals(JLanguageTool.SENTENCE_END_TAGNAME)
            && ! posTag.contains(IPOSTag.bad.getText())
            && ! (posTag.contains(":inanim") && posTag.contains(":v_kly")) )
        return true;
    }
    return false;
  }

  @Override
  protected void filterSuggestions(List<String> suggestions) {
    super.filterSuggestions(suggestions);

    // do not suggest "кіно прокат, вело- прогулянка..."
    for (Iterator<String> iterator = suggestions.iterator(); iterator.hasNext();) {
      String item = iterator.next();
      if( item.contains(" ") && DO_NOT_SUGGEST_SPACED_PATTERN.matcher(item).matches()
              || item.contains("- ") ) {
        iterator.remove();
      }
    }
  }

}
