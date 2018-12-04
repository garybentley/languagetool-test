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

import java.util.List;
import java.util.Map;
import java.util.Set;

import morfologik.stemming.Dictionary;

import org.languagetool.UserConfig;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.ContextWords;
import org.languagetool.tagging.ca.CatalanTagger;
import org.languagetool.AnalyzedTokenReadings;

public interface CatalanResourceDataBroker extends ResourceDataBroker {

    Set<Dictionary> getDictionaries(UserConfig userConfig) throws Exception;

    List<String> getSpellingIgnoreWords() throws Exception;

    List<ContextWords> getWrongWordsInContext() throws Exception;

    List<ContextWords> getDiacriticWrongWordsInContext() throws Exception;

    Map<String, AnalyzedTokenReadings> getAccentuationRelevantWords1() throws Exception;

    Map<String, AnalyzedTokenReadings> getAccentuationRelevantWords2() throws Exception;

    Map<String, List<String>> getWrongWords() throws Exception;

    Map<String, List<String>> getDNVWrongWords() throws Exception;

    Map<String, List<String>> getDNVSecondaryWrongWords() throws Exception;

    Map<String, List<String>> getDNVColloquialWrongWords() throws Exception;

    Map<String, List<String>> getBalearicWrongWords() throws Exception;

    Map<String, List<String>> getVerbsWrongWords() throws Exception;

    Map<String, List<String>> getDiactriticsIECWrongWords() throws Exception;

    Map<String, List<String>> getDiactriticsTraditionalWrongWords() throws Exception;

    Map<String, List<String>> getOperationNameWrongWords() throws Exception;

    //CompoundRuleData getCompounds() throws Exception;

    //List<PatternRule> getCompoundPatternRules(String message) throws Exception;

    //@Override
    //CatalanTagger getTagger() throws Exception;

}
