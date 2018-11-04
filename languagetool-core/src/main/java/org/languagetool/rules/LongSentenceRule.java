/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Michael Bryant
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
package org.languagetool.rules;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.UserConfig;

/**
 * A rule that warns on long sentences. Note that this rule is off by default.
 */
public class LongSentenceRule extends Rule {

  public static final String RULE_ID = "TOO_LONG_SENTENCE";
  public static final String RULE_MATCH_MESSAGE_ID = "long_sentence_rule_msg2";
  public static final String RULE_DESCRIPTION_MESSAGE_ID = "long_sentence_rule_desc";

  private static final int DEFAULT_MAX_WORDS = 50;
  private static final Pattern NON_WORD_REGEX = Pattern.compile("[.?!…:;,~’'\"„“”»«‚‘›‹()\\[\\]\\-–—*×∗·+÷/=]");

  protected int maxWords = DEFAULT_MAX_WORDS;

  public static final RuleConfiguration RULE_CONFIGURATION;

  static {
      RULE_CONFIGURATION = new RuleConfiguration(LongSentenceRule.class, RULE_ID, RuleConfiguration.newDescription(RULE_DESCRIPTION_MESSAGE_ID, Integer.class),
                    RuleConfiguration.newMatch(RULE_MATCH_MESSAGE_ID, Integer.class));
  }

  public static RuleConfiguration getRuleConfiguration() {
      return RULE_CONFIGURATION;
  }

  /**
   * @since 4.2
   */
  public LongSentenceRule(ResourceBundle messages, int defaultWords, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    if (!defaultActive) {
      setDefaultOff();
    }
    this.maxWords = (defaultWords > 0 ? defaultWords : DEFAULT_MAX_WORDS);
    setLocQualityIssueType(ITSIssueType.Style);
  }

  /**
   * Creates a rule with default inactive
   * @since 4.2
   */
  public LongSentenceRule(ResourceBundle messages, int defaultWords) {
    this(messages, defaultWords, false);
  }


  /**
   * Creates a rule with default values.
   * @since 4.2
   */
  public LongSentenceRule(ResourceBundle messages) {
    this(messages, DEFAULT_MAX_WORDS);
  }

  @Override
  public String getDescription() {
    return MessageFormat.format(messages.getString(RULE_DESCRIPTION_MESSAGE_ID), maxWords);
  }

  /**
   * Override this ID by adding a language acronym (e.g. TOO_LONG_SENTENCE_DE)
   * to use adjustment of maxWords by option panel
   * @since 4.1
   */
  @Override
  public String getId() {
    return RULE_ID;
  }

  /*
   * get maximal Distance of words in number of sentences
   * @since 4.1
   */
  @Override
  public int getDefaultValue() {
    return maxWords;
  }

  /**
   * @since 4.2
   */
  @Override
  public boolean hasConfigurableValue() {
    return true;
  }

  /**
   * @since 4.2
   */
  @Override
  public int getMinConfigurableValue() {
      // GTODO Sort this, never used, arbitrary...
    return 5;
  }

  /**
   * @since 4.2
   */
  @Override
  public int getMaxConfigurableValue() {
      // GTODO Sort this, never used, arbitrary...
    return 100;
  }

  /**
   * @since 4.2
   */
  @Override
  public String getConfigureText() {
      // GTODO This shouldn't be here, maybe use a "RuleConfiguration" object... or return json/xml that contains the relevant data.
    return messages.getString("guiLongSentencesText");
  }

  public String getMessage() {
      // GTODO Not very useful since it doesn't tell you how long the sentence is in words...
		return MessageFormat.format(messages.getString(RULE_MATCH_MESSAGE_ID), maxWords);
  }

  public void setMaxWords(int v) {
      if (v < 1) {
          throw new IllegalArgumentException("Value must be greater than 0.");
      }
      this.maxWords = v;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String msg = getMessage();
    if (tokens.length < maxWords + 1) {   // just a short-circuit
      return toRuleMatchArray(ruleMatches);
    } else {
      int numWords = 0;
      int startPos = 0;
      int prevStartPos;
      for (AnalyzedTokenReadings aToken : tokens) {
        String token = aToken.getToken();
        if (!aToken.isSentenceStart() && !aToken.isSentenceEnd() && !NON_WORD_REGEX.matcher(token).matches()) {
          numWords++;
          prevStartPos = startPos;
          startPos = aToken.getStartPos();
          if (numWords > maxWords) {
            RuleMatch ruleMatch = new RuleMatch(this, sentence, prevStartPos, aToken.getEndPos(), msg);
            ruleMatches.add(ruleMatch);
            break;
          }
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
