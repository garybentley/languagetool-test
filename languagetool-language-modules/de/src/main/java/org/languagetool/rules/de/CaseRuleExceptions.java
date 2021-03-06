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
package org.languagetool.rules.de;

import org.languagetool.JLanguageTool;

import java.io.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @since 3.0
 */
final class CaseRuleExceptions {

  private Set<String> exceptions;
  //GTODO  = loadExceptions("/de/case_rule_exceptions.txt");

  private CaseRuleExceptions(Set<String> exceptions) {
      this.exceptions = Objects.requireNonNull(exceptions, "Exceptions must be provided.");
  }

  public Set<String> getExceptions() {
    return exceptions;
  }

  public Set<Pattern[]> getExceptionPatterns() {
    HashSet<Pattern[]> exceptionPatterns = new HashSet<>(250);
    for (String phrase : exceptions) {
      String[] parts = phrase.split(" ");
      Pattern[] patterns = new Pattern[parts.length];
      for (int j = 0; j < parts.length; j++) {
        patterns[j] = Pattern.compile(parts[j]);
      }
      exceptionPatterns.add(patterns);
    }
    return Collections.unmodifiableSet(exceptionPatterns);
  }
/*
GTODO Clean up
  private static Set<String> loadExceptions(String path) {
    Set<String> result = new HashSet<>();
    try (
      InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
      InputStreamReader reader = new InputStreamReader(stream, "utf-8");
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty() || line.startsWith("#")) {
          continue;
        }
        if (line.matches("^\\s.*") || line.matches(".*\\s$")) {
          throw new RuntimeException("Invalid line in " + path + ", starts or ends with whitespace: '" + line + "'");
        }
        result.add(line);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not load case rule exceptions from " + path, e);
    }
    return Collections.unmodifiableSet(result);
  }
  */

}
