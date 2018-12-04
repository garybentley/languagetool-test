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
package org.languagetool;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Tests a core class as its behavior depends on files in the classpath
 * that don't exist in core.
 */
public class LanguagesTest {

  @Test
  public void testGet() {
    List<Language> languages = Languages.get();
    List<Language> languagesWithDemo = Languages.getWithDemoLanguage();
    assertThat(languages.size() + 1, is(languagesWithDemo.size()));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetIsUnmodifiable() {
    List<Language> languages = Languages.get();
    languages.add(languages.get(0));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetWithDemoLanguageIsUnmodifiable() {
    List<Language> languages = Languages.getWithDemoLanguage();
    languages.add(languages.get(0));
  }

  @Test
  public void testGetLanguageForShortName() {
      // GTODO These tests should be changed to use the Language.LOCALE value.
    assertEquals("en-US", Languages.getLanguage("en-us").getLocale().toLanguageTag());
    assertEquals("en-US", Languages.getLanguage("EN-US").getLocale().toLanguageTag());
    assertEquals("en-US", Languages.getLanguage("en-US").getLocale().toLanguageTag());
    assertEquals("de", Languages.getLanguage("de").getLocale().toLanguageTag());
    assertEquals(null, Languages.getLanguage("xy"));
    assertEquals(null, Languages.getLanguage("YY-KK"));
  }

  @Test
  public void testIsLanguageSupported() {
    Assert.assertTrue(Languages.isLanguageSupported("xx"));
    Assert.assertTrue(Languages.isLanguageSupported("XX"));
    Assert.assertTrue(Languages.isLanguageSupported("en-US"));
    Assert.assertTrue(Languages.isLanguageSupported("en-us"));
    Assert.assertTrue(Languages.isLanguageSupported("EN-US"));
    Assert.assertTrue(Languages.isLanguageSupported("de"));
    Assert.assertTrue(Languages.isLanguageSupported("de-DE"));
    Assert.assertTrue(Languages.isLanguageSupported("de-DE-x-simple-language"));
    Assert.assertTrue(Languages.isLanguageSupported("de-DE-x-simple-LANGUAGE"));
    assertFalse(Languages.isLanguageSupported("yy-ZZ"));
    assertFalse(Languages.isLanguageSupported("zz"));
    assertFalse(Languages.isLanguageSupported("somthing totally invalid"));
  }
/*
GTODO These tests are no longer valid since the expected behaviour is to return null rather than throw an exception.
That's flow control through exceptions which I intensely dislike.  These tests should be changed to test for null.
Also they have some redundancy.
  @Test(expected=IllegalArgumentException.class)
  public void testIsLanguageSupportedInvalidCode() {
    Languages.isLanguageSupported("somthing-totally-inv-alid");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName1() {
    Languages.getLanguageForShortCode("de-");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName2() {
    Languages.getLanguageForShortCode("dexx");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testInvalidShortName3() {
    Languages.getLanguageForShortCode("xyz-xx");
  }
*/
  @Test
  public void testGetLanguageForName() {
    assertEquals("en-US", Languages.getLanguageForName("English (US)").getLocale().toLanguageTag());
    assertEquals("de", Languages.getLanguageForName("German").getLocale().toLanguageTag());
    assertEquals(null, Languages.getLanguageForName("Foobar"));
  }

  @Test
  public void testIsVariant() {
      /*
      GTODO This is not the business of the standalone to be testing whether a language has a variant.
      The relevant language module should test for this.

    Assert.assertTrue(Languages.getLanguageForShortCode("en-US").isVariant());
    Assert.assertTrue(Languages.getLanguageForShortCode("de-CH").isVariant());

    assertFalse(Languages.getLanguageForShortCode("en").isVariant());
    assertFalse(Languages.getLanguageForShortCode("de").isVariant());
    */
  }

  @Test
  public void testHasVariant() {
      /*
      GTODO This is not the business of the standalone to be testing whether a language has a variant.
      The relevant language module should test for this.
    Assert.assertTrue(Languages.getLanguageForShortCode("en").hasVariant());
    Assert.assertTrue(Languages.getLanguageForShortCode("de").hasVariant());

    assertFalse(Languages.getLanguageForShortCode("en-US").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("de-CH").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("ast").hasVariant());
    assertFalse(Languages.getLanguageForShortCode("pl").hasVariant());

    for (Language language : Languages.getWithDemoLanguage()) {
      if (language.hasVariant()) {
        assertNotNull("Language " + language + " needs a default variant", language.getDefaultLanguageVariant());
      }
    }
    */
  }

  @Test
  public void testGetLanguageForLocale() {
    assertEquals("de", Languages.getLanguage(Locale.GERMAN).getLocale().getLanguage());
    assertEquals("de", Languages.getLanguage(Locale.GERMANY).getLocale().getLanguage());
    assertEquals("de-DE", Languages.getLanguage(new Locale("de", "DE")).getLocale().toLanguageTag());
    assertEquals("de-AT", Languages.getLanguage(new Locale("de", "AT")).getLocale().toLanguageTag());
    assertEquals("en-US", Languages.getLanguage(new Locale("en", "US")).getLocale().toLanguageTag());
    assertEquals("en-GB", Languages.getLanguage(new Locale("en", "GB")).getLocale().toLanguageTag());
    // fallback to the language's default variant if not specified:
    assertEquals("en-US", Languages.getBestMatchLanguage(new Locale("en")).getLocale().toLanguageTag());
    assertEquals("de-DE", Languages.getBestMatchLanguage(new Locale("de")).getLocale().toLanguageTag());
    assertEquals("pl-PL", Languages.getBestMatchLanguage(new Locale("pl")).getLocale().toLanguageTag());
    // final fallback is everything else fails:
    // GTODO This is just bad, if you ask for Korean you should either get Korean or nothing since the caller would assume that they received Korean.  assertEquals("en-US", Languages.getLanguageForLocale(Locale.KOREAN).getShortCodeWithCountryAndVariant());
  }
}
