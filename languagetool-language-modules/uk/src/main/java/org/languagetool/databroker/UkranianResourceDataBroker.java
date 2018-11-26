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

import org.languagetool.rules.ConfusionSet;
import org.languagetool.UserConfig;
import org.languagetool.tagging.WordTagger;
import org.languagetool.tagging.disambiguation.uk.SimpleDisambiguator;
import org.languagetool.rules.uk.*;

public interface UkranianResourceDataBroker extends ResourceDataBroker {

    List<String> getSpellingIgnoreWords() throws Exception;

    Set<Dictionary> getDictionaries(UserConfig userConfig) throws Exception;

    WordTagger getWordTagger() throws Exception;

    Map<String, List<String>> getWrongWords() throws Exception;

    Map<String, List<String>> getSoftWrongWords() throws Exception;

    Map<String, List<String>> getRenamedWrongWords() throws Exception;

    Set<String> getDashPrefixes() throws Exception;

    Set<String> getDashSlaves() throws Exception;

    Set<String> getDashLeftMasters() throws Exception;

    CaseGovernmentHelper getCaseGovernmentHelper() throws Exception;

    TokenAgreementNounVerbExceptionHelper getTokenAgreementNounVerbExceptionHelper() throws Exception;

    TokenAgreementAdjNounExceptionHelper getTokenAgreementAdjNounExceptionHelper() throws Exception;

    Map<String, SimpleDisambiguator.TokenMatcher> getDisambiguatorRemoveMappings() throws Exception;

}
