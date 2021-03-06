/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.English;
import org.languagetool.rules.PartialPosTagFilter;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.en.EnglishTagger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@link PartialPosTagFilter} for English that does not run the disambiguator.
 * @since 3.2
 * @see EnglishPartialPosTagFilter
 */
public class NoDisambiguationEnglishPartialPosTagFilter extends PartialPosTagFilter {

  private final EnglishTagger tagger;

  // GTODO private final Tagger tagger = new English().getTagger();

  public NoDisambiguationEnglishPartialPosTagFilter(EnglishTagger tagger) {
      this.tagger = tagger;
  }

  @Override
  protected List<AnalyzedTokenReadings> tag(String token) {
      List<AnalyzedTokenReadings> tags = tagger.tag(Collections.singletonList(token));
      AnalyzedTokenReadings[] atr = tags.toArray(new AnalyzedTokenReadings[tags.size()]);
      return Arrays.asList(atr);
  }
}
