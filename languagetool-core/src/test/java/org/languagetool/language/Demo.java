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
package org.languagetool.language;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.xx.DemoChunker;
import org.languagetool.rules.Rule;
import org.languagetool.tokenizers.*;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.xx.DemoDisambiguator2;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.DefaultTestResourceDataBroker;

/**
 * A demo language that is part of languagetool-core and thus always available.
 */
public class Demo extends Language<DefaultTestResourceDataBroker> {

  public static final String SHORT_CODE = "xx";

  private Tagger tagger;
  private Chunker chunker;
  //private Disambiguator disambiguator;

  // GTODO Remove this class
  public Demo(String _bogus) {

  }

  @Override
  public Locale getLocale() {
    return new Locale("en");
  }

  @Override
  public CaseConverter getCaseConverter() throws Exception {
      return getUseDataBroker().getCaseConverter();
  }

  @Override
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public DefaultTestResourceDataBroker getDefaultDataBroker() {
      throw new UnsupportedOperationException("Cant call.");
  }

  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
      /*
    if (disambiguator == null) {
      disambiguator = new DemoDisambiguator2();
    }
    return disambiguator;
    */
  }

  @Override
  public String getName() {
    return "Testlanguage";
  }
/*
  @Override
  public String getShortCode() {
    return SHORT_CODE;
  }
*/

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public Language getDefaultLanguageVariant() {
      return this;
  }

  @Override
  public String[] getCountries() {
    return new String[] {"XX"};
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new DemoTagger();
    }
    return tagger;
  }

  /**
   * @since 2.3
   */
  @Override
  public Chunker getChunker() {
    if (chunker == null) {
      chunker = new DemoChunker();
    }
    return chunker;
  }

  @Override
  public Contributor[] getMaintainers() {
    return null;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) {
    return Collections.emptyList();
  }

}
