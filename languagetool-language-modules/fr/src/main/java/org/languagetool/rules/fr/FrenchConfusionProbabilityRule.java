/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.languagetool.language.French;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Example;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionSet;

import java.util.Map;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @since 3.1
 */
public class FrenchConfusionProbabilityRule extends ConfusionProbabilityRule {

    public FrenchConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, French language, Map<String,List<ConfusionSet>> confusionSets) {
        super(messages, languageModel, language, confusionSets);
        addExamplePair(Example.wrong("Friedman résume cela en écrivant que le système de <marker>pris</marker> libres remplit trois fonctions.<marker>"),
                       Example.fixed("Friedman résume cela en écrivant que le système de <marker>prix</marker> libres remplit trois fonctions.<marker>"));
  }

}
