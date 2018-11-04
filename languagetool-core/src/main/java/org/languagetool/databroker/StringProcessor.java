/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Gary Bentley
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
package org.languagetool.databroker;

import java.util.*;

/**
 * A class used to validate and process pieces of text.
 */
public interface StringProcessor<E> {
    /**
     * Get any errors for the string.
     *
     * As a default we return an empty set.
     *
     * @param str The string to check for errors.
     * @return A list of errors.
     */
    public default Set<String> getErrors(String str) {
        return new HashSet<>();
    }

    /**
     * Determine whether the string should be skipped, i.e. it's acceptable to the implementation.
     * Return {@code true} if the string shouldn't be used.
     *
     * As a default we return {@code false}.
     *
     * @param str The string to check.
     * @return {@code true} if the string should be skipped.
     */
    public default boolean shouldSkip(String str) {
        return false;
    }

    /**
     * Process the string and return a modified form, if needed.
     *
     * As a default we return the passed in string unaltered.
     *
     * @param str The string to process/modify.
     * @return The line processed.
     */
    public E getProcessed(String str) throws Exception;
}
