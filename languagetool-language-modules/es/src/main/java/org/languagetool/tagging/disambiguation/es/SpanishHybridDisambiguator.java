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

package org.languagetool.tagging.disambiguation.es;

import java.util.Objects;

import org.languagetool.AnalyzedSentence;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;
import org.languagetool.tagging.disambiguation.Disambiguator;

/**
 * Hybrid chunker-disambiguator for Spanish
 *
 * @author Marcin Miłkowski
 */
public class SpanishHybridDisambiguator extends AbstractDisambiguator {

    private final Disambiguator chunker;
    // GTODO = new MultiWordChunker("/es/multiwords.txt");
    private final Disambiguator disambiguator;
    // GTODO  = new XmlRuleDisambiguator(new Spanish());

    public SpanishHybridDisambiguator (Disambiguator chunker, Disambiguator disambiguator) {
        this.chunker = Objects.requireNonNull(chunker, "Chunker must be provided.");
        this.disambiguator = Objects.requireNonNull(disambiguator, "Disambiguator must be provided.");
    }

    /**
     * Calls two disambiguator classes: (1) a chunker; (2) a rule-based
     * disambiguator.
     */
    @Override
    public final AnalyzedSentence disambiguate(AnalyzedSentence input)
            throws Exception {
        return disambiguator.disambiguate(chunker.disambiguate(input));
    }


}
