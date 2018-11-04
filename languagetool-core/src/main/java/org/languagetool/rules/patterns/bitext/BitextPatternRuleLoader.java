/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns.bitext;

import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilterCreator;

import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads {@link PatternRule}s from an XML file.
 *
 * @author Marcin Miłkowski
 */
public class BitextPatternRuleLoader extends DefaultHandler {
/*
GTODO: Clean up
  public final List<BitextPatternRule> getRules(InputStream is, RuleFilterCreator ruleFilterCreator) throws IOException {
    List<BitextPatternRule> rules;
      BitextPatternRuleHandler handler = new BitextPatternRuleHandler(ruleFilterCreator);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      saxParser.parse(is, handler);
      rules = handler.getBitextRules();
      return rules;
  }
*/
}
