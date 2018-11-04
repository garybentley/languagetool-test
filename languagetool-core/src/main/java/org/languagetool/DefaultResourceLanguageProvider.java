package org.languagetool;

import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.MultiKeyProperties;
import org.languagetool.tools.StringTools;
import org.languagetool.databroker.DefaultResourceDataBroker;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;

/**
 * A language provider that loads from the classpath.  It uses the file: META-INF/org/languagetool/language-module.properties to determine
 * what languages are available.  Multiple language-module.properties files can be present.  Each file found is treated as a java properties file with key/value pairs.
 * Languages are identified by key: languageClasses.  The value is assumed to be a comma separated list of {@link org.languagetool.Language} classes that can be
 * class loaded and thus are on the classpath.  Duplication of language class values is allowed.
 * An example file:
 * <pre>{@code
 * languageClasses=org.languagetool.language.Italian
 * languageClasses=org.languagetool.language.Polish
 * languageClasses=org.languagetool.language.English,org.languagetool.language.AmericanEnglish
 * }</pre>
 */
public class DefaultResourceLanguageProvider implements LanguageProvider
{

    public static final String PROPERTIES_PATH = "META-INF/org/languagetool/language-module.properties";
    public static final String PROPERTIES_KEY = "languageClasses";

    private List<Language> languages = null;
    private Set<String> languageClassNames = new HashSet<>();

    /**
     * Create a new provider that will try and load all the languages immediately.
     */
    public DefaultResourceLanguageProvider () {
        this (false);
    }

    /**
     * Create a new provider that uses lazy loading, i.e. will only load a language as it is requested.
     *
     * @param lazyLoad Only load languages as they are requested.
     */
    public DefaultResourceLanguageProvider (boolean lazyLoad) {
        if (!lazyLoad) {
            this.loadLanguages(this.getClass().getClassLoader());
        }
    }

    /**
     * Get all the languages.
     *
     * @return The languages.
     */
    public List<Language> getAll() {
        return this.getAll(null);
    }

    /**
     * Get all the languages that match the filter.
     *
     * @param filter The filter to apply.
     * @return The matched languages as an unmodifiableList.
     */
    public List<Language> getAll(LanguageFilter filter) {
        List<Language> langs = new ArrayList<>();
        for (Language l : this.getAllLanguages()) {
            if (filter == null || filter.accept(l)) {
                langs.add(l);
            }
        }
        return Collections.unmodifiableList(langs);
    }

    /**
     * Get a single language that matches the filter.  If multiple languages match then null is returned.
     *
     * @param filter The filter to apply.
     * @return The language that matches or null if no languages match or if multiple languages match.
     */
    public Language get(LanguageFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Expected a filter to be provided.");
        }

        for (Language l : this.getAllLanguages()) {
            if (filter.accept (l)) {
                return l;
            }
        }
        return null;
    }

    public void addLanguage (Language l) {
        this.languages.add (l);
    }

    /**
     * Load languages from a url.  This uses a URLClassLoader for loading the classes/data for the language and so uses the same rules for class loading/resolution
     * as {@java.net.URLClassLoader}.
     *
     * GTODO Change to use a Path, Allow for multiple paths (jars) and allow for a directory that may contain jars, i.e. for each file in the path dir convert to a URL.
     *
     * @param url The url to load languages from.
     */
    public void loadLanguages (URL url) {
        URLClassLoader cl = new URLClassLoader (new URL[] {url}, this.getClass().getClassLoader());
        this.loadLanguages(cl);
    }

    /**
     * Load languages from a specified class loader.
     *
     * @param cl The class load to load languages from.
     */
    public void loadLanguages (ClassLoader cl) {
        Set<Language> langs = new HashSet<>();
        try {
          Enumeration<URL> propertyFiles = cl.getResources(PROPERTIES_PATH);
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
                  if (this.languageClassNames.contains(className)) {
                    // avoid duplicates - this way we are robust against problems with the maven assembly
                    // plugin which aggregates files more than once (in case the deployment descriptor
                    // contains both <format>zip</format> and <format>dir</format>):
                    continue;
                  }
                  if (this.languages == null){
                      this.languages = new ArrayList<>();
                  }
                  this.languages.add(createLanguageObjects(url, className, cl));
                  this.languageClassNames.add(className);
                }
              }
            }
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
    }

    private List<Language> getAllLanguages() {
        if (this.languages != null) {
            return this.languages;
        }
        this.loadLanguages(this.getClass().getClassLoader());
        return Collections.unmodifiableList(this.languages);
    }

    private Language createLanguageObjects(URL url, String className, ClassLoader loader) {
      try {
        Class<?> aClass = Class.forName(className, true, loader);
        Constructor<?> constructor = aClass.getConstructor();
        Language l = (Language) constructor.newInstance();
        return l;
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Class '" + className + "' specified in " + url + " could not be found in classpath", e);
      } catch (Exception e) {
        throw new RuntimeException("Object for class '" + className + "' specified in " + url + " could not created", e);
      }
    }

}
