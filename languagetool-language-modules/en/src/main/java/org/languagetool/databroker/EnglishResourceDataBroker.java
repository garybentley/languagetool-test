/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Gary Bentley
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

import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.ResourceBundle;

import morfologik.stemming.Dictionary;
import org.languagetool.language.English;
import org.languagetool.UserConfig;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.synthesis.en.EnglishSynthesizer;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.chunking.EnglishChunker;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.tagging.en.EnglishTagger;

public interface EnglishResourceDataBroker extends ResourceDataBroker {
    Map<String, List<String>> getWrongWords() throws Exception;
    Map<String, List<String>> getContractionWrongWords() throws Exception;
    CompoundRuleData getCompounds() throws Exception;
    Set<Dictionary> getDictionaries(UserConfig config) throws Exception;
    List<ContextWords> getWrongWordsInContext() throws Exception;
    Dictionary getWordTaggerDictionary() throws Exception;
    Map<String, String> getCoherencyMappings() throws Exception;
    Map<String, List<ConfusionSet>> getConfusionSets() throws Exception;
    List<NeuralNetworkRule> createNeuralNetworkRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception;

    /**
     * Get the spelling ignore words.
     */
    List<String> getSpellingIgnoreWords() throws Exception;

    List<String> getSpellingProhibitedWords() throws Exception;

    List<PatternRule> getCompoundPatternRules(String message) throws Exception;

    /**
     * Get the list of words that require a 'A' rather than 'AN'.
     *
     * @return The list of words.
     */
    Set<String> getRequiresAWords() throws Exception;

    /**
     * Get the list of words that require a 'AN' rather than 'A'.
     *
     * @return The list of words.
     */
    Set<String> getRequiresANWords() throws Exception;
}
