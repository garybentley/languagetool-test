/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.English;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.Languages;
import org.languagetool.language.Contributor;
import org.languagetool.rules.ConfusionSetLoader;
import org.languagetool.rules.Rule;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;
import org.languagetool.databroker.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Comparator.comparing;

/**
 * Command line tool to list supported languages and their number of rules.
 *
 * @author Daniel Naber
 */
@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
public final class RuleOverview {

  enum SpellcheckSupport {
    Full, NoSuggestion, None
  }

  public static void main(final String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: " + RuleOverview.class.getName() + " <webRoot>");
      System.exit(1);
    }
    final RuleOverview prg = new RuleOverview();
    prg.run(new File(args[0]));
  }

  private RuleOverview() {
    // no public constructor
  }

  private void run(File webRoot) throws Exception {
    System.out.println("<p><b>Rules in LanguageTool " + JLanguageTool.VERSION + "</b><br />");
    System.out.println("Date: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "</p>\n");
    System.out.println("<table class=\"tablesorter sortable\" style=\"width: auto\">");
    System.out.println("<thead>");
    System.out.println("<tr>");
    System.out.println("  <th valign='bottom' width=\"200\">Language</th>");
    System.out.println("  <th valign='bottom' align=\"left\" width=\"60\">XML<br/>rules</th>");
    System.out.println("  <th></th>");
    System.out.println("  <th align=\"left\" width=\"60\">Java<br/>rules</th>");
    System.out.println("  <th align=\"left\" width=\"60\">False<br/>friends</th>");
    System.out.println("  <th align=\"left\" width=\"60\">Spell<br/>check*</th>");
    System.out.println("  <th align=\"left\" width=\"60\">Confusion<br/>pairs</th>");
    //System.out.println("  <th valign='bottom' width=\"65\">Auto-<br/>detected</th>");
    System.out.println("  <th valign='bottom' align=\"left\" width=\"90\">Activity</th>");
    System.out.println("  <th valign='bottom' align=\"left\">Rule Maintainers</th>");
    System.out.println("</tr>");
    System.out.println("</thead>");
    System.out.println("<tbody>");
    final List<Language> sortedLanguages = getSortedLanguages();

    English en = new English();
    DefaultResourceDataBroker broker = DefaultResourceDataBroker.newClassPathInstance(en, en.getClass().getClassLoader());

    //setup false friends counting
    //final String falseFriendFile = JLanguageTool.getDataBroker().getRulesDir() + File.separator + "false-friends.xml";
    final String falseFriendRules = StringTools.readStream(broker.getRulesDirPathStream(DefaultResourceDataBroker.FALSE_FRIEND_RULES_FILE_NAME), DefaultResourceDataBroker.DEFAULT_CHARSET.name())
      .replaceAll("(?s)<!--.*?-->", "")
      .replaceAll("(?s)<rules.*?>", "");

    int overallJavaCount = 0;
    int langSpecificWebsiteCount = 0;
    RuleActivityOverview activity = new RuleActivityOverview();
    for (final Language lang : sortedLanguages) {
      if (lang.isVariant()) {
        continue;
      }
      System.out.print("<tr>");
      final String langCode = lang.getLocale().getLanguage();
      final File langSpecificWebsite = new File(webRoot, langCode);
      final List<String> variants = getVariantNames(sortedLanguages, lang);
      String variantsText = "";
      if (variants.size() > 0) {
        variantsText = "<br/><span class='langVariants'>Variants for: " + String.join(", ", variants) + "</span>";
      }
      if (langSpecificWebsite.isDirectory()) {
        System.out.print("<td valign=\"top\"><a href=\"../" + langCode + "/\">" + lang.getName() + "</a>" + variantsText + "</td>");
        langSpecificWebsiteCount++;
      } else {
        System.out.print("<td valign=\"top\">" + lang.getName() + " " + variantsText + "</td>");
      }

      int allRules = countRulesForLanguage(lang);
      if (allRules == 0) {
        System.out.println("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        final String ruleBase = "https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/"
                + langCode + "/src/main/resources/org/languagetool/rules/";
        System.out.print("<td valign=\"top\" align=\"right\">" + allRules + "</td>");
        System.out.print("<td valign=\"top\" align=\"right\">" +
            //"<a href=\"" + ruleBase + langCode + "/grammar.xml" + "\">Show</a> / " +
            "<a href=\"http://community.languagetool.org/rule/list?lang=" + langCode + "\">Browse</a>,&nbsp;" +
            "<a href=\"" + ruleBase + langCode + "/grammar.xml\">XML</a>" +
            "</td>");
      }

      // count Java rules:
      final File dir = new File("../languagetool-language-modules/" + langCode + "/src/main/java" +
              DefaultResourceDataBroker.DEFAULT_RULES_DIR + "/" + langCode);
      if (!dir.exists()) {
        System.out.print("<td valign=\"top\" align=\"right\">0</td>");
      } else {
        final File[] javaRules = dir.listFiles(new JavaFilter(lang.getName()));
        final int javaCount = javaRules.length;
        if (javaCount > 0) {
          final String sourceCodeLink =
                  "https://github.com/languagetool-org/languagetool/blob/master/languagetool-language-modules/"
                  + langCode + "/src/main/java/org/languagetool/rules/"
                  + langCode + "/";
          System.out.print("<td valign=\"top\" align=\"right\"><a href=\"" + sourceCodeLink + "\">" + javaCount + "</a></td>");
        } else {
          System.out.print("<td valign=\"top\" align=\"right\">" + javaCount + "</td>");
        }
        overallJavaCount++;
      }

      System.out.print("<td valign=\"top\" align=\"right\">" + countFalseFriendRules(falseFriendRules, lang) + "</td>");

      SpellcheckSupport spellcheckSupport = spellcheckSupport(lang, sortedLanguages);
      String spellSupportStr = "";
      if (spellcheckSupport == SpellcheckSupport.Full) {
        spellSupportStr = "✓";
      } else if (spellcheckSupport == SpellcheckSupport.NoSuggestion) {
        spellSupportStr = "<span title='spell check without suggestions'>(✓)</span>";
      }
      System.out.print("<td valign=\"top\" align=\"right\">" + spellSupportStr + "</td>");

      System.out.print("<td valign=\"top\" align=\"right\">" + countConfusionPairs(lang) + "</td>");

      // activity:
      int commits = activity.getActivityFor(lang, 365/2);
      int width = (int) Math.max(commits * 0.5, 1);
      String images = "";
      if (width > 50) {
        images += "<img title='" + commits + " commits in the last 6 months' src='../images/bar-end.png' width='22' height='10'/>";
        width = 50;
      }
      images += "<img title='" + commits + " commits in the last 6 months' src='../images/bar.png' width='" + width + "' height='10'/>";
      System.out.print("<td valign=\"top\" align=\"right\"><span style='display:none'>" + commits + "</span>" + images + "</td>");

      // maintainer information:
      String maintainerInfo = getMaintainerInfo(lang);
      String maintainerText;
      boolean greyOutMaintainer = false;
      if (lang.getMaintainedState() != LanguageMaintainedState.ActivelyMaintained) {
        maintainerText = "<span class='maintainerNeeded'><a href='http://wiki.languagetool.org/tasks-for-language-maintainers'>Looking for maintainer</a></span> - ";
        greyOutMaintainer = true;
      } else {
        maintainerText = "";
      }
      if (greyOutMaintainer) {
        maintainerInfo = "<span class='previousMaintainer'><br>previous maintainer: " + maintainerInfo + "</span>";
      }
      System.out.print("<td valign=\"top\" align=\"left\">" + maintainerText + maintainerInfo + "</td>");

      System.out.println("</tr>");
    }

    if (overallJavaCount == 0) {
      throw new RuntimeException("No Java rules found - start this script from the languagetool-standalone directory");
    }
    if (langSpecificWebsiteCount == 0) {
      throw new RuntimeException("No language specific websites found - please let the web root parameter " +
              "point to the 'www' directory (current value: '" + webRoot + "')");
    }

    System.out.println("</tbody>");
    System.out.println("</table>");
  }

  private int countRulesForLanguage(Language lang) throws IOException {
      int count = 0;
      /*
      GTODO Not the way to do this (seriously checking for a string in a stream?).
    List<String> ruleFileNames = lang.getRuleFileNames();

    for (String ruleFileName : ruleFileNames) {
      final URL url = this.getClass().getResource(ruleFileName);
      if (url != null) {
        String xmlRules = StringTools.readStream(Tools.getStream(ruleFileName), "utf-8");
        xmlRules = xmlRules.replaceAll("(?s)<!--.*?-->", "");
        xmlRules = xmlRules.replaceAll("(?s)<rules.*?>", "");
        count += countXmlRules(xmlRules);
        count += countXmlRuleGroupRules(xmlRules);
      }
    }
    */
    return count;
  }

  private List<String> getVariantNames(List<Language> allLanguages, Language lang) {
    List<Language> variants = getVariants(allLanguages, lang);
    List<String> result = new ArrayList<>();
    for (Language l : variants) {
      result.add(l.getName().replaceAll(".*\\((.*?)\\).*", "$1").trim());
    }
    return result;
  }

  private List<Language> getVariants(List<Language> allLanguages, Language lang) {
    List<Language> variants = new ArrayList<>();
    for (Language sortedLanguage : allLanguages) {
      if (sortedLanguage.isVariant() && lang.getLocale().getLanguage().equals(sortedLanguage.getLocale().getLanguage())) {
        variants.add(sortedLanguage);
      }
    }
    return variants;
  }

  private List<Language> getSortedLanguages() {
    final List<Language> sortedLanguages = new ArrayList<>(Languages.get());
    sortedLanguages.sort(comparing(Language::getName));
    return sortedLanguages;
  }

  private int countXmlRules(String xmlRules) {
    return StringUtils.countMatches(xmlRules, "<rule ");  // rules with IDs
  }

  private int countXmlRuleGroupRules(String xmlRules) {
    return StringUtils.countMatches(xmlRules, "<rule>"); // rules in rule groups have no ID
  }

  private int countFalseFriendRules(String falseFriendRules, Language lang) {
    int pos = 0;
    int count = 0;
    while (true) {
      pos = falseFriendRules.indexOf("<pattern lang=\"" + lang.getLocale().getLanguage(), pos + 1);
      if (pos == -1) {
        break;
      }
      count++;
    }
    return count;
  }


  private SpellcheckSupport spellcheckSupport(Language lang, List<Language> allLanguages) throws Exception {
    if (spellcheckSupport(lang) != SpellcheckSupport.None) {
      return spellcheckSupport(lang);
    }
    List<Language> variants = getVariants(allLanguages, lang);
    for (Language variant : variants) {
      if (spellcheckSupport(variant) != SpellcheckSupport.None) {
        return spellcheckSupport(variant);
      }
    }
    return SpellcheckSupport.None;
  }

  private SpellcheckSupport spellcheckSupport(Language<ResourceDataBroker> lang) throws Exception {
    for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), null, Collections.emptyList())) {
      if (rule.isDictionaryBasedSpellingRule()) {
        if (rule instanceof HunspellNoSuggestionRule) {
          return SpellcheckSupport.NoSuggestion;
        } else {
          return SpellcheckSupport.Full;
        }
      }
    }
    return SpellcheckSupport.None;
  }

  private int countConfusionPairs(Language lang) throws Exception {
    DefaultResourceDataBroker broker = DefaultResourceDataBroker.newClassPathInstance(lang, lang.getClass().getClassLoader());
    String path = "/" + lang.getLocale().getLanguage() + "/confusion_sets.txt";
    if (broker.resourceDirPathExists(path))  {
        return broker.getConfusionSetFromResourcePath(path, DefaultResourceDataBroker.DEFAULT_CHARSET).size()/2;
    }
    return 0;
  }

  private String getMaintainerInfo(Language lang) {
    final StringBuilder maintainerInfo = new StringBuilder();
    if (lang.getMaintainers() != null) {
      for (Contributor contributor : lang.getMaintainers()) {
        if (!StringTools.isEmpty(maintainerInfo.toString())) {
          maintainerInfo.append(", ");
        }
        if (contributor.getUrl() != null) {
          maintainerInfo.append("<a href=\"");
          maintainerInfo.append(contributor.getUrl());
          maintainerInfo.append("\">");
        }
        maintainerInfo.append(contributor.getName());
        if (contributor.getUrl() != null) {
          maintainerInfo.append("</a>");
        }
      }
    }
    return maintainerInfo.toString();
  }

  private static class JavaFilter implements FileFilter {

    private final String langName;

    JavaFilter(String langName) {
      this.langName = langName;
    }

    @Override
    public boolean accept(final File f) {
      final String filename = f.getName();
      final boolean isAbstractTopClass = filename.endsWith(langName + "Rule.java");
      final boolean isSpellerClass = filename.endsWith("SpellerRule.java");
      return filename.endsWith(".java") && !isAbstractTopClass && !isSpellerClass;
    }

  }

}
