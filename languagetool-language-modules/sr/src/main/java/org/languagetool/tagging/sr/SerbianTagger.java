/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.sr;

import morfologik.stemming.Dictionary;

import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.rules.patterns.CaseConverter;

import java.util.Locale;

/** @since 4.0 */
public class SerbianTagger extends BaseTagger {

  // GTODO protected static final String BASE_DICTIONARY_PATH = "/sr/dictionary";
  // GTODO protected static final String EKAVIAN_DICTIONARY_PATH = BASE_DICTIONARY_PATH + "/ekavian/";

// GTODO REmove this class, no longer needed.

    public SerbianTagger(Dictionary baseDict, WordTagger tagger, CaseConverter caseCon) {
        super(baseDict, tagger, caseCon, true);
    }

/*
  @Override
  public String getManualAdditionsFileName() {
    return "/sr/dictionary/added.txt";
  }
  */
}
