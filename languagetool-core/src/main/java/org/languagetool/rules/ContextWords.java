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
package org.languagetool.rules;

import java.util.regex.Pattern;

public class ContextWords {

    public String[] matches = {"", ""};
    public String[] explanations = {"", ""};
    public Pattern[] words;
    public Pattern[] contexts;

    public ContextWords(Pattern word0, Pattern word1, String match0, String match1, Pattern context0, Pattern context1, String explanation0, String explanation1) {
        words = new Pattern[2];
        contexts = new Pattern[2];

        words[0] = word0;
        words[1] = word1;
        matches[0] = match0;
        matches[1] = match1;
        contexts[0] = context0;
        contexts[1] = context1;
        explanations[0] = explanation0;
        explanations[1] = explanation1;
    }
/*
  ContextWords() {
    words = new Pattern[2];
    contexts = new Pattern[2];
  }

  private String addBoundaries(String str) {
    String ignoreCase = "";
    if (str.startsWith("(?i)")) {
      str = str.substring(4);
      ignoreCase = "(?i)";
    }
    return ignoreCase + "\\b(" + str + ")\\b";
  }

  void setWord(int i, String word) {
    words[i] = Pattern.compile(addBoundaries(word));
  }

  void setContext(int i, String context) {
    contexts[i] = Pattern.compile(addBoundaries(context));
  }
*/
}
