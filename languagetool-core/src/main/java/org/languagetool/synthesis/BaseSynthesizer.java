/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Marcin Miłkowski
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
package org.languagetool.synthesis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;

public class BaseSynthesizer implements Synthesizer {

  private Set<String> possibleTags;

  //GTODO private final String tagFileName;
  //GTODO private final String resourceFileName;
  private final IStemmer stemmer;
  //GTODO protected final ResourceDataBroker dataBroker;

  //private volatile Dictionary dictionary;

  /**
   * Create a synthesizer using the stemmer and tags.
   *
   * @param stemmer The stemmer to use.
   * @param possibleTags The tags.
   */
  public BaseSynthesizer(IStemmer stemmer, Set<String> possibleTags) {
     this.stemmer = Objects.requireNonNull(stemmer, "Stemmer must be provided.");
     this.possibleTags = Objects.requireNonNull(possibleTags, "Possible tags must be provided.");
      //GTODO String resourceFileName, String tagFileName,
    //GTODO this.resourceFileName = resourceFileName;
    //GTODO this.tagFileName = tagFileName;
    //GTODO this.dataBroker = dataBroker;
    //this.dictionary = dict;
    //this.stemmer = createStemmer();
    //this.stemmer = new DictionaryLookup(dictionary);
  }

  /**
   * Returns the {@link Dictionary} used for this synthesizer.
   * The dictionary file can be defined in the {@link #BaseSynthesizer(String, String) constructor}.
   *
   * @param dataBroker The data broker to use for resource lookup.
   * @throws IOException In case the dictionary cannot be loaded.
   */
   /*
  protected Dictionary getDictionary() throws IOException {
    Dictionary dict = this.dictionary;
    if (dict == null) {
      synchronized (this) {
        dict = this.dictionary;
        if (dict == null) {
          URL url = dataBroker.getFromResourceDirAsUrl(resourceFileName);
          this.dictionary = dict = Dictionary.read(url);
        }
      }
    }
    return dict;
  }
  */

  /**
   * Creates a new {@link IStemmer} based on the configured {@link #getDictionary() dictionary}.
   * The result must not be shared among threads.
   * @since 2.3
   */
   /*
   GTODO: Clean up
  protected IStemmer createStemmer() {
    try {
      return new DictionaryLookup(dictionary);//GTODO getDictionary());
    } catch (IOException e) {
      throw new RuntimeException("Could not load dictionary", e);
    }
  }
*/
  /**
   * Lookup the inflected forms of a lemma defined by a part-of-speech tag.
   * @param lemma the lemma to be inflected.
   * @param posTag the desired part-of-speech tag.
   * @param results the list to collect the inflected forms.
   */
  protected void lookup(String lemma, String posTag, List<String> results) {
    synchronized (this) { // the stemmer is not thread-safe
      List<WordData> wordForms = stemmer.lookup(lemma + "|" + posTag);
      for (WordData wd : wordForms) {
        results.add(wd.getStem().toString());
      }
    }
  }

  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * @param token AnalyzedToken to be inflected.
   * @param posTag The desired part-of-speech tag.
   * @return inflected words, or an empty array if no forms were found
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) {
    List<String> wordForms = new ArrayList<>();
    lookup(token.getLemma(), posTag, wordForms);
    return wordForms.toArray(new String[wordForms.size()]);
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag,
      boolean posTagRegExp) {
    if (posTagRegExp) {
      //GTODO initPossibleTags();
      Pattern p = Pattern.compile(posTag);
      List<String> results = new ArrayList<>();
      for (String tag : possibleTags) {
        Matcher m = p.matcher(tag);
        if (m.matches()) {
          lookup(token.getLemma(), tag, results);
        }
      }
      return results.toArray(new String[results.size()]);
    }
    return synthesize(token, posTag);
  }

  @Override
  public String getPosTagCorrection(String posTag) {
    return posTag;
  }

  public Set<String> getPossibleTags() {
      return possibleTags;
  }

  /**
   * @since 2.5
   * @return the stemmer interface to be used.
   */
  public IStemmer getStemmer() {
    return stemmer;
  }
/*
GTODO: Clean up
  protected void initPossibleTags() throws IOException {
    List<String> tags = possibleTags;
    if (tags == null) {
      synchronized (this) {
        tags = possibleTags;
        if (tags == null) {
          try (InputStream stream = dataBroker.getFromResourceDirAsStream(tagFileName)) {
            possibleTags = SynthesizerTools.loadWords(stream);
          }
        }
      }
    }
  }
*/
}
