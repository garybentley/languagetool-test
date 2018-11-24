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
package org.languagetool.rules.es;

import org.languagetool.language.Spanish;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Example;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
import org.languagetool.rules.ConfusionSet;

import java.util.ResourceBundle;
import java.util.Map;
import java.util.List;

/**
 * @since 3.1
 */
public class SpanishConfusionProbabilityRule extends ConfusionProbabilityRule {

    public SpanishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Spanish language, Map<String,List<ConfusionSet>> confusionSets) {
      this(messages, languageModel, language, 3, confusionSets);
    }

    public SpanishConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Spanish language, int grams, Map<String,List<ConfusionSet>> confusionSets) {
      super(messages, languageModel, language, grams, confusionSets);
      addExamplePair(Example.wrong("El proyecto no <marker>tubo</marker> una buena acogida."),
                     Example.fixed("El proyecto no <marker>tuvo</marker> una buena acogida."));
  }

}
