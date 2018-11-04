package org.languagetool.rules.patterns;

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

/**
 * An interface to define a case converter, i.e. how to change case for words.
 */
public interface CaseConverter {
    public boolean startsWithUpperCase(String str);

    public boolean isAllUpperCase(String str);

    public String toUpperCase(String str);

    public String toLowerCase(String str);

    public String uppercaseFirstChar(String str);

    public boolean isMixedCase(String str);

    /**
     * Convert the string according to the conversion provided and sample (optional).
     *
     * @param s The string to convert.
     * @param conversion The conversion to perform.
     * @param sample A sample to use for indicating the conversion.
     * @return The case converted string.
     */
    public String convert(String s, Match.CaseConversion conversion, String sample);
}
