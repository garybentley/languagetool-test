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
package org.languagetool.synthesis.ro;

import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.ResourceBundle;

import morfologik.stemming.IStemmer;

import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ManualSynthesizer;

/**
 * Romanian word form synthesizer.
 *
 * @author Ionuț Păduraru
 */
public class RomanianSynthesizer extends BaseSynthesizer {

  // GTODO private static final String RESOURCE_FILENAME = "/ro/romanian_synth.dict";
  // GTODO private static final String TAGS_FILE_NAME = "/ro/romanian_tags.txt";
  // GTODO private static final String USER_DICT_FILENAME = "/ro/added.txt";

  private ManualSynthesizer manualSynthesizer;

  public RomanianSynthesizer(ResourceBundle messages, IStemmer stemmer, Set<String> tags, ManualSynthesizer synthesizer) {
      super(stemmer, tags);
      manualSynthesizer = Objects.requireNonNull(synthesizer, "Synthesizer must be provided.");
  }

  @Override
  protected void lookup(String lemma, String posTag, List<String> results) {
    super.lookup(lemma, posTag, results);
    // add words that are missing from the romanian_synth.dict file
    final List<String> manualForms = manualSynthesizer.lookup(lemma, posTag);
    if (manualForms != null) {
      results.addAll(manualForms);
    }
  }

/*
GTODO
  @Override
  protected void initPossibleTags() throws IOException {
    super.initPossibleTags();
    initSynth();
    // add any possible tag from manual synthesiser
    for (String tag : manualSynthesizer.getPossibleTags()) {
      if (!possibleTags.contains(tag)) {
        possibleTags.add(tag);
      }
    }
  }
*/
/*
GTODO
  private synchronized void initSynth() {
    if (manualSynthesizer == null) {
      try {
        try (InputStream stream = dataBroker.getFromResourceDirAsStream(USER_DICT_FILENAME)) {
          manualSynthesizer = new ManualSynthesizer(stream);
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
  */
}
