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
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import org.languagetool.UserConfig;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.chunking.Chunker;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.rules.de.*;

public interface GermanResourceDataBroker extends ResourceDataBroker {

    Set<Pattern[]> getCaseRuleExceptionPatterns() throws Exception;

    Hunspell.Dictionary getHunspellDictionary() throws Exception;

    Set<Dictionary> getDictionaries(UserConfig config) throws Exception;

    Map<String, List<ConfusionSet>> getConfusionSets() throws Exception;

    Map<String, String> getCoherencyMappings() throws Exception;

    List<NeuralNetworkRule> createNeuralNetworkRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception;

    CompoundWordTokenizer getNonStrictCompoundSplitter() throws Exception;

    GermanCompoundTokenizer getStrictCompoundTokenizer() throws Exception;

    List<ContextWords> getWrongWordsInContext() throws Exception;

    CompoundRuleData getCompounds() throws Exception;

    @Override
    GermanTagger getTagger() throws Exception;

    /**
     * Get the spelling ignore words.
     */
    List<String> getSpellingIgnoreWords() throws Exception;

    List<String> getSpellingProhibitedWords() throws Exception;

    //@Override
    Chunker getPostDisambiguationChunker() throws Exception;

    List<SpellingRuleWithSuggestion> getOldSpellingRuleSuggestions() throws Exception;

}
