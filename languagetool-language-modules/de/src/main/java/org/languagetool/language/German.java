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

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.chunking.Chunker;
import org.languagetool.chunking.GermanChunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.de.*;
import org.languagetool.rules.de.LongSentenceRule;
import org.languagetool.rules.de.SentenceWhitespaceRule;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRuleCreator;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.de.GermanTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.CompoundWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.de.GermanCompoundTokenizer;
import org.languagetool.databroker.GermanResourceDataBroker;
import org.languagetool.databroker.DefaultGermanResourceDataBroker;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * Support for German - use the sub classes {@link GermanyGerman}, {@link SwissGerman}, or {@link AustrianGerman}
 * if you need spell checking.
 */
public class German extends Language<GermanResourceDataBroker> implements AutoCloseable {

    public static final String LANGUAGE_ID = "de";
    public static final Locale LOCALE = new Locale(LANGUAGE_ID);

  private static final Language GERMANY_GERMAN = new GermanyGerman();

  private List<Rule> nnRules;
  private Word2VecModel word2VecModel;

  /**
   * @deprecated use {@link GermanyGerman}, {@link AustrianGerman}, or {@link SwissGerman} instead -
   *  they have rules for spell checking, this class doesn't (deprecated since 3.2)
   */
  @Deprecated
  public German() {
  }

  @Override
  public Locale getLocale() {
      return LOCALE;
  }

  @Override
  public GermanResourceDataBroker getUseDataBroker() throws Exception {
      return super.getUseDataBroker();
  }

  @Override
  public GermanResourceDataBroker getDefaultDataBroker() throws Exception {
      return new DefaultGermanResourceDataBroker(this, getClass().getClassLoader());
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return GERMANY_GERMAN;
  }

  @Override
  public boolean isVariant() {
      return false;
  }

  @Override
  public Disambiguator getDisambiguator() throws Exception {
      return getUseDataBroker().getDisambiguator();
  }

  /**
   * @since 2.9
   */
  @Override
  public Chunker getPostDisambiguationChunker() throws Exception {
      return getUseDataBroker().getPostDisambiguationChunker();
  }

  @Override
  public String getName() {
    return "German";
  }

  @Override
  public String[] getCountries() {
      // GTODO: Only these countries?
    return new String[]{"LU", "LI", "BE"};
  }

  @Override
  public Tagger getTagger() throws Exception {
      return getUseDataBroker().getTagger();
  }

  @Override
  @NotNull
  public Synthesizer getSynthesizer() throws Exception {
      return getUseDataBroker().getSynthesizer();
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return getUseDataBroker().getSentenceTokenizer();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Jan Schreiber"),
        Contributors.DANIEL_NABER,
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      messages = getUseMessages(messages);
    return Arrays.asList(
            createCommaWhitespaceRule(messages),
            createUnpairedBracketsRule(messages),
            createUppercaseSentenceStartRule(messages),
            createMultipleWhitespaceRule(messages),
            // specific to German:
            createOldSpellingRule(messages),
            createSentenceWhitespaceRule(messages),
            createDoublePunctuationRule(messages),
            createMissingVerbRule(messages),
            createWordRepeatRule(messages),
            createWordRepeatBeginningRule(messages),
            createWrongWordInContextRule(messages),
            createAgreementRule(messages),
            createCaseRule(messages),
            createCompoundRule(messages),
            createDashRule(messages),
            createVerbAgreementRule(messages),
            createSubjectVerbAgreementRule(messages),
            createWordCoherencyRule(messages),
            createSimilarNameRule(messages),
            createWiederVsWiderRule(messages),
            createWhiteSpaceBeforeParagraphEndRule(messages),
            createWhiteSpaceAtBeginOfParagraphRule(messages),
            createEmptyLineRule(messages),
            createGermanStyleRepeatedWordRule(messages, userConfig),
            createCompoundCoherencyRule(messages),
            createLongSentenceRule(messages, userConfig),
            createLongParagraphRule(messages, userConfig),
            createFillerWordsRule(messages, userConfig),
            createParagraphRepeatBeginningRule(messages),
            createPunctuationMarkAtParagraphEnd(messages),
            createDuUpperLowerCaseRule(messages),
            createUnitConversionRule(messages)
    );
  }

  public MultipleWhitespaceRule createMultipleWhitespaceRule(ResourceBundle messages) throws Exception {
      return new MultipleWhitespaceRule(getUseMessages(messages), this);
  }

  public UppercaseSentenceStartRule createUppercaseSentenceStartRule(ResourceBundle messages) throws Exception {
      return new UppercaseSentenceStartRule(getUseMessages(messages), this,
                          Example.wrong("Das Haus ist alt. <marker>es</marker> wurde 1950 gebaut."),
                          Example.fixed("Das Haus ist alt. <marker>Es</marker> wurde 1950 gebaut."));
  }

  public GenericUnpairedBracketsRule createUnpairedBracketsRule(ResourceBundle messages) throws Exception {
      return new GenericUnpairedBracketsRule(getUseMessages(messages),
              Arrays.asList("[", "(", "{", "„", "»", "«", "\""),
              Arrays.asList("]", ")", "}", "“", "«", "»", "\""));
  }

  public CommaWhitespaceRule createCommaWhitespaceRule(ResourceBundle messages) throws Exception {
      return new CommaWhitespaceRule(getUseMessages(messages),
              Example.wrong("Die Partei<marker> ,</marker> die die letzte Wahl gewann."),
              Example.fixed("Die Partei<marker>,</marker> die die letzte Wahl gewann."));
  }

  public OldSpellingRule createOldSpellingRule(ResourceBundle messages) throws Exception {
      return new OldSpellingRule(getUseMessages(messages), getUseDataBroker().getOldSpellingRuleSuggestions());
  }

  public SentenceWhitespaceRule createSentenceWhitespaceRule(ResourceBundle messages) throws Exception {
      return new SentenceWhitespaceRule(getUseMessages(messages));
  }

  public GermanDoublePunctuationRule createDoublePunctuationRule(ResourceBundle messages) throws Exception {
      return new GermanDoublePunctuationRule(getUseMessages(messages));
  }

  public GermanWrongWordInContextRule createWrongWordInContextRule(ResourceBundle messages) throws Exception {
      return new GermanWrongWordInContextRule(getUseMessages(messages), getUseDataBroker().getWrongWordsInContext());
  }

  public AgreementRule createAgreementRule(ResourceBundle messages) throws Exception {
      return new AgreementRule(getUseMessages(messages), this);
  }

  public CaseRule createCaseRule(ResourceBundle messages) throws Exception {
      return new CaseRule(getUseMessages(messages), this, getUseDataBroker().getTagger(), getUseDataBroker().getCaseRuleExceptionPatterns());
  }

  public DashRule createDashRule(ResourceBundle messages) throws Exception {
      return new DashRule(getUseMessages(messages));
  }

  public VerbAgreementRule createVerbAgreementRule(ResourceBundle messages) throws Exception {
      return new VerbAgreementRule(getUseMessages(messages), this);
  }

  public SubjectVerbAgreementRule createSubjectVerbAgreementRule(ResourceBundle messages) throws Exception {
      return new SubjectVerbAgreementRule(getUseMessages(messages), this, getUseDataBroker().getTagger());
  }

  public GermanStyleRepeatedWordRule createGermanStyleRepeatedWordRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new GermanStyleRepeatedWordRule(getUseMessages(messages), userConfig);
  }

  public GermanFillerWordsRule createFillerWordsRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      return new GermanFillerWordsRule(getUseMessages(messages), this, userConfig);
  }

  public SimilarNameRule createSimilarNameRule(ResourceBundle messages) throws Exception {
      return new SimilarNameRule(getUseMessages(messages));
  }

  public WiederVsWiderRule createWiederVsWiderRule(ResourceBundle messages) throws Exception {
      return new WiederVsWiderRule(getUseMessages(messages));
  }

  public WhiteSpaceBeforeParagraphEnd createWhiteSpaceBeforeParagraphEndRule(ResourceBundle messages) throws Exception {
      return new WhiteSpaceBeforeParagraphEnd(getUseMessages(messages), this);
  }

  public WhiteSpaceAtBeginOfParagraph createWhiteSpaceAtBeginOfParagraphRule(ResourceBundle messages) throws Exception {
      return new WhiteSpaceAtBeginOfParagraph(getUseMessages(messages));
  }

  public EmptyLineRule createEmptyLineRule(ResourceBundle messages) throws Exception {
      return new EmptyLineRule(getUseMessages(messages), this);
  }

  public CompoundCoherencyRule createCompoundCoherencyRule(ResourceBundle messages) throws Exception {
      return new CompoundCoherencyRule(getUseMessages(messages));
  }

  public GermanParagraphRepeatBeginningRule createParagraphRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new GermanParagraphRepeatBeginningRule(getUseMessages(messages), this);
  }

  public PunctuationMarkAtParagraphEnd createPunctuationMarkAtParagraphEnd(ResourceBundle messages) throws Exception {
      return new PunctuationMarkAtParagraphEnd(getUseMessages(messages), this);
  }

  public DuUpperLowerCaseRule createDuUpperLowerCaseRule(ResourceBundle messages) throws Exception {
      return new DuUpperLowerCaseRule(getUseMessages(messages));
  }

  public UnitConversionRule createUnitConversionRule(ResourceBundle messages) throws Exception {
      return new UnitConversionRule(getUseMessages(messages));
  }

  public MissingVerbRule createMissingVerbRule(ResourceBundle messages) throws Exception {
      return new MissingVerbRule(getUseMessages(messages), this);
  }

  public GermanWordRepeatRule createWordRepeatRule(ResourceBundle messages) throws Exception {
      return new GermanWordRepeatRule(getUseMessages(messages));
  }

  public CompoundRule createCompoundRule(ResourceBundle messages) throws Exception {
      return new CompoundRule(getUseMessages(messages), getUseDataBroker().getCompounds());
  }

  public WordCoherencyRule createWordCoherencyRule(ResourceBundle messages) throws Exception {
      return new WordCoherencyRule(getUseMessages(messages), getUseDataBroker().getCoherencyMappings());
  }

  public GermanWordRepeatBeginningRule createWordRepeatBeginningRule(ResourceBundle messages) throws Exception {
      return new GermanWordRepeatBeginningRule(getUseMessages(messages));
  }

  public LongParagraphRule createLongParagraphRule(ResourceBundle messages, int maxWords) throws Exception {
      return new LongParagraphRule(getUseMessages(messages), this, maxWords);
  }

  public LongParagraphRule createLongParagraphRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      int confWords = -1;
      if (userConfig != null) {
         confWords = userConfig.getConfigValueByID(LongParagraphRule.getRuleConfiguration().getRuleId());
      }
      return createLongParagraphRule(messages, confWords);
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, UserConfig userConfig) throws Exception {
      int confWords = -1;
      if (userConfig != null) {
        confWords = userConfig.getConfigValueByID(LongSentenceRule.getRuleConfiguration().getRuleId());
      }
      return createLongSentenceRule(messages, confWords);
  }

  public LongSentenceRule createLongSentenceRule(ResourceBundle messages, int maxWords) throws Exception {
      return new LongSentenceRule(getUseMessages(messages), maxWords);
  }

  /**
   * @since 2.7
   */
   /*
   GTODO Clean up
  public Tokenizer getNonStrictCompoundSplitter() {
    if (compoundTokenizer == null) {
      try {
        GermanCompoundTokenizer tokenizer = new GermanCompoundTokenizer(false);  // there's a spelling mistake in (at least) one part, so strict mode wouldn't split the word
        compoundTokenizer = word -> new ArrayList<>(tokenizer.tokenize(word));
      } catch (IOException e) {
        throw new RuntimeException("Could not set up German compound splitter", e);
      }
    }
    return compoundTokenizer;
  }
*/
  /**
   * @since 2.7
   */
/*
GTODO Clean up
  public Tokenizer getStrictCompoundTokenizer() {
      return getUseDataBroker().getStrictCompoundTokenizer();
  }
*/
  @Override
  public LanguageModel getLanguageModel() throws Exception {
      return getUseDataBroker().getLanguageModel();
  }

  /** @since 4.0 */
  @Override
  public Word2VecModel getWord2VecModel() throws Exception {
      return getUseDataBroker().getWord2VecModel();
  }

  /** @since 3.1 */
  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      List<Rule> rules = new ArrayList<>();

      rules.add (createConfusionProbabilityRule(messages, languageModel));
      return rules;
  }

  public GermanConfusionProbabilityRule createConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return new GermanConfusionProbabilityRule(getUseMessages(messages), languageModel, this, getUseDataBroker().getConfusionSets());
  }

  public ProhibitedCompoundRule createProhibitedCompoundRule(ResourceBundle messages, BaseLanguageModel languageModel, GermanSpellerRule spellerRule) throws Exception {
      return new ProhibitedCompoundRule(getUseMessages(messages), languageModel, spellerRule, getUseDataBroker().getConfusionSets());
  }

  /** @since 4.0 */
  @Override
  public List<NeuralNetworkRule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception {
      return getUseDataBroker().createNeuralNetworkRules(messages, word2vecModel);
  }

  /**
   * Closes the language model, if any.
   * @since 3.1
   */
  @Override
  public void close() throws Exception {
      getUseDataBroker().close();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public int getPriorityForId(String id) {
    switch (id) {
      case "OLD_SPELLING_INTERNAL": return 10;
      case "DE_PROHIBITED_COMPOUNDS": return 1;  // a more detailed error message than from spell checker
      case "ANS_OHNE_APOSTROPH": return 1;
      case "CONFUSION_RULE": return -1;  // probably less specific than the rules from grammar.xml
      case "AKZENT_STATT_APOSTROPH": return -1;  // lower prio than PLURAL_APOSTROPH
      case "PUNKT_ENDE_ABSATZ": return -10;  // should never hide other errors, as chance for a false alarm is quite high
      case "KOMMA_ZWISCHEN_HAUPT_UND_NEBENSATZ": return -10;
    }
    return 0;
  }

}
