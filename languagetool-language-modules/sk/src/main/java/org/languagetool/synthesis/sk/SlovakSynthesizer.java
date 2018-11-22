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
package org.languagetool.synthesis.sk;

import java.util.Set;
import java.util.ResourceBundle;

import morfologik.stemming.IStemmer;

import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * Slovak word form synthesizer.
 * @author Marcin Miłkowski
 */
public class SlovakSynthesizer extends BaseSynthesizer {

  // GTODO private static final String RESOURCE_FILENAME = "/sk/slovak_synth.dict";
  // GTODO private static final String TAGS_FILE_NAME = "/sk/slovak_tags.txt";

  public SlovakSynthesizer(ResourceBundle messages, IStemmer stemmer, Set<String> tags) {
      super(stemmer, tags);
  }
}
