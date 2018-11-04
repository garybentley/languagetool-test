/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.language.English;
import org.languagetool.UserConfig;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;
import org.languagetool.synthesis.Synthesizer;
import morfologik.stemming.Dictionary;

import java.util.Set;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;

public abstract class AbstractEnglishSpellerRule extends MorfologikSpellerRule {

  private final Synthesizer synthesizer;
/*
GTODO If used will cause an NPE, clean up
  public AbstractEnglishSpellerRule(ResourceBundle messages, English language) throws Exception {
    this(messages, language, null, null, null, null, null);
  }
*/
  /**
   * @since 4.2
   */
   // GTODO Aim to get rid of "English language" in the constructor...  Its not used in this class, only our superclass.
  public AbstractEnglishSpellerRule(ResourceBundle messages, English language, UserConfig userConfig, Set<Dictionary> dictionaries, List<String> ignoreWords, List<String> prohibitedWords, Synthesizer synthesizer) throws Exception {
    super(messages, language, userConfig, dictionaries, ignoreWords, prohibitedWords);
    this.synthesizer = synthesizer;
    //GTODO: synthesizer = new EnglishSynthesizer(language.getUseDataBroker());
    setCheckCompound(true);
    addExamplePair(Example.wrong("This <marker>sentenc</marker> contains a spelling mistake."),
                   Example.fixed("This <marker>sentence</marker> contains a spelling mistake."));
                   /*
                   GTODO Clean up
    String languageSpecificIgnoreFile = getSpellingFileName().replace(".txt", "_"+language.getShortCodeWithCountryAndVariant()+".txt");
    for (String ignoreWord : wordListLoader.loadWords(languageSpecificIgnoreFile)) {
      addIgnoreWords(ignoreWord);
    }
    */
  }

  @Override
  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar) throws Exception {
    List<RuleMatch> ruleMatches = super.getRuleMatches(word, startPos, sentence, ruleMatchesSoFar);
    if (ruleMatches.size() > 0) {
      // so 'word' is misspelled:
      IrregularForms forms = getIrregularFormsOrNull(word);
      if (forms != null) {
        RuleMatch oldMatch = ruleMatches.get(0);
        RuleMatch newMatch = new RuleMatch(this, sentence, oldMatch.getFromPos(), oldMatch.getToPos(),
                "Possible spelling mistake. Did you mean <suggestion>" + forms.forms.get(0) +
                "</suggestion>, the " + forms.formName + " form of the " + forms.posName +
                " '" + forms.baseform + "'?");
        List<String> allSuggestions = new ArrayList<>();
        allSuggestions.addAll(forms.forms);
        for (String repl : oldMatch.getSuggestedReplacements()) {
          if (!allSuggestions.contains(repl)) {
            allSuggestions.add(repl);
          }
        }
        newMatch.setSuggestedReplacements(allSuggestions);
        ruleMatches.set(0, newMatch);
      }
    }
    return ruleMatches;
  }

  @Nullable
  private IrregularForms getIrregularFormsOrNull(String word) throws Exception {
    IrregularForms irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("ed"), "VBD", "verb", "past tense");
    if (irregularFormsOrNull != null) {
        return irregularFormsOrNull;
    }
    irregularFormsOrNull = getIrregularFormsOrNull(word, "ed", Arrays.asList("d" /* e.g. awaked */), "VBD", "verb", "past tense");
    if (irregularFormsOrNull != null) {
        return irregularFormsOrNull;
    }
    irregularFormsOrNull = getIrregularFormsOrNull(word, "s", Arrays.asList("s"), "NNS", "noun", "plural");
    if (irregularFormsOrNull != null) {
        return irregularFormsOrNull;
    }
    irregularFormsOrNull = getIrregularFormsOrNull(word, "es", Arrays.asList("es"/* e.g. 'analysises' */), "NNS", "noun", "plural");
    if (irregularFormsOrNull != null) {
        return irregularFormsOrNull;
    }
    irregularFormsOrNull = getIrregularFormsOrNull(word, "er", Arrays.asList("er"/* e.g. 'farer' */), "JJR", "adjective", "comparative");
    if (irregularFormsOrNull != null) {
        return irregularFormsOrNull;
    }
    irregularFormsOrNull = getIrregularFormsOrNull(word, "est", Arrays.asList("est"/* e.g. 'farest' */), "JJS", "adjective", "superlative");
    return irregularFormsOrNull;
  }

  @Nullable
  private IrregularForms getIrregularFormsOrNull(String word, String wordSuffix, List<String> suffixes, String posTag, String posName, String formName) throws Exception {
      for (String suffix : suffixes) {
        if (word.endsWith(wordSuffix)) {
          String baseForm = word.substring(0, word.length() - suffix.length());
          String[] forms = synthesizer.synthesize(new AnalyzedToken(word, null, baseForm), posTag);
          List<String> result = new ArrayList<>();
          for (String form : forms) {
            if (!speller1.isMisspelled(form)) {
              // only accept suggestions that the spellchecker will accept
              result.add(form);
            }
          }
          // the internal dict might contain forms that the spell checker doesn't accept (e.g. 'criterions'),
          // but we trust the spell checker in this case:
          result.remove(word);
          result.remove("badder");  // non-standard usage
          result.remove("baddest");  // non-standard usage
          result.remove("spake");  // can be removed after dict update
          if (result.size() > 0) {
            return new IrregularForms(baseForm, posName, formName, result);
          }
        }
      }
      return null;
  }

  /**
   * @since 2.7
   */
  @Override
  protected List<String> getAdditionalTopSuggestions(List<String> suggestions, String word) throws Exception {
    if ("Alot".equals(word)) {
      return Arrays.asList("A lot");
    } else if ("alot".equals(word)) {
      return Arrays.asList("a lot");
    } else if ("thru".equals(word)) {
      return Arrays.asList("through");
    } else if ("speach".equals(word)) {  // the replacement pairs would prefer "speak"
      return Arrays.asList("speech");
    } else if ("icecreem".equals(word)) {
      return Arrays.asList("ice cream");
    } else if ("fora".equals(word)) {
      return Arrays.asList("for a");
    } else if ("te".equals(word)) {
      return Arrays.asList("the");
    } else if ("todays".equals(word)) {
      return Arrays.asList("today's");
    }
    return super.getAdditionalTopSuggestions(suggestions, word);
  }

  private static class IrregularForms {
    final String baseform;
    final String posName;
    final String formName;
    final List<String> forms;
    private IrregularForms(String baseform, String posName, String formName, List<String> forms) {
      this.baseform = baseform;
      this.posName = posName;
      this.formName = formName;
      this.forms = forms;
    }
  }
}
