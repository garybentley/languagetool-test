package org.languagetool;

import java.net.URLClassLoader;
import java.util.Set;
import java.util.HashSet;

public class URLLanguageProvider extends DefaultResourceLanguageProvider
{

    public URLLanguageProvider() {
        super();
    }

    public URLLanguageProvider(boolean lazyLoad) {
        super(lazyLoad);
    }
/*
    public Set<Language> loadLanguages(URL url)
    {

        URLClassLoader cl = new URLClassLoader (new URL[] {url}, this.getClass().getClassLoader());

        List<Language> languages = new ArrayList<>();
        Set<String> languageClassNames = new HashSet<>();
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
          this.languages = Collections.unmodifiableList(languages);
          return this.languages;
        }

    }

    private Language createLanguageObjects(URL url, String className) {
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

}
