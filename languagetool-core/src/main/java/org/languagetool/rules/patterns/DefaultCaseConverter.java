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

import java.util.Objects;
import java.util.Locale;

import org.languagetool.Language;
import org.languagetool.tools.StringTools;

/**
 * A default implementation of the case converter.
 */
public class DefaultCaseConverter implements CaseConverter {
    private Locale locale;
    private static DefaultCaseConverter defConverter = new DefaultCaseConverter(new Locale("en"));

    public static DefaultCaseConverter getDefault() {
        return defConverter;
    }

    public DefaultCaseConverter(Language lang) {
        this(lang.getLocale());
    }

    public DefaultCaseConverter(Locale locale) {
        Objects.requireNonNull(locale, "Locale cannot be null.");
        this.locale = locale;
    }

    public boolean isMixedCase(String str) {
        return StringTools.isEmpty(str) ? false : StringTools.isMixedCase(str);
    }

    public boolean startsWithUpperCase(String str) {
        return StringTools.isEmpty(str) ? false : StringTools.startsWithUppercase(str);
    }

    public boolean isAllUpperCase(String str) {
        return StringTools.isEmpty(str) ? false : StringTools.isAllUppercase(str);
    }

    public String toUpperCase(String str) {
        return StringTools.isEmpty(str) ? str : str.toUpperCase(locale);
    }

    public String toLowerCase(String str) {
        return StringTools.isEmpty(str) ? str : str.toLowerCase(locale);
    }

    public String uppercaseFirstChar(String str) {
        return StringTools.isEmpty(str) ? str : StringTools.uppercaseFirstChar(str, locale);
    }

    public String lowercaseFirstChar(String str) {
        return StringTools.isEmpty(str) ? str : StringTools.lowercaseFirstChar(str);
    }

    /**
     * Convert the string according to the conversion provided and sample (optional).
     *
     * @param s The string to convert.
     * @param conversion The conversion to perform.
     * @param sample A sample to use for indicating the conversion.
     * @return The case converted string.
     */
    public String convert(String s, Match.CaseConversion conversion, String sample) {
        if (StringTools.isEmpty(s)) {
          return s;
        }
        String token = s;
        switch (conversion) {
          case NONE:
            break;
          case PRESERVE:
            if (startsWithUpperCase(sample)) {
              if (isAllUpperCase(sample)) {
                token = toUpperCase(token);
              } else {
                token = uppercaseFirstChar(token);
              }
            }
            break;
          case STARTLOWER:
            token = token.substring(0, 1).toLowerCase() + token.substring(1);
            break;
          case STARTUPPER:
            token = uppercaseFirstChar(token);
            break;
          case ALLUPPER:
            token = toUpperCase(token);
            break;
          case ALLLOWER:
            token = token.toLowerCase();
            break;
          default:
            break;
        }
        return token;

    }
}
