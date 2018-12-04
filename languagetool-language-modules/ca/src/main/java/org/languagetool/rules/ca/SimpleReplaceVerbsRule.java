/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;

/**
 * A rule that matches incorrect verbs (including all inflected forms) and
 * suggests correct ones instead.
 *
 * Loads the relevant words from <code>rules/ca/replace_verbs.txt</code>.
 *
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRule extends AbstractSimpleReplaceRule {

/*
GTODO
  @Override
  protected Map<String, List<String>> getWrongWords() {
      if (wrongWords == null) {
          wrongWords = load("/ca/replace_verbs.txt", dataBroker);
      }
    return wrongWords;
  }
*/
  private static final String endings = "a|à|ada|ades|am|ant|ar|ara|arà|aran|aràs|aré|arem|àrem|"
      + "aren|ares|areu|àreu|aria|aríem|arien|aries|aríeu|às|àssem|assen|asses|àsseu|àssim|assin|"
      + "assis|àssiu|at|ats|au|ava|àvem|aven|aves|àveu|e|em|en|es|és|éssem|essen|esses|ésseu|éssim|"
      + "essin|essis|éssiu|eu|i|í|in|is|o|ïs";
  private static final Pattern desinencies_1conj_0 = Pattern.compile("(.+?)(" + endings + ")");
  private static final Pattern desinencies_1conj_1 = Pattern.compile("(.+)("  + endings + ")");
  private Tagger tagger;
  private Synthesizer synth;

  public SimpleReplaceVerbsRule(final ResourceBundle messages, Map<String, List<String>> wrongWords, Tagger tagger, Synthesizer synthesizer, CaseConverter caseCon) {
    super(messages, wrongWords, caseCon);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    super.setIgnoreTaggedWords();
    this.tagger = Objects.requireNonNull(tagger, "Tagger must be provided.");
    this.synth = Objects.requireNonNull(synthesizer, "Synthesizer must be provided.");
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_VERBS";
  }

  @Override
  public String getDescription() {
    return "Detecta verbs incorrectes i proposa suggeriments de canvi";
  }

  @Override
  public String getShort() {
    return "Verb incorrecte";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Verb incorrecte.";
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (AnalyzedTokenReadings tokenReadings : tokens) {
      String originalTokenStr = tokenReadings.getToken();
      if (ignoreTaggedWords && tokenReadings.isTagged()) {
        continue;
      }
      String tokenString = getCaseConverter().toLowerCase(originalTokenStr);
      AnalyzedTokenReadings analyzedTokenReadings = null;
      String infinitive = null;
      int i = 0;
      while (i < 2 && analyzedTokenReadings == null) {
        Matcher m;
        if (i == 0) {
          m = desinencies_1conj_0.matcher(tokenString);
        } else {
          m = desinencies_1conj_1.matcher(tokenString);
        }
        if (m.matches()) {
          String lexeme = m.group(1);
          String desinence = m.group(2);
          if (desinence.startsWith("e") || desinence.startsWith("é")
              || desinence.startsWith("i") || desinence.startsWith("ï")) {
            if (lexeme.endsWith("c")) {
              lexeme = lexeme.substring(0, lexeme.length() - 1).concat("ç");
            } else if (lexeme.endsWith("qu")) {
              lexeme = lexeme.substring(0, lexeme.length() - 2).concat("c");
            } else if (lexeme.endsWith("g")) {
              lexeme = lexeme.substring(0, lexeme.length() - 1).concat("j");
            } else if (lexeme.endsWith("gü")) {
              lexeme = lexeme.substring(0, lexeme.length() - 2).concat("gu");
            } else if (lexeme.endsWith("gu")) {
              lexeme = lexeme.substring(0, lexeme.length() - 2).concat("g");
            }
          }
          if (desinence.startsWith("ï")) {
            desinence = "i" + desinence.substring(1, desinence.length());
          }
          infinitive = lexeme.concat("ar");
          if (containsWord(infinitive)) {
            List<String> wordAsArray = Arrays.asList("cant".concat(desinence));
            List<AnalyzedTokenReadings> analyzedTokenReadingsList = tagger.tag(wordAsArray);
            if (analyzedTokenReadingsList != null) {
              analyzedTokenReadings = analyzedTokenReadingsList.get(0);
            }
          }
        }
        i++;
      }

      // synthesize replacements
      if (analyzedTokenReadings != null) {
        List<String> possibleReplacements = new ArrayList<>();
        String[] synthesized = null;
        List<String> replacementInfinitives = getReplacements(infinitive);
        for (String replacementInfinitive : replacementInfinitives) {
          if (replacementInfinitive.startsWith("(")) {
            possibleReplacements.add(replacementInfinitive);
          } else {
            String[] parts = replacementInfinitive.split(" "); // the first part
                                                               // is the verb
            AnalyzedToken infinitiveAsAnTkn = new AnalyzedToken(parts[0],
                "V.*", parts[0]);
            for (AnalyzedToken analyzedToken : analyzedTokenReadings) {
                synthesized = synth.synthesize(infinitiveAsAnTkn,
                    analyzedToken.getPOSTag());
              for (String s : synthesized) {
                for (int j = 1; j < parts.length; j++) {
                  s = s.concat(" ").concat(parts[j]);
                }
                if (!possibleReplacements.contains(s)) {
                  possibleReplacements.add(s);
                }
              }
            }
          }
        }
        if (possibleReplacements.size() > 0) {
          RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings,
              possibleReplacements, sentence);
          ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
