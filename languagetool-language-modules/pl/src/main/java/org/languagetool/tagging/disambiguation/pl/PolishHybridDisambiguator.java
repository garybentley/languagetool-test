/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.disambiguation.pl;

import java.io.IOException;

import org.languagetool.AnalyzedSentence;
import org.languagetool.language.Polish;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * Hybrid chunker-disambiguator for Polish.
 *
 * @author Marcin Miłkowski
 */

public class PolishHybridDisambiguator extends AbstractDisambiguator<PolishResourceDataBroker> {

  private final Disambiguator chunker;
  private final Disambiguator disambiguator;

  public PolishHybridDisambiguator (PolishResourceDataBroker dataBroker) {
      super(dataBroker);
      this.chunker = dataBroker.getChunker();
      this.disambiguate = dataBroker.getDisambiguator();
      /*
      GTODO: Clean up
      this.chunker = new MultiWordChunker("/pl/multiwords.txt", dataBroker);
      this.disambiguator = new XmlRuleDisambiguator(new Polish());
      */
  }

  /**
   * Calls two disambiguator classes: (1) a chunker; (2) a rule-based
   * disambiguator.
   */
  @Override
  public final AnalyzedSentence disambiguate(AnalyzedSentence input)
      throws IOException {
    return chunker.disambiguate(disambiguator.disambiguate(input));
  }

}
