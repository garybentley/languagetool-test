package org.languagetool.rules.uk;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;

/**
 * Loads extra helper dictionaries in plain text format
 * @since 2.9
 */
public class ExtraDictionaryLoader {
/*
GTODO REemove this class, no longer needed.
  public static Set<String> loadSet(String path, ResourceDataBroker dataBroker) {
    Set<String> result = new HashSet<>();
    try (InputStream is = dataBroker.getFromResourceDirAsStream(path);
         Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if( ! line.startsWith("#") ) {
          result.add(line);
        }
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
*/
/*
GTODO
  public static Map<String, List<String>> loadLists(String path, ResourceDataBroker dataBroker) {
    Map<String, List<String>> result = new HashMap<>();
    try (InputStream is = dataBroker.getFromRulesDirAsStream(path);
         Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if( ! line.startsWith("#") && ! line.trim().isEmpty() ) {
          String[] split = line.split(" *= *|\\|");
          List<String> list = Arrays.asList(split).subList(1, split.length);
          result.put(split[0], list);
        }
      }
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
*/
}
