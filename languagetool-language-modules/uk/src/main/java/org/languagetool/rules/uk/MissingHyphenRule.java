/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.uk.PosTagHelper;

/**
 *
 * @author Andriy Rysin
 */
public class MissingHyphenRule extends Rule {

  private final Set<String> dashPrefixes;
  // GTODO  = ExtraDictionaryLoader.loadSet("/uk/dash_prefixes.txt");
  private static final Pattern ALL_LOWER = Pattern.compile("[а-яіїєґ'-]+");
  private WordTagger wordTagger;


  public MissingHyphenRule(ResourceBundle messages, WordTagger wordTagger, Set<String> dashPrefixes) throws Exception {
    super(messages);
    setLocQualityIssueType(ITSIssueType.Misspelling);
    this.wordTagger = Objects.requireNonNull(wordTagger, "Word tagger must be provided.");
    this.dashPrefixes = Objects.requireNonNull(dashPrefixes, "Dash prefixes must be provided.");

    // these two generate too many false positives
    this.dashPrefixes.remove("блок");
    this.dashPrefixes.remove("рейтинг");
    for(java.util.Iterator<String> it = this.dashPrefixes.iterator(); it.hasNext(); ) {
      if( ! ALL_LOWER.matcher(it.next()).matches() ) {
        it.remove();
      }
    }
  }

  @Override
  public final String getId() {
    return "UK_MISSING_HYPHEN";
  }

  @Override
  public String getDescription() {
    return "Пропущений дефіс";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws Exception {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i = 1; i < tokens.length-1; i++) {
      AnalyzedTokenReadings tokenReadings = tokens[i];
      AnalyzedTokenReadings nextTokenReadings = tokens[i + 1];

      boolean isCapitalized = Character.isUpperCase(tokenReadings.getToken().charAt(0));

      if (isInPrefixes(tokenReadings, isCapitalized)
          && PosTagHelper.hasPosTagPart(nextTokenReadings, "noun")
//          && ! PosTagHelper.hasPosTag(nextTokenReadings, Pattern.compile("^(?!noun).*"))
          && ALL_LOWER.matcher(nextTokenReadings.getToken()).matches() ) {

        String hyphenedWord = tokenReadings.getToken() + "-" + nextTokenReadings.getToken();
        String tokenToCheck = isCapitalized ? StringUtils.uncapitalize(hyphenedWord) : hyphenedWord;

        if ( wordTagger.tag(tokenToCheck).size() > 0 ) {
          RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, tokenReadings.getStartPos(), nextTokenReadings.getEndPos(), "Можливо, пропущено дефіс?", getDescription());
          potentialRuleMatch.setSuggestedReplacement(hyphenedWord);

          ruleMatches.add(potentialRuleMatch);
        }
      }

    }

    return ruleMatches.toArray(new RuleMatch[0]);
  }

  private boolean isInPrefixes(AnalyzedTokenReadings tokenReadings, boolean isCapitalized) {
    String token = tokenReadings.getToken();
    if( isCapitalized ) {
      token = StringUtils.uncapitalize(token);
    }
    return dashPrefixes.contains(token);
  }


}
