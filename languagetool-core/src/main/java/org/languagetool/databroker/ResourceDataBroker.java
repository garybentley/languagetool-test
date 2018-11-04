/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.databroker;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.jetbrains.annotations.Nullable;
import biz.k11i.xgboost.Predictor;
import com.optimaize.langdetect.profiles.LanguageProfile;

import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.FalseFriendPatternRule;
import org.languagetool.rules.patterns.RuleFilterCreator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.rules.patterns.bitext.BitextPatternRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.Rule;
import org.languagetool.Language;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.chunking.Chunker;
import org.languagetool.rules.bitext.BitextRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.languagemodel.LanguageModel;


/**
 * Is responsible for getting the necessary resources for the grammar checker
 * library. Following directories are currently needed by a couple of classes:
 *
 * <ul style="list-type: circle">
 * <li>{@code /resource}</li>
 * <li>{@code /rules}</li>
 * </ul>
 *
 * This interface determines methods to obtain any contents from these
 * directories.
 * <p>
 *
 * GTODO Clean up
 * Make sure that you never obtain any grammar checker resources by calling
 * {@code Object.class.getResource(String)} or {@code
 * Object.class.getResourceAsStream(String)} directly. If you would like to
 * obtain something from these directories do always use
 * {@link JLanguageTool#getDataBroker()} which provides proper methods for
 * reading the directories above.
 * <p>
 *
 * For example, if you want to get the {@link URL} of {@code
 * /rules/de/grammar.xml} just invoke
 * {@link ResourceDataBroker#getFromRulesDirAsUrl(String)} and pass {@code
 * /de/grammar.xml} as a string. Note: The {@code /rules} directory's name isn't
 * passed, because its name might have changed. The same usage does apply for the
 * {@code /resource} directory.
 *
 * @author PAX
 * @since 1.0.1
 */
public interface ResourceDataBroker extends AutoCloseable {

  /**
   * @param className The fully qualified name of the class to load.
   * @return The requested class.
   * @throws ClassNotFoundException If the class cannot be loaded.
   */
  Class getClass(String className) throws ClassNotFoundException;

  Language getLanguage();

  /**
   * Get the message resource bundle to use for the language.
   */
  ResourceBundle getMessageBundle() throws Exception;

  /**
   * Get the model 2 vec model.  Can return null if not supported for the language.
   */
  @Nullable
  Word2VecModel getWord2VecModel() throws Exception;

  /**
   * Get the chunker.
   */
  Chunker getChunker() throws Exception;

  /**
   * Get the pattern rules.
   */
  List<AbstractPatternRule> getPatternRules() throws Exception;

  /**
   * Get the disambiguator.
   */
   @Nullable
   Disambiguator getDisambiguator() throws Exception;

   /**
    * Get the sentence tokenizer.
    */
   SentenceTokenizer getSentenceTokenizer() throws Exception;

   /**
    * Get the language profile.
    */
   LanguageProfile getLanguageProfile() throws Exception;

   /**
    * Get the bitext rules for the language.
    */
    List<BitextRule> getBitextRules() throws Exception;

   /**
    * Get the word tagger.
    */
    WordTagger getWordTagger() throws Exception;

    /**
     * Get the tagger.
     */
    Tagger getTagger() throws Exception;

    /**
     * Get the rule filter creator.
     */
    RuleFilterCreator getRuleFilterCreator() throws Exception;

    /**
     * Get the false friend rules applicable between the language this broker is handling and the passed in language.
     */
    List<FalseFriendPatternRule> getFalseFriendPatternRules(Language otherLanguage) throws Exception;

    /**
     * Get the word tokenizer.
     */
    WordTokenizer getWordTokenizer() throws Exception;

    /**
     * Get the case converter.
     */
    CaseConverter getCaseConverter() throws Exception;

    /**
     * Get the synthesizer.
     */
    Synthesizer getSynthesizer() throws Exception;

    /**
     * Get the language model.
     */
    @Nullable
    LanguageModel getLanguageModel() throws Exception;

    /**
     * Get the Predictor used to make predictions about the text for the specified rule, can return null
     * if no predictor exists for the rule.
     */
    @Nullable
    Predictor getRulePredictor(Rule rule) throws Exception;

}
