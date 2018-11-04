/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.rules.spelling.morfologik;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;
import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;
import org.languagetool.databroker.ResourceDataBroker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Morfologik-based spell checker.
 */
public class MorfologikSpeller {

  // Speed up the server use case, where rules get initialized for every call.
  // See https://github.com/morfologik/morfologik-stemming/issues/69 for confirmation that
  // Dictionary is thread-safe:
  /*
  private static final LoadingCache<String, Dictionary> dictCache = CacheBuilder.newBuilder()
      //.maximumSize(0)
      .expireAfterWrite(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Dictionary>() {
        @Override
        public Dictionary load(@NotNull String fileInClassPath) throws IOException {
          return Dictionary.read(dataBroker.getFromResourceDirAsUrl(fileInClassPath));
        }
      });
*/
/*
GTODO: Clean up
  private static final LoadingCache<ResourceDataBroker, LoadingCache<String, Dictionary>> brokerCache = CacheBuilder.newBuilder ()
     .weakKeys ()
     .build(new CacheLoader<ResourceDataBroker, LoadingCache<String, Dictionary>>() {
         @Override
         public LoadingCache<String, Dictionary> load(@NotNull ResourceDataBroker dataBroker) throws IOException {
             return CacheBuilder.newBuilder()
                     //.maximumSize(0)
                     .expireAfterWrite(10, TimeUnit.MINUTES)
                     .build(new CacheLoader<String, Dictionary>() {
                       @Override
                       public Dictionary load(@NotNull String dictFile) throws IOException {
                         return Dictionary.read(dataBroker.getFromResourceDirAsUrl(dictFile));
                       }
                     });
                 }
             });
*/
  private Dictionary dictionary;
  private Speller speller;

  /**
   * Creates a speller with the given maximum edit distance.
   * @param fileInClassPath path in classpath to morfologik dictionary
   */
/*
GTODO: Clean up
  public MorfologikSpeller(final String dictFile, int maxEditDistance, final ResourceDataBroker dataBroker) {
      LoadingCache<String, Dictionary> dbCache = brokerCache.getUnchecked(dataBroker);
      init(dbCache.getUnchecked(dictFile), maxEditDistance);
  }
*/
  /**
   * Creates a speller with a maximum edit distance of one.
   * @param fileInClassPath path in classpath to morfologik dictionary
   */
   /*
   GTODO: Clean up
  public MorfologikSpeller(String fileInClassPath, ResourceDataBroker dataBroker) throws IOException {
    this(fileInClassPath, 1, dataBroker);
  }
*/
  public MorfologikSpeller(Dictionary dictionary, int maxEditDistance) {
      if (maxEditDistance <= 0) {
        throw new IllegalArgumentException("maxEditDistance must be > 0: " + maxEditDistance);
      }
      this.dictionary = dictionary;
      speller = new Speller(dictionary, maxEditDistance);
  }
/*
  private void init (Dictionary dictionary, int maxEditDistance) {
      if (maxEditDistance <= 0) {
        throw new RuntimeException("maxEditDistance must be > 0: " + maxEditDistance);
      }
      this.dictionary = dictionary;
      speller = new Speller(dictionary, maxEditDistance);
  }
*/
  public boolean isMisspelled(String word) {
    return word.length() > 0
            && !SpellingCheckRule.LANGUAGETOOL.equals(word)
            && speller.isMisspelled(word);
  }

  public List<String> getSuggestions(String word) {
    List<String> suggestions = new ArrayList<>();
    suggestions.addAll(speller.findReplacements(word));
    suggestions.addAll(speller.replaceRunOnWords(word));
    // capitalize suggestions if necessary
    // GTODO Uppercase check should be via CaseConverter
    if (dictionary.metadata.isConvertingCase() && StringTools.startsWithUppercase(word)) {
      for (int i = 0; i < suggestions.size(); i++) {
        String uppercaseFirst = StringTools.uppercaseFirstChar(suggestions.get(i));
        // do not use capitalized word if it matches the original word or it's mixed case
        if (uppercaseFirst.equals(word) || StringTools.isMixedCase(suggestions.get(i))) {
          uppercaseFirst = suggestions.get(i);
        }
        // remove capitalized duplicates
        int auxIndex = suggestions.indexOf(uppercaseFirst);
        if (auxIndex > i) {
          suggestions.remove(auxIndex);
        }
        if (auxIndex > -1 && auxIndex < i) {
          suggestions.remove(i);
          i--;
        } else {
          suggestions.set(i, uppercaseFirst);
        }
      }
    }
    return suggestions;
  }

  /**
   * Determines whether the dictionary uses case conversions.
   * @return True when the speller uses spell conversions.
   * @since 2.5
   */
  public boolean convertsCase() {
    return speller.convertsCase();
  }

}
