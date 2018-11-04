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
package org.languagetool;

import org.languagetool.language.Contributor;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.databroker.DefaultResourceDataBroker;
import org.languagetool.tokenizers.*;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.xx.DemoDisambiguator2;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.xx.DemoChunker;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

import java.nio.charset.*;

/**
 * A test language to be used for general rule testing or testing of things not directly related to a Language subclass.  It should be used
 * as a replacement for Demo and FakeLanguage and be a central point for general testing.
 *
 * It has a short code of "xx" and uses a {@link DefaultResourceDataBroker} thus expects resources to be on the class path in the
 * subdir: /xx.
 *
 * It has a single country with name "XX".
 *
 * Set your own data broker or subclass and override {@link getDefaultDataBroker} to use your own data broker for resource
 * retrieval.
 */
public class TestLanguage extends Language<DefaultTestResourceDataBroker> {

  public static final String DEFAULT_TEST_LANG_CODE = "xx";
  public static final String DEFAULT_TEST_LANG_COUNTRY = "XX";
  public static final String DEFAULT_TEST_LANG_NAME = "Test Language";

  private Locale locale;
  private DefaultTestResourceDataBroker dataBroker;
  private String name;
  //private Tagger tagger;
  //private Chunker chunker;
  //private Disambiguator disambiguator;

  public TestLanguage() {
      this(DEFAULT_TEST_LANG_CODE, DEFAULT_TEST_LANG_COUNTRY, DEFAULT_TEST_LANG_NAME);
  }

  public TestLanguage(String langCode, String country, String name) {
      this.locale = new Locale(langCode, country);
      this.name = name;
  }

  public TestLanguage(String langCode) {
      this(langCode, DEFAULT_TEST_LANG_COUNTRY, DEFAULT_TEST_LANG_NAME);
  }

  public TestLanguage(String langCode, String country) {
      this(langCode, country, DEFAULT_TEST_LANG_NAME);
  }

  @Override
  public Locale getLocale() {
      return locale;
  }

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public Language getDefaultLanguageVariant() {
      return null;
  }

  @Override
  public LuceneLanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  public Chunker getChunker() throws Exception {
      return getUseDataBroker().getChunker();
  }

  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  @Override
  public CaseConverter getCaseConverter() throws Exception {
      return getUseDataBroker().getCaseConverter();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public DefaultTestResourceDataBroker getUseDataBroker() throws Exception {
      return getDefaultDataBroker();
  }

  @Override
  public DefaultTestResourceDataBroker getDefaultDataBroker() throws Exception {
      if (dataBroker == null) {
          dataBroker = new DefaultTestResourceDataBroker(this, getClass().getClassLoader());
      }
      return dataBroker;
  }

  public List<AbstractPatternRule> getPatternRules() throws Exception {
    return getUseDataBroker().getPatternRules();
  }

  @Override
  public WordTokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getCountries() {
    return new String[] {locale.getCountry() };
  }

  @Override
  public Contributor[] getMaintainers() {
    return null;
  }

  /**
   * Get the relevant rules, returns an empty list.
   *
   * @return An empty list.
   */
  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
    return Collections.emptyList();
  }
}
