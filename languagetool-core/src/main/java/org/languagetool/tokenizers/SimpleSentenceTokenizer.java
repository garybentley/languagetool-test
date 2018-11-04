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
package org.languagetool.tokenizers;

import net.loomchild.segment.srx.SrxDocument;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.language.Contributor;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.databroker.DefaultResourceDataBroker;
import org.languagetool.chunking.*;

import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A very simple sentence tokenizer that splits on {@code [.!?…]} followed by whitespace
 * or an uppercase letter. You probably want to use an adapted {@link SRXSentenceTokenizer} instead.
 * @since 2.6
 */
public class SimpleSentenceTokenizer extends SRXSentenceTokenizer {

  public SimpleSentenceTokenizer(Language lang, SrxDocument doc) {
    super(lang, doc);
  }

/*
GTODO Clean up
  static class AnyLanguage<E extends ResourceDataBroker> extends Language<E> {
    @Override public String getShortCode() {
      return "xx";
    }
    @Override public String getName() {
      return "FakeLanguage";
    }
    @Override public String[] getCountries() {
      return new String[0];
    }
    @Override public Contributor[] getMaintainers() {
      return new Contributor[0];
    }
    @Override public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) {
      return Collections.emptyList();
    }

    @Override
    public Chunker getChunker() throws Exception {
        return getUseDataBroker().getChunker();
    }

    @Override
    public CaseConverter getCaseConverter() throws Exception {
        return getUseDataBroker().getCaseConverter();
    }
    @Override
    public WordTokenizer getWordTokenizer() throws Exception {
        return getUseDataBroker().getWordTokenizer();
    }
    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
        return getUseDataBroker().getSentenceTokenizer();
    }
    @Override public DefaultResourceDataBroker getDefaultDataBroker() {
        return new DefaultResourceDataBroker(this, getClass().getClassLoader());
    }
  }
*/
}
