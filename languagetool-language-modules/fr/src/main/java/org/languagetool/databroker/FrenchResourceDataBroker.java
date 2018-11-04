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
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.rules.ConfusionSet;

public interface FrenchResourceDataBroker extends ResourceDataBroker {

    CompoundRuleData getCompounds() throws Exception;

    Map<String, List<ConfusionSet>> getConfusionSets() throws Exception;

    List<String> getSpellingProhibitedWords() throws Exception;

    List<String> getSpellingIgnoreWords() throws Exception;

    Hunspell.Dictionary getHunspellDictionary() throws Exception;

    Set<Dictionary> getDictionaries(UserConfig userConf) throws Exception;

    Dictionary getWordTaggerDictionary() throws Exception;

}
