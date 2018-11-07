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
package org.languagetool;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.languagetool.chunking.Chunker;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.databroker.DefaultResourceDataBroker;
import org.languagetool.language.Contributor;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.patterns.*;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.SimpleSentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.WordTokenizer;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.*;

/**
 * Base class for any supported language (English, German, etc). Language classes
 * are detected at runtime by searching the classpath for files named
 * {@code META-INF/org/languagetool/language-module.properties}. Those file(s)
 * need to contain a key {@code languageClasses} which specifies the fully qualified
 * class name(s), e.g. {@code org.languagetool.language.English}. Use commas to specify
 * more than one class.
 *
 * <p>Sub classes should typically use lazy init for anything that's costly to set up.
 * This improves start up time for the LanguageTool stand-alone version.
 */
public abstract class Language<E extends ResourceDataBroker> implements AutoCloseable {

  //private static final Disambiguator DEMO_DISAMBIGUATOR = new DemoDisambiguator();
  //private static final Tagger DEMO_TAGGER = new DemoTagger();
  //private static final SentenceTokenizer SENTENCE_TOKENIZER = new SimpleSentenceTokenizer();
  //private static final WordTokenizer WORD_TOKENIZER = new WordTokenizer();

  private final UnifierConfiguration unifierConfig = new UnifierConfiguration();
  private final UnifierConfiguration disambiguationUnifierConfig = new UnifierConfiguration();

  private final Pattern ignoredCharactersRegex = Pattern.compile("[\u00AD]");  // soft hyphen

  private List<AbstractPatternRule> patternRules;

  private E dataBroker;
  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;
  private Tagger demoTagger;
  private Disambiguator demoDisambiguator;

  public Language () {
      // Set up a default broker that ensures we use the correct class loader, i.e. whatever loaded us.
      // GTODO: Remove?
      //this.dataBroker = new DefaultResourceDataBroker(Language.class.getClassLoader());
  }

  /**
   * Get the default resource data broker to use.  If no explicit data broker is set on this language
   * then the default is used instead.  If the default is null then we use JLanguageTool.getDataBroker instead.
   */
  public abstract E getDefaultDataBroker() throws Exception;

  /**
   * Get this language's character code, e.g. <code>en</code> for English.
   * For most languages this is a two-letter code according to ISO 639-1,
   * but for those languages that don't have a two-letter code, a three-letter
   * code according to ISO 639-2 is returned.
   * The country parameter (e.g. "US"), if any, is not returned.
   * @since 3.6
   */
  //public abstract String getShortCode();

  /**
   * Get this language's name in English, e.g. <code>English</code> or
   * <code>German (Germany)</code>.
   * @return language name
   */
  public abstract String getName();

  /**
   * Get this language's country options , e.g. <code>US</code> (as in <code>en-US</code>) or
   * <code>PL</code> (as in <code>pl-PL</code>).
   * @return String[] - array of country options for the language.
   */
  public abstract String[] getCountries();

  /**
   * Get the name(s) of the maintainer(s) for this language or <code>null</code>.
   */
  @Nullable
  public abstract Contributor[] getMaintainers();

  /**
   * Get the rules classes that should run for texts in this language.
   * @since 1.4 (signature modified in 2.7)
   */
   // GTODO Need to remove the assumption that the rules WILL be available.
   // GTODO Check is altLanguages EVER used by a language implementation?
  public abstract List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception;

  // -------------------------------------------------------------------------

  /**
   * Get this language's variant, e.g. <code>valencia</code> (as in <code>ca-ES-valencia</code>)
   * or <code>null</code>.
   * Attention: not to be confused with "country" option
   * @return variant for the language or {@code null}
   * @since 2.3
   */
  @Nullable
  public String getVariant() {
      // GTODO Remove and rely on Locale.getVariant instead.
    return "".equals(getLocale().getVariant()) ? null : getLocale().getVariant();
  }

  /**
   * Get enabled rules different from the default ones for this language variant.
   *
   * @return enabled rules for the language variant.
   * @since 2.4
   */
   // GTODO Why do this?  Why not just get the rules there are...
  public List<String> getDefaultEnabledRulesForVariant() {
    return Collections.emptyList();
  }

  /**
   * Get disabled rules different from the default ones for this language variant.
   *
   * @return disabled rules for the language variant.
   * @since 2.4
   */
   // GTODO Why do this?  Why not just get the rules there are...
  public List<String> getDefaultDisabledRulesForVariant() {
    return Collections.emptyList();
  }

  /**
   * @param indexDir directory with a '3grams' sub directory which contains a Lucene index with 3gram occurrence counts
   * @return a LanguageModel or {@code null} if this language doesn't support one
   * @since 2.7
   */
  @Nullable
  public LanguageModel getLanguageModel()/*GTODO File indexDir*/ throws Exception {
    return null;
  }

  /**
   * Get a list of rules that require a {@link LanguageModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 2.7
   */
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
    return Collections.emptyList();
  }

  /**
   * @param indexDir directory with a subdirectories like 'en', each containing dictionary.txt and final_embeddings.txt
   * @return a {@link Word2VecModel} or {@code null} if this language doesn't support one
   * @since 4.0
   */
  @Nullable
  public Word2VecModel getWord2VecModel() throws Exception {
    return null;
  }

  /**
   * Get a list of rules that require a {@link Word2VecModel}. Returns an empty list for
   * languages that don't have such rules.
   * @since 4.0
   */
  public List<? extends Rule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception {
    return Collections.emptyList();
  }

  /**
   * Get this language's Java locale, not considering the country code.
   */
  public abstract Locale getLocale();
  /*
  GTODO Clean up
    return new Locale(getShortCode());
  }
*/
  /**
   * Get this language's Java locale, considering language code and country code (if any).
   * @since 2.1
   */
   /*
   GTODO Clean up
  public Locale getLocaleWithCountryAndVariant() {
    if (getCountries().length > 0) {
      if (getVariant() != null) {
        return new Locale(getShortCode(), getCountries()[0], getVariant());
      } else {
        return new Locale(getShortCode(), getCountries()[0]);
      }
    } else {
      return getLocale();
    }
  }
*/
  /**
   * Set the resource data broker to use.
   *
   * @param broker The resource data broker to use.
   */
  public void setDataBroker(E broker) {
      dataBroker = broker;
  }

  /**
   * Get the resource data broker this language is using.
   *
   * @return The broker this language is using.
   */
  public E getDataBroker() {
      return dataBroker;
  }

  /**
   * Get the set data broker or default to {@org.languagetool.JLanguageTool#getDataBrokder()} if not set.
   *
   * @return The broker to use.
   */
  public E getUseDataBroker() throws Exception {
      if (dataBroker == null) {
          E def = getDefaultDataBroker();
          if (def == null) {
              throw new IllegalArgumentException("Default data broker cannot be null.");
          }
          dataBroker = def;
      }
      return dataBroker;
  }

  /**
   * Get the location of the rule file(s) in a form like {@code /org/languagetool/rules/de/grammar.xml},
   * i.e. a path in the classpath.
   */
   /*
   GTODO Clean up
  public List<String> getRuleFileNames() {
    List<String> ruleFiles = new ArrayList<>();
    ResourceDataBroker dataBroker = getUseDataBroker();
    ruleFiles.add(dataBroker.getRulesDir()
            + "/" + getShortCode() + "/" + JLanguageTool.PATTERN_FILE);
    if (getShortCodeWithCountryAndVariant().length() > 2) {
      String fileName = getShortCode() + "/"
              + getShortCodeWithCountryAndVariant()
              + "/" + JLanguageTool.PATTERN_FILE;
      if (dataBroker.ruleFileExists(fileName)) {
        ruleFiles.add(dataBroker.getRulesDir() + "/" + fileName);
      }
    }
    return ruleFiles;
  }
*/
  /**
   * Languages that have country variants need to overwrite this to select their most common variant.
   * @return default country variant or {@code null}
   * @since 1.8
   */
  @Nullable
  public abstract Language getDefaultLanguageVariant();

  /**
   * Return the resource message bundle that should be used for this language.
   */
  public ResourceBundle getMessageBundle() throws Exception {
      return getUseDataBroker().getMessageBundle();
  }

  /**
   * Get this language's part-of-speech disambiguator implementation.
   */
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
      /*
      GTODO: Clean up, probably not needed
      if (demoDisambiguator == null) {
          demoDisambiguator = new DemoDisambiguator();
      }
    return demoDisambiguator;
    */
  }

  /**
   * Get this language's part-of-speech tagger implementation. The tagger must not
   * be {@code null}, but it can be a trivial pseudo-tagger that only assigns {@code null} tags.
   */
  @NotNull
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  /**
   * Get this language's sentence tokenizer implementation.
   */
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  /**
   * Get the case converter.
   */
  public CaseConverter getCaseConverter() throws Exception {
      return getUseDataBroker().getCaseConverter();
  }

  /**
   * Get this language's word tokenizer implementation.
   */
  public Tokenizer getWordTokenizer() throws Exception {
      return getUseDataBroker().getWordTokenizer();
  }

  /**
   * Get this language's chunker implementation or {@code null}.
   * @since 2.3
   */
  @Nullable
  public Chunker getChunker() throws Exception {
      return getUseDataBroker().getChunker();
  }

  /**
   * Get this language's chunker implementation or {@code null}.
   * @since 2.9
   */
  @Nullable
  public Chunker getPostDisambiguationChunker() throws Exception {
      // GTODO: Add to data broker
    return null;
  }

  /**
   * Get this language's part-of-speech synthesizer implementation or {@code null}.
   */
  @Nullable
  public Synthesizer getSynthesizer() throws Exception {
    return getUseDataBroker().getSynthesizer();
  }

  /**
   * Get this language's feature unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getUnifier() {
      // GTODO: Add to data broker
    return unifierConfig.createUnifier();
  }

  /**
   * Get this language's feature unifier used for disambiguation.
   * Note: it might be different from the normal rule unifier.
   * @return Feature unifier for analyzed tokens.
   */
  public Unifier getDisambiguationUnifier() {
      // GTODO: Add to data broker
    return disambiguationUnifierConfig.createUnifier();
  }

  /**
   * @since 2.3
   */
  public UnifierConfiguration getUnifierConfiguration() {
      // GTODO: Add to data broker
    return unifierConfig;
  }

  /**
   * @since 2.3
   */
  public UnifierConfiguration getDisambiguationUnifierConfiguration() {
      // GTODO: Add to data broker
    return disambiguationUnifierConfig;
  }

  private String localeToLanguageId() {
      Locale l = getLocale();
      StringBuilder b = new StringBuilder(l.getLanguage());
      String c = l.getCountry();
      if (!"".equals(c)) {
          b.append("-");
          b.append(c);
      }
      String v = l.getVariant();
      if (!"".equals(v)) {
          b.append("-");
          b.append(v);
      }
      return b.toString();
  }

  /**
   * Get the name of the language translated to the current locale,
   * if available. Otherwise, get the untranslated name.
   */
  public final String getTranslatedName(ResourceBundle messages) {
    try {
      return messages.getString(localeToLanguageId());
    } catch (MissingResourceException e) {
      try {
        return messages.getString(getLocale().getLanguage());
      } catch (MissingResourceException e1) {
        return getName();
      }
    }
  }

  public boolean isVariantOf(Language lang) {
      if (getLocale().getLanguage().equals(lang.getLocale().getLanguage())) {
          // Same language.
          return isVariant();
      }
      return false;
  }

  /**
   * Get the short name of the language with country and variant (if any), if it is
   * a single-country language. For generic language classes, get only a two- or
   * three-character code.
   * @since 3.6
   */
   /*
   GTODO Clean up
  public final String getShortCodeWithCountryAndVariant() {
    String name = getShortCode();
    if (getCountries().length == 1 && !name.contains("-x-")) {   // e.g. "de-DE-x-simple-language"
      name += "-" + getCountries()[0];
      if (getVariant() != null) {   // e.g. "ca-ES-valencia"
        name += "-" + getVariant();
      }
    }
    return name;
  }
*/
  /**
   * Get the pattern rules as defined in the files returned by {@link #getRuleFileNames()}.
   * @since 2.7
   */
  @SuppressWarnings("resource")
  protected List<AbstractPatternRule> getPatternRules() throws Exception {
      return getUseDataBroker().getPatternRules();
      /*
    // use lazy loading to speed up server use case and start of stand-alone LT, where all the languages get initialized:
    if (patternRules == null) {
      List<AbstractPatternRule> rules = new ArrayList<>();
      PatternRuleLoader ruleLoader = new PatternRuleLoader(getUseDataBroker());
      for (String fileName : getRuleFileNames()) {
        InputStream is = null;
        try {
            // TODO: Change to use the dataBroker.
          is = this.getClass().getResourceAsStream(fileName);
          if (is == null) {                     // files loaded via the dialog
            is = new FileInputStream(fileName);
          }
          rules.addAll(ruleLoader.getRules(is, fileName));
          patternRules = Collections.unmodifiableList(rules);
        } finally {
          if (is != null) {
            is.close();
          }
        }
      }
    }
    return patternRules;
    */
  }

  @Override
  public final String toString() {
    return getName();
  }

  /**
   * Whether this is a country variant of another language, i.e. whether it doesn't
   * directly extend {@link Language}, but a subclass of {@link Language}.
   * @since 1.8
   */
  public abstract boolean isVariant();
/*
 GTODO Clean up
    for (Language language : Languages.get()) {
      boolean skip = language.getShortCodeWithCountryAndVariant().equals(getShortCodeWithCountryAndVariant());
      if (!skip && language.getClass().isAssignableFrom(getClass())) {
        return true;
      }
    }
    return false;

  }
*/
 public Set<Language> getVariants() {
     return Languages.get().stream()
        .filter(l -> getLocale().getLanguage().equals(l.getLocale().getLanguage()) && l.isVariant())
        .collect(Collectors.toSet());
 }
  /**
   * Whether this class has at least one subclass that implements variants of this language.
   * @since 1.8
   */
/*
GTODO No longer valid, the class may not be loaded...
  public final boolean hasVariant() {
    for (Language language : Languages.get()) {
      boolean skip = language.getShortCodeWithCountryAndVariant().equals(getShortCodeWithCountryAndVariant());
      if (!skip && getClass().isAssignableFrom(language.getClass())) {
        return true;
      }
    }
    return false;
  }
*/
  /**
   * For internal use only. Overwritten to return {@code true} for languages that
   * have been loaded from an external file after start up.
   */
  public boolean isExternal() {
    return false;
  }

  /**
   * Return true if this is the same language as the given one, considering country
   * variants only if set for both languages. For example: en = en, en = en-GB, en-GB = en-GB,
   * but en-US != en-GB
   * @since 1.8
   */
  public boolean equalsConsiderVariantsIfSpecified(Language otherLanguage) {
      if (getLocale().getLanguage().equals(otherLanguage.getLocale().getLanguage())) {
          String c = getLocale().getCountry();
          String oc = otherLanguage.getLocale().getCountry();
          if ("".equals(c) || "".equals(oc)) {
              return true;
          }
          return c.equals(oc);
      }
      return false;
      /*
       GTODO Clean up
    if (getShortCode().equals(otherLanguage.getShortCode())) {
      boolean thisHasCountry = hasCountry();
      boolean otherHasCountry = otherLanguage.hasCountry();
      return !(thisHasCountry && otherHasCountry) ||
              getShortCodeWithCountryAndVariant().equals(otherLanguage.getShortCodeWithCountryAndVariant());
    } else {
      return false;
    }
    */
  }
/*
 GTODO Clean up
  private boolean hasCountry() {
      return !"".equals(getLocale().getCountry());
    return getCountries().length == 1;
  }
*/
  /**
   * @return Return compiled regular expression to ignore inside tokens
   * @since 2.9
   */
  public Pattern getIgnoredCharactersRegex() {
    return ignoredCharactersRegex;
  }

  /**
   * Information about whether the support for this language in LanguageTool is actively maintained.
   * If not, the user interface might show a warning.
   * @since 3.3
   */
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.LookingForNewMaintainer;
  }

  /*
   * True if language should be hidden on GUI (i.e. en, de, pt,
   * instead of en-US, de-DE, pt-PT)
   * @since 3.3
   */
   /*
    GTODO This should be the decision of the gui code to decide what to display NOT the language...
  public boolean isHiddenFromGui() {
    return hasVariant() && !isVariant() && !isTheDefaultVariant();
  }
*/
/*
GTODO Clean up
  private boolean isTheDefaultVariant() {
    if (getDefaultLanguageVariant() != null) {
      return getClass().equals(getDefaultLanguageVariant().getClass());
    }
    return false;
  }
*/
  /**
   * Returns a priority for Rule or Category Id (default: 0).
   * Positive integers have higher priority.
   * Negative integers have lower priority.
   * @since 3.6
   */
  public int getPriorityForId(String id) {
    return 0;
  }

  /**
   * Get the messages to use, if the messages passed is null then return the message bundle for this language.
   *
   * @param messages The messages, may be null.
   * @return The messages to use, either those passed in or those returned by {@link getMessageBundle()}.
   */
  protected ResourceBundle getUseMessages(ResourceBundle messages) throws Exception {
      return messages != null ? messages : getMessageBundle();
  }

  /**
   * Considers languages as equal if their language code, including the country and variant codes are equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Language other = (Language) o;
    return Objects.equals(getLocale(), other.getLocale());
    //GTODO getShortCodeWithCountryAndVariant(), other.getShortCodeWithCountryAndVariant());
  }

  @Override
  public int hashCode() {
      Locale l = getLocale();
    return Objects.hash(l.getLanguage(), l.getCountry(), l.getVariant());
    //GTODO getShortCodeWithCountryAndVariant().hashCode();
  }

  @Override
  public void close() throws Exception {
      getUseDataBroker().close();
  }

}
