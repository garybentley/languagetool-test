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
import java.nio.charset.*;
import java.io.*;

import org.languagetool.tokenizers.*;
import org.languagetool.tools.*;

public class PairedWordsDataLoader {

    public static List<Map<String, String>> loadWords(InputStream stream, WordTokenizer tokenizer, Charset charset)
            throws IOException {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
      List<Map<String, String>> list = new ArrayList<>();
      try (
        InputStreamReader isr = new InputStreamReader(stream, charset);
        BufferedReader br = new BufferedReader(isr))
      {
        String line;
        while ((line = br.readLine()) != null) {
          line = line.trim();
          if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
            continue;
          }

          String[] parts = line.split("=");
          if (parts.length != 2) {
            throw new IOException("Expected exactly 1 '=' character. Line: " + line);
          }

          String[] wrongForms = parts[0].split("\\|"); // multiple incorrect forms
          for (String wrongForm : wrongForms) {
            int wordCount = 0;
            List<String> tokens = tokenizer.tokenize(wrongForm);
            for (String token : tokens) {
              if (!StringTools.isWhitespace(token)) {
                wordCount++;
              }
            }
            // grow if necessary
            for (int i = list.size(); i < wordCount; i++) {
              list.add(new HashMap<>());
            }
            list.get(wordCount - 1).put(wrongForm, parts[1]);
          }
        }
      }
      // seal the result (prevent modification from outside this class)
      List<Map<String,String>> result = new ArrayList<>();
      for (Map<String, String> map : list) {
        result.add(Collections.unmodifiableMap(map));
      }
      return Collections.unmodifiableList(result);
    }

}
