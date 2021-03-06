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

import org.languagetool.JLanguageTool;

import java.io.InputStream;
import java.io.IOException;
import java.util.*;
import java.nio.charset.*;

import org.languagetool.databroker.ResourceDataBroker;

/**
 * Load replacement data from a UTF-8 file. One replacement per line,
 * word and its replacement(s) separated by an equals sign. Both the
 * word and the replacements can have more than one form if separated
 * by pipe symbols.
 * @since 3.0
 */
public final class SimpleReplaceDataLoader {

/*
GTODO Clean up
  public Map<String, List<String>> loadWords(InputStream stream, Charset charset) throws IOException {
      if (charset == null) {
          charset = StandardCharsets.UTF_8;
      }
      Map<String, List<String>> map = new HashMap<>();
      try (Scanner scanner = new Scanner(stream, charset.name())) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          if (line.isEmpty() || line.charAt(0) == '#') { // # = comment
            continue;
          }
          String[] parts = line.split("=");
          if (parts.length != 2) {
            throw new IOException("Error in line '" + line + "', expected format 'word=replacement'");
          }
          String[] wrongForms = parts[0].split("\\|");
          List<String> replacements = Arrays.asList(parts[1].split("\\|"));
          for (String wrongForm : wrongForms) {
            map.put(wrongForm, replacements);
          }
        }
      }
      return Collections.unmodifiableMap(map);
  }
*/
  /**
   * Load replacement rules from a utf-8 file in the classpath.
   */
/*
GTODO Clean up
  public Map<String, List<String>> loadWords(String path, ResourceDataBroker dataBroker) {
    InputStream stream = dataBroker.getFromRulesDirAsStream(path);
    try {
        return loadWords(stream, StandardCharsets.UTF_8);
    } catch(Exception e) {
        throw new RuntimeException("Unable to load words from path: " + dataBroker.getFromResourceDirAsUrl(path), e);
    }
  }
*/
}
