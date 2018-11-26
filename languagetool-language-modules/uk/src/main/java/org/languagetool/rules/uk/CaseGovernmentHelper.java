package org.languagetool.rules.uk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Objects;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.uk.PosTagHelper;

public class CaseGovernmentHelper {

  private final Map<String, Set<String>> mappings;
  //CASE_GOVERNMENT_MAP = loadMap("/uk/case_government.txt");

/*
GTODO
  static {
    CASE_GOVERNMENT_MAP.put("згідно з", new HashSet<>(Arrays.asList("v_oru")));
  }
*/
  public CaseGovernmentHelper(Map<String, Set<String>> mapping) {
      this.mappings = Objects.requireNonNull(mapping, "Mapping must be provided.");
      this.mappings.put("згідно з", new HashSet<>(Arrays.asList("v_oru")));
  }

/*
GTODO
  private static Map<String, Set<String>> loadMap(String path, ResourceDataBroker dataBroker) {
    Map<String, Set<String>> result = new HashMap<>();
    try (InputStream is = dataBroker.getFromResourceDirAsStream(path);
        Scanner scanner = new Scanner(is, "UTF-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] parts = line.split(" ");
        String[] vidm = parts[1].split(":");
        result.put(parts[0], new LinkedHashSet<>(Arrays.asList(vidm)));
      }
      //        System.err.println("Found case governments: " + result.size());
      return result;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
*/
  public boolean hasCaseGovernment(AnalyzedTokenReadings analyzedTokenReadings, String rvCase) {
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( rvCase.equals("v_oru") && PosTagHelper.hasPosTagPart(token, "adjp:pasv") )
        return true;

      if( mappings.containsKey(token.getLemma())
          && mappings.get(token.getLemma()).contains(rvCase) )
        return true;
    }
    return false;
  }

  public Set<String> getInflections(String key) {
      return mappings.get(key);
  }

  public Set<String> getCaseGovernments(AnalyzedTokenReadings analyzedTokenReadings, String startPosTag) {
    LinkedHashSet<String> list = new LinkedHashSet<>();
    for(AnalyzedToken token: analyzedTokenReadings.getReadings()) {
      if( token.getPOSTag() != null
          && (token.getPOSTag().startsWith(startPosTag)
              || (startPosTag == "prep" && token.getPOSTag().equals("<prep>")) )
          && mappings.containsKey(token.getLemma()) ) {

        Set<String> rvList = mappings.get(token.getLemma());
        list.addAll(rvList);

        if( token.getPOSTag().contains("adjp:pasv") ) {
          rvList.add("v_oru");
        }
      }
    }
    return list;
  }

}
