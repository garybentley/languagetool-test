package org.languagetool;

import java.util.function.Consumer;
import java.util.List;

public interface LanguageProvider {

    /**
     * Get the supported languages.
     *
     * @return A list of the supported languages.
     */
    public List<Language> getAll();

    /**
     * Get the languages that match the specified filter.
     *
     * @param The filter to apply.
     * @return A list of the matched languages.
     */
    public List<Language> getAll(LanguageFilter filter);

    /**
     * Return the language that matches the specified filter.
     * Return null if no languages match or more than 1 language matches.
     *
     * @param The filter to apply.
     * @return The language that matched.
     */
    public Language get(LanguageFilter filter);

}
