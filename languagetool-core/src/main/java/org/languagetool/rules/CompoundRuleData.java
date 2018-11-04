/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * Data about words that are compounds and should thus not be written
 * as separate words.
 * @since 3.0
 */
public class CompoundRuleData {

  private Set<String> incorrectCompounds = new HashSet<>();
  private Set<String> noDashSuggestion = new HashSet<>();
  private Set<String> noDashLowerCaseSuggestion = new HashSet<>();
  private Set<String> onlyDashSuggestion = new HashSet<>();

  public CompoundRuleData(Set<String> incorrectCompounds, Set<String> noDashSuggestions, Set<String> noDashLowerCaseSuggestions, Set<String> onlyDashSuggestions) {
      this.incorrectCompounds = incorrectCompounds;
      this.noDashSuggestion = noDashSuggestions;
      this.noDashLowerCaseSuggestion = noDashLowerCaseSuggestions;
      this.onlyDashSuggestion = onlyDashSuggestions;
  }
/*
GTODO: Clean up
  public CompoundRuleData(String path) {
    this(new String[] {path});
  }

  public CompoundRuleData(String... paths) {
      this(JLanguageTool.getDataBroker(), paths);
  }

  public CompoundRuleData(ResourceDataBroker dataBroker, String... paths) {
    for (String path : paths) {
      try {
        loadCompoundFile(path, dataBroker);
      } catch (IOException e) {
        throw new RuntimeException("Could not load compound data from " + path, e);
      }
    }
  }
*/
  Set<String> getIncorrectCompounds() {
    return Collections.unmodifiableSet(incorrectCompounds);
  }

  Set<String> getNoDashSuggestion() {
    return Collections.unmodifiableSet(noDashSuggestion);
  }

  Set<String> getOnlyDashSuggestion() {
    return Collections.unmodifiableSet(onlyDashSuggestion);
  }

  Set<String> getNoDashLowerCaseSuggestion() {
	return Collections.unmodifiableSet(noDashLowerCaseSuggestion);
  }

/*
GTODO: Clean up
  private void loadCompoundFile(String path, ResourceDataBroker dataBroker) throws IOException {
    try (
      InputStream stream = dataBroker.getFromResourceDirAsStream(path);
      InputStreamReader reader = new InputStreamReader(stream, "utf-8");
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;     // ignore comments
        }
        line = line.replace('-', ' ');  // the set contains the incorrect spellings, i.e. the ones without hyphen
        validateLine(path, line);
        if (line.endsWith("+")) {
          line = removeLastCharacter(line);
          noDashSuggestion.add(line);
        } else if (line.endsWith("*")) {
          line = removeLastCharacter(line);
          onlyDashSuggestion.add(line);
        } else if (line.endsWith("?")) { // github issue #779
          line = removeLastCharacter(line);
          noDashSuggestion.add(line);
          noDashLowerCaseSuggestion.add(line);
        } else if (line.endsWith("$")) { // github issue #779
          line = removeLastCharacter(line);
          noDashLowerCaseSuggestion.add(line);
        }
        incorrectCompounds.add(line);
      }
    }
  }

  private void validateLine(String path, String line) throws IOException {
    String[] parts = line.split(" ");
    if (parts.length == 1) {
      throw new RuntimeException("Not a compound in file " + path + ": " + line);
    }
    if (parts.length > AbstractCompoundRule.MAX_TERMS) {
      throw new RuntimeException("Too many compound parts in file " + path + ": " + line + ", maximum allowed: " + AbstractCompoundRule.MAX_TERMS);
    }
    if (incorrectCompounds.contains(line.toLowerCase())) {
      throw new RuntimeException("Duplicated word in file " + path + ": " + line);
    }
  }

  private String removeLastCharacter(String str) {
    return str.substring(0, str.length() - 1);
  }
*/
}
