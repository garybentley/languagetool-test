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

import org.languagetool.language.Polish;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.MultiWordChunker;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetools.databroker;

public class DefaultPolishResourceDataBroker extends DefaultResourceDataBroker implements PolishResourceDataBroker {

    private Disambiguator chunker;
    private Disambiguator disambiguator;

    public Disambiguator getChunker() {
        if (chunker == null) {
            chunker = new MultiWordChunker("/pl/multiwords.txt", this);
        }
        return chunker;
    }

    public Disambiguator getDisambiguator() {
        if (disambiguator == null) {
            disambiguator = new XmlRuleDisambiguator(new Polish());
        }
        return disambiguator;
    }

}
