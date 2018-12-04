/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.ca;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.WordTagger;
import org.languagetool.rules.patterns.CaseConverter;

/**
 * Catalan Tagger
 *
 * @author Jaume Ortolà 
 */
public class CatalanTagger extends BaseTagger {

  private static final Pattern ADJ_PART_FS = Pattern.compile("VMP00SF.|A[QO].[FC][SN].");
  private static final Pattern VERB = Pattern.compile("V.+");
  //private static final Pattern NOUN = Pattern.compile("NC.+");
  private boolean variant;
  private CaseConverter caseConverter;

  private static final Pattern PREFIXES_FOR_VERBS = Pattern.compile("(auto)(.*[aeiouàéèíòóïü].+[aeiouàéèíòóïü].*)",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
/*
GTODO
  @Override
  public String getManualAdditionsFileName() {
    return "/ca/manual-tagger.txt";
  }
*/
  public CatalanTagger(Dictionary baseDict, WordTagger tagger, CaseConverter caseCon, boolean variant) {
    super(baseDict, tagger, caseCon, false);
    this.variant = variant;
    this.caseConverter = Objects.requireNonNull(caseCon, "Case converter must be provided.");
  }
/*
GTODO
  public CatalanTagger(Language language) {
    super("/ca/" + language.getShortCodeWithCountryAndVariant() + ".dict",  new Locale("ca"), false);
    variant = language.getVariant();
  }
  */
  /*
  GTODO
  @Override
  public String getManualRemovalsFileName() {
    return "/ca/removed-tagger.txt";
  }
  */
  /*
   GTODO
  @Override
  public boolean overwriteWithManualTagger(){
    return false;
  }
*/

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());

    for (String word : sentenceTokens) {
      // This hack allows all rules and dictionary entries to work with
      // typewriter apostrophe
      boolean containsTypewriterApostrophe = false;
      boolean containsTypographicApostrophe = false;
      if (word.length() > 1) {
        if (word.contains("'")) {
          containsTypewriterApostrophe = true;
        }
        if (word.contains("’")) {
          containsTypographicApostrophe = true;
          word = word.replace("’", "'");
        }
      }
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = caseConverter.toLowerCase(word);
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = caseConverter.isMixedCase(word);
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));

      // normal case:
      addTokens(taggerTokens, l);
      // tag non-lowercase (alluppercase or startuppercase), but not mixedcase
      // word with lowercase word tags:
      if (!isLowercase && !isMixedCase) {
        List<AnalyzedToken> lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
        addTokens(lowerTaggerTokens, l);
      }

      // additional tagging with prefixes
      if (l.isEmpty() && !isMixedCase) {
        addTokens(additionalTags(word, dictLookup), l);
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }

      AnalyzedTokenReadings atr = new AnalyzedTokenReadings(l, pos);
      if (containsTypewriterApostrophe) {
        List<ChunkTag> listChunkTags = new ArrayList<>();
        listChunkTags.add(new ChunkTag("containsTypewriterApostrophe"));
        atr.setChunkTags(listChunkTags);
      }
      if (containsTypographicApostrophe) {
        List<ChunkTag> listChunkTags = new ArrayList<>();
        listChunkTags.add(new ChunkTag("containsTypographicApostrophe"));
        atr.setChunkTags(listChunkTags);
      }

      tokenReadings.add(atr);
      pos += word.length();
    }

    return tokenReadings;
  }

  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, IStemmer stemmer) {
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());
    List<AnalyzedToken> additionalTaggedTokens = new ArrayList<>();
    //Any well-formed adverb with suffix -ment is tagged as an adverb (RG)
    //Adjectiu femení singular o participi femení singular + -ment
    if (word.endsWith("ment")){
      final String lowerWord = caseConverter.toLowerCase(word);
      final String possibleAdj = lowerWord.replaceAll("^(.+)ment$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleAdj, dictLookup.lookup(possibleAdj));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = ADJ_PART_FS.matcher(posTag);
          if (m.matches()) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "RG", lowerWord));
            return additionalTaggedTokens;
          }
        }
      }
    }
    //Any well-formed verb with prefixes is tagged as a verb copying the original tags
    Matcher matcher = PREFIXES_FOR_VERBS.matcher(word);
    if (matcher.matches()) {
      final String possibleVerb = matcher.group(2).toLowerCase();
      List<AnalyzedToken> taggerTokens = asAnalyzedTokenList(possibleVerb, dictLookup.lookup(possibleVerb));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = VERB.matcher(posTag);
          if (m.matches()) {
            String lemma = matcher.group(1).toLowerCase().concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }
    // Any well-formed noun with prefix ex- is tagged as a noun copying the original tags
    /*if (word.startsWith("ex")) {
      final String lowerWord = word.toLowerCase(conversionLocale);
      final String possibleNoun = lowerWord.replaceAll("^ex(.+)$", "$1");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleNoun,dictLookup.lookup(possibleNoun));
      for (AnalyzedToken taggerToken : taggerTokens) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          final Matcher m = NOUN.matcher(posTag);
          if (m.matches()) {
            String lemma = "ex".concat(taggerToken.getLemma());
            additionalTaggedTokens.add(new AnalyzedToken(word, posTag, lemma));
          }
        }
      }
      return additionalTaggedTokens;
    }*/
    // Interpret deprecated characters of "ela geminada"
    // U+013F LATIN CAPITAL LETTER L WITH MIDDLE DOT
    // U+0140 LATIN SMALL LETTER L WITH MIDDLE DOT
    if (word.contains("\u0140") || word.contains("\u013f")) {
      final String lowerWord = caseConverter.toLowerCase(word);
      final String possibleWord = lowerWord.replaceAll("\u0140", "l·");
      return asAnalyzedTokenList(word, dictLookup.lookup(possibleWord));
    }

    // adjectives -iste in Valencian variant
    if (variant && word.endsWith("iste")) {
      final String lowerWord = caseConverter.toLowerCase(word);
      final String possibleAdjNoun = lowerWord.replaceAll("^(.+)iste$", "$1ista");
      List<AnalyzedToken> taggerTokens;
      taggerTokens = asAnalyzedTokenList(possibleAdjNoun, dictLookup.lookup(possibleAdjNoun));
      for (AnalyzedToken taggerToken : taggerTokens ) {
        final String posTag = taggerToken.getPOSTag();
        if (posTag != null) {
          if (posTag.equals("NCCS000")) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "NCMS000", possibleAdjNoun));
          }
          if (posTag.equals("AQ0CS0")) {
            additionalTaggedTokens.add(new AnalyzedToken(word, "AQ0MS0", possibleAdjNoun));
          }
          if (!additionalTaggedTokens.isEmpty()) {
            return additionalTaggedTokens;
          }
        }
      }
    }

    return null;
  }

  private void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }

}
