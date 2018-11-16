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

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.UserConfig;
import org.languagetool.chunking.Chunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.rules.patterns.RuleFilterCreator;
import org.languagetool.rules.patterns.PatternRuleHandler;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.databroker.DefaultResourceDataBroker;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.tokenizers.WordTokenizer;

import java.nio.file.*;
import java.util.*;

/**
 * Create a language by specifying the language's XML rule file.
 */
public final class LanguageBuilder {

  private LanguageBuilder() {
  }

  public static Language makeAdditionalLanguage(Locale locale, String langName, Path ruleFile) throws Exception {
    return makeLanguage(locale, langName, ruleFile, true);
  }

  /**
   * Takes an XML file named <tt>rules-xx-language.xml</tt>,
   * e.g. <tt>rules-de-German.xml</tt> and builds
   * a Language object for that language.

   * GTODO This whole thing seems to be just to load some pattern rules???
   * Maybe just allow adding new pattern rules to a language???
   */
  private static Language makeLanguage(Locale locale, String langName, Path ruleFile, boolean isAdditional) throws Exception {
    Objects.requireNonNull(ruleFile, "Rule file cannot be null");
/*
GTODO, clean up the file extension has no bearing on the format within the file...
    if (!file.getName().endsWith(".xml")) {
      throw new RuleFilenameException(file);
    }
    */
    /*
    GTODO Clean up
    String[] parts = file.getName().split("-");
    boolean startsWithRules = parts[0].equals("rules");
    boolean secondPartHasCorrectLength = parts.length == 3 &&
            (parts[1].length() == "en".length() || parts[1].length() == "ast".length() || parts[1].length() == "en_US".length());
    if (!startsWithRules || !secondPartHasCorrectLength) {
      throw new RuleFilenameException(file);
    }
    */
    //TODO: when the XML file is mergeable with
    // other rules (check this in the XML Rule Loader by using rules[@integrate='add']?),
    // subclass the existing language,
    //and adjust the settings if any are set in the rule file default configuration set
    // GTODO This may fail because some of the rules require specific data...
    // GTODO May need to go through the language...
    List<AbstractPatternRule> rules = DefaultResourceDataBroker.createPatternRules(ruleFile, new PatternRuleHandler(new RuleFilterCreator(LanguageBuilder.class.getClassLoader()), false));

    Language newLanguage;
    // Do we already have the language?
    if (Languages.isLanguageSupported(locale)) {
      Language baseLanguage = Languages.getNewLanguageInstance(locale);
      //Language baseLanguage = Languages.getLanguageForShortCode(parts[1]).getClass().newInstance();
      newLanguage = new ExtendedLanguage<ResourceDataBroker>(baseLanguage, langName, rules);
    } else {
      newLanguage = new Language<ResourceDataBroker>() {
        @Override
        public Language getDefaultLanguageVariant() {
            return null;
        }

        @Override
        public Locale getLocale() {
          return locale;
        }

        @Override
        public boolean isVariant() {
            return !"".equals(locale.getVariant());
        }

        @Override
        public ResourceBundle getMessageBundle() throws Exception {
            return getUseDataBroker().getMessageBundle();
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
        public SentenceTokenizer getSentenceTokenizer() throws Exception {
            return getUseDataBroker().getSentenceTokenizer();
        }

        @Override
        public Tokenizer getWordTokenizer() throws Exception {
            return getUseDataBroker().getWordTokenizer();
        }

        @Override
        public Contributor[] getMaintainers() {
          return null;
        }
/*
GTODO Clean up
        @Override
        public String getShortCode() {
            return locale.getLanguage();
            */
            /*
            GTODO Clean up
          if (parts[1].length() == 2) {
            return parts[1];
          }
          return parts[1].split("_")[0]; //en as in en_US
          */
        //}

        @Override
        public String[] getCountries() {
            return new String[]{locale.getCountry()};
/*
GTODO Clean up
          if (parts[1].length() == 2) {
            return new String[]{""};
          }
          return new String[]{parts[1].split("_")[1]}; //US as in en_US
          */
        }

        @Override
        public String getName() {
            return langName;
          //GTODO return parts[2].replace(".xml", "");
        }

        @Override
        public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) {
          return Collections.emptyList();
        }

        @Override
        public List<AbstractPatternRule> getPatternRules() throws Exception {
            return rules;
        }

        /*
        GTODO Clean up
        @Override
        public List<String> getRuleFileNames() {
          List<String> ruleFiles = new ArrayList<>();
          ruleFiles.add(file.getAbsolutePath());
          return ruleFiles;
        }
*/
        @Override
        public boolean isExternal() {
          return isAdditional;
        }

        @Override
        public ResourceDataBroker getDefaultDataBroker() throws Exception {
            return DefaultResourceDataBroker.newClassPathInstance(this, getClass().getClassLoader());
        }
      };
    }
    return newLanguage;
  }

  static class ExtendedLanguage<E extends ResourceDataBroker> extends Language<E> {

    private final Language<E> baseLanguage;
    private final String name;
    private final List<AbstractPatternRule> patternRules;
    //GTODO private final File ruleFile;

    ExtendedLanguage(Language<E> baseLanguage, String name, List<AbstractPatternRule> patternRules) {
      this.baseLanguage = baseLanguage;
      this.name = name;
      this.patternRules = patternRules;
      //GTODO this.ruleFile = ruleFile;
    }

    @Override
    public boolean isVariant() {
        return baseLanguage.isVariant();
    }

    @Override
    public List<AbstractPatternRule> getPatternRules() {
        return patternRules;
    }

    @Override
    public E getDefaultDataBroker() throws Exception {
        return baseLanguage.getDefaultDataBroker();
    }

    @Override
    public String getName() {
      return name;
    }
/*
GTODO Clean up
    @Override
    public List<String> getRuleFileNames() {
      List<String> ruleFiles = new ArrayList<>();
      ruleFiles.addAll(baseLanguage.getRuleFileNames());
      ruleFiles.add(ruleFile.getAbsolutePath());
      return ruleFiles;
    }
*/
    @Override
    public boolean isExternal() {
      return true;
    }

    @Override
    public Locale getLocale() {
      return baseLanguage.getLocale();
    }

    @Override
    public Contributor[] getMaintainers() {
      return baseLanguage.getMaintainers();
    }

    @Override
    public String[] getCountries() {
      return baseLanguage.getCountries();
    }

    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws Exception {
      return baseLanguage.getRelevantRules(messages, null, altLanguages);
    }

    @Nullable @Override
    public String getVariant() {
      return baseLanguage.getVariant();
    }

    @Override
    public CaseConverter getCaseConverter() throws Exception {
        return baseLanguage.getCaseConverter();
    }

    @Override
    public List<String> getDefaultEnabledRulesForVariant() {
      return baseLanguage.getDefaultEnabledRulesForVariant();
    }

    @Override
    public List<String> getDefaultDisabledRulesForVariant() {
      return baseLanguage.getDefaultDisabledRulesForVariant();
    }

    @Nullable
    @Override
    public LanguageModel getLanguageModel() throws Exception {
      return baseLanguage.getLanguageModel();
    }

    @Override
    public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel) throws Exception {
      return baseLanguage.getRelevantLanguageModelRules(messages, languageModel);
    }

    @Override
    public List<? extends Rule> getRelevantWord2VecModelRules(ResourceBundle messages, Word2VecModel word2vecModel) throws Exception {
      return baseLanguage.getRelevantWord2VecModelRules(messages, word2vecModel);
    }

    @Nullable
    @Override
    public Language getDefaultLanguageVariant() {
      return baseLanguage.getDefaultLanguageVariant();
    }

    @Override
    public Disambiguator getDisambiguator() throws Exception {
      return baseLanguage.getDisambiguator();
    }

    @Override
    public Tagger getTagger() throws Exception {
      return baseLanguage.getTagger();
    }

    @Override
    public SentenceTokenizer getSentenceTokenizer() throws Exception {
      return baseLanguage.getSentenceTokenizer();
    }

    @Override
    public Tokenizer getWordTokenizer() throws Exception {
      return baseLanguage.getWordTokenizer();
    }

    @Nullable @Override
    public Chunker getChunker() throws Exception {
      return baseLanguage.getChunker();
    }

    @Nullable @Override
    public Chunker getPostDisambiguationChunker() throws Exception {
      return baseLanguage.getPostDisambiguationChunker();
    }

    @Nullable @Override
    public Synthesizer getSynthesizer() throws Exception {
      return baseLanguage.getSynthesizer();
    }

  }
}
