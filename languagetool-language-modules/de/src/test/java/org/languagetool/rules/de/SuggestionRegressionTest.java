/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.databroker.DefaultGermanResourceDataBroker;
import org.languagetool.databroker.StringProcessor;

import java.nio.charset.*;
import java.nio.file.*;
import java.util.stream.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.*;

import static org.junit.Assert.fail;

public class SuggestionRegressionTest {

/*
GTODO Test this test.
*/
  @Test
  @Ignore("a bit too slow to run every time")
  public void testSuggestions() throws Exception {
    GermanyGerman german = new GermanyGerman();
    // This broker knows how to load words from the class path.
    DefaultGermanResourceDataBroker broker = new DefaultGermanResourceDataBroker(german, german.getClass().getClassLoader());
    german.setDataBroker(broker);
    GermanSpellerRule rule = german.createSpellerRule(null, null);
    JLanguageTool lt = new JLanguageTool(german);
    Path path = broker.getPath("/resources/suggestions.txt");
    AtomicBoolean different = new AtomicBoolean(false);
    List<String> results = broker.loadWords(path, StandardCharsets.UTF_8, new StringProcessor<String>()
    {
        @Override
        public boolean shouldSkip(String line) {
            if (line.startsWith("#")) {
                return false;
            }
            String[] parts = line.split(" => ?");
            String word = parts[0];
            try {
                if (rule.match(lt.analyzeText(word).get(0)).length == 0) {
                    return true;
                }
            } catch(Exception e) {
                throw new RuntimeException(String.format("Unable to analyze text: %1$s", word), e);
            }
            return false;
        }
        @Override
        public String getProcessed(String line) throws Exception {
            if (line.startsWith("#")) {
                return line;
            }
            String[] parts = line.split(" => ?");
            String word = parts[0];
            List<String> oldSuggestions = parts.length > 1 ? Arrays.asList(parts[1].split(", ")) : Collections.emptyList();
            List<String> newSuggestions = rule.getSuggestions(word);
            String thisResult = word + " => " + String.join(", ", newSuggestions);
            //result.append(thisResult).append("\n");
            if (!oldSuggestions.equals(newSuggestions)) {
              System.err.println("Input   : " + word);
              System.err.println("Expected: " + oldSuggestions);
              System.err.println("Got     : " + newSuggestions);
              different.set(true);
            }
            return thisResult;
        }
    });
    Files.write(path, results.stream().collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8));
/*
GTODO Clean up

    String file = "src/test/resources/suggestions.txt";
    List<String> lines = Files.readAllLines(Paths.get(file));
    boolean different = false;
    StringBuilder result = new StringBuilder();
    JLanguageTool lt = new JLanguageTool(german);
    for (String line : lines) {
      if (line.startsWith("#")) {
        result.append(line).append("\n");
        continue;
      }
      String[] parts = line.split(" => ?");
      String word = parts[0];
      if (rule.match(lt.analyzeText(word).get(0)).length == 0) {
        System.out.println("No error, removing from file: " + word);
        continue;
      }
      List<String> oldSuggestions = parts.length > 1 ? Arrays.asList(parts[1].split(", ")) : Collections.emptyList();
      List<String> newSuggestions = rule.getSuggestions(word);
      String thisResult = word + " => " + String.join(", ", newSuggestions);
      result.append(thisResult).append("\n");
      if (!oldSuggestions.equals(newSuggestions)) {
        System.err.println("Input   : " + word);
        System.err.println("Expected: " + oldSuggestions);
        System.err.println("Got     : " + newSuggestions);
        different = true;
      }
    }

    try (FileWriter fw = new FileWriter(file)) {
      fw.write(result.toString());
    }
    */
    if (different.get()) {
      fail(String.format("There were differences between expected and real suggestions, the differences have been written to: %1$, please check them. If they are okay, commit the file, otherwise roll back the changes.", path));
    }
  }



}
