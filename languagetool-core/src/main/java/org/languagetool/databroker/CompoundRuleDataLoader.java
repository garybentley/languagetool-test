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
package org.languagetool.databroker;

import java.util.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.Path;

import org.languagetool.rules.AbstractCompoundRule;
import org.languagetool.rules.CompoundRuleData;

// GTODO: Add description.
public class CompoundRuleDataLoader {

    public static void loadCompoundRuleData(Path path, Set<String> incorrectCompounds, Set<String> noDashSuggestion, Set<String> noDashLowerCaseSuggestion,
                        Set<String> onlyDashSuggestion, Charset charset) throws IOException {
        path = path.toRealPath();
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
          try (Scanner scanner = new Scanner(path, charset.name())) {
            int lineNo = -1;
            String line;
            while (scanner.hasNextLine()) {
              line = scanner.nextLine();
              lineNo++;
              if (line.isEmpty() || line.charAt(0) == '#') {
                continue;     // ignore comments
              }
              line = line.replace('-', ' ');  // the set contains the incorrect spellings, i.e. the ones without hyphen
              validateLine(lineNo, line, incorrectCompounds);
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

    private static void validateLine(int lineNo, String line, Set<String> incorrectCompounds) throws IOException {
      String[] parts = line.split(" ");
      if (parts.length == 1) {
        throw new IOException(String.format("Not a compound at line(%1$s): %2$s", lineNo, line));
      }
      if (parts.length > AbstractCompoundRule.MAX_TERMS) {
        throw new IOException(String.format("Too many compound parts at line(%1$s): %2$s, maximum allowed: %3$s", lineNo, line, AbstractCompoundRule.MAX_TERMS));
      }
      if (incorrectCompounds.contains(line.toLowerCase())) {
        throw new IOException(String.format("Duplicated word at line(%1$s): %2$s", lineNo, line));
      }
    }

    private static String removeLastCharacter(String str) {
      return str.substring(0, str.length() - 1);
    }

}
