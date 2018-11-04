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

import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.MultiKeyProperties;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;

/**
 * Helper methods to list all supported languages and to get language objects
 * by their name or language code etc.
 * @since 2.9
 */
public final class Languages {

  //private static final List<Language> LANGUAGES = getAllLanguages();
  private static final String PROPERTIES_PATH = "META-INF/org/languagetool/language-module.properties";
  private static final String PROPERTIES_KEY = "languageClasses";

  private static LanguageProvider LANG_PROV = null;

  private Languages() {
  }

  public static void setLanguageProvider(LanguageProvider prov) {
      LANG_PROV = prov;
  }

  public static LanguageProvider getLanguageProvider() {
      if (LANG_PROV == null) {
          setLanguageProvider(new DefaultResourceLanguageProvider());
      }
      return LANG_PROV;
  }

  /**
   * Language classes are detected at runtime by searching the classpath for files named
   * {@code META-INF/org/languagetool/language-module.properties}. Those file(s)
   * need to contain a key {@code languageClasses} which specifies the fully qualified
   * class name(s), e.g. {@code org.languagetool.language.English}. Use commas to specify
   * more than one class.
   * @return an unmodifiable list of all supported languages
   */
   public static List<Language> get() {
       return Languages.getLanguageProvider().getAll(l -> !"xx".equals(l.getLocale().getLanguage()));
   }
   /*
  public static List<Language> get() {
    List<Language> result = new ArrayList<>();
    for (Language lang : LANGUAGES) {
      if (!"xx".equals(lang.getShortCode())) {  // skip demo language
        result.add(lang);
      }
    }
    return Collections.unmodifiableList(result);
  }
*/
  /**
   * Like {@link #get()} but the list contains also LanguageTool's internal 'Demo'
   * language, if available. Only useful for tests.
   * @return an unmodifiable list
   */
   /*
  public static List<Language> getWithDemoLanguage() {
    return LANGUAGES;
  }
  */
  public static List<Language> getWithDemoLanguage() {
      return Languages.getLanguageProvider().getAll();
  }
/*
  private static List<Language> getAllLanguages() {
    List<Language> languages = new ArrayList<>();
    Set<String> languageClassNames = new HashSet<>();
    try {
      Enumeration<URL> propertyFiles = Language.class.getClassLoader().getResources(PROPERTIES_PATH);
      while (propertyFiles.hasMoreElements()) {
        URL url = propertyFiles.nextElement();
        try (InputStream inputStream = url.openStream()) {
          // We want to be able to read properties file with duplicate key, as produced by
          // Maven when merging files:
          MultiKeyProperties props = new MultiKeyProperties(inputStream);
          List<String> classNamesStr = props.getProperty(PROPERTIES_KEY);
          if (classNamesStr == null) {
            throw new RuntimeException("Key '" + PROPERTIES_KEY + "' not found in " + url);
          }
          for (String classNames : classNamesStr) {
            String[] classNamesSplit = classNames.split("\\s*,\\s*");
            for (String className : classNamesSplit) {
              if (languageClassNames.contains(className)) {
                // avoid duplicates - this way we are robust against problems with the maven assembly
                // plugin which aggregates files more than once (in case the deployment descriptor
                // contains both <format>zip</format> and <format>dir</format>):
                continue;
              }
              languages.add(createLanguageObjects(url, className));
              languageClassNames.add(className);
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return Collections.unmodifiableList(languages);
  }
*/
/*
  private static Language createLanguageObjects(URL url, String className) {
    try {
      Class<?> aClass = Class.forName(className);
      Constructor<?> constructor = aClass.getConstructor();
      return (Language) constructor.newInstance();
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Class '" + className + "' specified in " + url + " could not be found in classpath", e);
    } catch (Exception e) {
      throw new RuntimeException("Object for class '" + className + "' specified in " + url + " could not created", e);
    }
  }
*/
  /**
   * Get the Language object for the given language name.
   *
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or {@code null} if there is no such language
   */
   /*
  @Nullable
  public static Language getLanguageForName(String languageName) {
    for (Language element : LANGUAGES) {
      if (languageName.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }
*/

  /**
     * Get the Language object for the given language name.
     *
     * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
     * @return a Language object or {@code null} if there is no such language
     */
    @Nullable
    public static Language getLanguageForName(String languageName) {
        return Languages.getLanguageProvider().get(l -> l.getName().equals(languageName));
    }

  /**
   * Get the Language object for the given language code.
   * @param langCode e.g. <code>en</code> or <code>en-US</code>
   * @throws IllegalArgumentException if the language is not supported or if the language code is invalid
   * @since 3.6
   */
   /*
   GTODO Clean up
  public static Language getLanguageForShortCode(String langCode) {
    Language language = getLanguageForShortCodeOrNull(langCode);
    if (language == null) {xxx
      List<String> codes = new ArrayList<>();
      for (Language realLanguage : getLanguageProvider().getAll()) {
        codes.add(realLanguage.getShortCodeWithCountryAndVariant());
      }
      Collections.sort(codes);
      throw new IllegalArgumentException("'" + langCode + "' is not a language code known to LanguageTool." +
              " Supported language codes are: " + String.join(", ", codes) + ". The list of languages is read from " + PROPERTIES_PATH +
              " in the Java classpath. See http://wiki.languagetool.org/java-api for details.");
    }
    return language;
  }
*/
  /**
   * Return whether a language with the given language code is supported. Which languages
   * are supported depends on the classpath when the {@code Language} object is initialized.
   * @param langCode e.g. {@code en} or {@code en-US}
   * @return true if the language is supported
   * @throws IllegalArgumentException in some cases of an invalid language code format
   */
  public static boolean isLanguageSupported(String langCode) {
    return getLanguage(langCode) != null;
  }

  /**
   * Return whether a language with the given locale is supported.
   *
   * @param locale The locale.
   * @return true if the language is supported.
   */
  public static boolean isLanguageSupported(Locale locale) {
      return getLanguage(locale) != null;
  }

  /**
   * Gets a new instance of a Language subclass for the specified locale.
   *
   * @param locale The locale.
   * @return The new language instance or null if the language isn't supported.
   */
  public static Language getNewLanguageInstance(Locale locale) throws Exception {
      Language l = getLanguage(locale);
      if (l == null) {
          return null;
      }
      return l.getClass().newInstance();
  }

  /**
   * Get the best match for a locale, using American English as the final fallback if nothing
   * else fits. The returned language will be a country variant language (e.g. British English, not just English)
   * if available.
   */
   /*
   GTODO Clean up
  public static Language getLanguageForLocale(Locale locale) {
    Language language = getLanguageForLanguageNameAndCountry(locale);
    if (language != null) {
      return language;
    } else {
      Language firstFallbackLanguage = getLanguageForLanguageNameOnly(locale);
      if (firstFallbackLanguage != null) {
        return firstFallbackLanguage;
      }
    }
    language = getLanguageProvider().get(l -> l.getShortCodeWithCountryAndVariant().equals("en-US"));
    return language;
  }
*/

  @Nullable
  public static Language getLanguage(String languageTag) {
        Objects.requireNonNull(languageTag);
        return getLanguage(Locale.forLanguageTag(languageTag));
  }

  @Nullable
  public static Language getLanguage(Locale locale) {
      Objects.requireNonNull(locale);
      Language lang = getLanguageProvider().get(l -> l.getLocale().equals(locale));
      return lang;
  }

  @Nullable
  public static Language getBestMatchLanguage(Locale locale) {
    Objects.requireNonNull(locale);
    // Check for an exact match...
    Language lang = getLanguageProvider().get(l -> l.getLocale().equals(locale));

    if (lang == null) {
        if (!"".equals(locale.getCountry())) {
            // There is a country.  Search for a match with language/country only.
            lang = getLanguageProvider().get(l -> new Locale(l.getLocale().getLanguage(), l.getLocale().getCountry()).equals (new Locale(locale.getLanguage(), locale.getCountry())));
        }
        if (lang == null) {
            // Just do a language lookup.
            lang = getLanguageProvider().get(l -> l.getLocale().getLanguage().equals(locale.getLanguage()));
        }
    }

    if (lang == null) {
        // Can't find any match.
        return null;
    }

    Language defl = lang.getDefaultLanguageVariant();
    // Is our best match a base level language, like "en", if so return the default variant.
    if (!lang.isVariant() && defl != null && !defl.getLocale().equals(lang.getLocale())) {
        return defl;
    }
    return lang;
  }

  @Nullable
  public static Language getBestMatchLanguage(String languageTag) {
      Objects.requireNonNull(languageTag);
      return getBestMatchLanguage(Locale.forLanguageTag(languageTag));
  }
/*
GTODO Clean up
  @Nullable
  private static Language getLanguageForShortCodeOrNull(String langCode) {
    StringTools.assureSet(langCode, "langCode");
    Language result = null;
    if (langCode.contains("-x-")) {
      // e.g. "de-DE-x-simple-language"
      result = getLanguageProvider().get(l -> l.getShortCode().equalsIgnoreCase(langCode));
    } else if (langCode.contains("-")) {
      String[] parts = langCode.split("-");
      if (parts.length == 2) { // e.g. en-US
          result = getLanguageProvider().get(l -> (parts[0].equalsIgnoreCase(l.getShortCode())
                                                    && l.getCountries().length == 1
                                                    && parts[1].equalsIgnoreCase(l.getCountries()[0])));

      } else if (parts.length == 3) { // e.g. ca-ES-valencia
          result = getLanguageProvider().get(l -> (parts[0].equalsIgnoreCase(l.getShortCode())
                                                    && l.getCountries().length == 1
                                                    && parts[1].equalsIgnoreCase(l.getCountries()[0])
                                                    && parts[2].equalsIgnoreCase(l.getVariant())));

      } else {
        throw new IllegalArgumentException("'" + langCode + "' isn't a valid language code");
      }

    } else {
      result = getLanguageProvider().get(l -> langCode.equalsIgnoreCase(l.getShortCode()));
      // TODO: It should return the DefaultLanguageVariant,
      // * not the first language found
    }
    return result;
  }
*/
/*
GTODO Clean up
  @Nullable
  private static Language getLanguageForLanguageNameAndCountry(Locale locale) {
    Language lang = getLanguageProvider().get(l -> l.getShortCode().equals(locale.getLanguage()));
    if (lang == null) {
        return null;
    }
    List<String> countryVariants = Arrays.asList(lang.getCountries());
    if (countryVariants.contains(locale.getCountry())) {
      return lang;
    }
    return null;
  }

  @Nullable
  private static Language getLanguageForLanguageNameOnly(Locale locale) {
    // use default variant if available:
    Language lang = getLanguageProvider().get(l -> l.getShortCode().equals(locale.getLanguage()) && l.hasVariant());
    if (lang != null) {
        Language defaultVariant = lang.getDefaultLanguageVariant();
        if (defaultVariant != null) {
          return defaultVariant;
        }
    }
    return lang;
  }
*/
}
