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
package org.languagetool.rules;

import java.util.*;

//GTODO Add rule configuration to ALL relevant rules.
//GTODO Maybe also add a per instance Rule Config.

public class RuleConfiguration {

    private String ruleId;
    private Description description;
    private Set<Match> matches;
    private Class ruleClass;

    public RuleConfiguration(Class ruleClass, String ruleId, String descriptionId, Match... matches) {
        this(ruleClass, ruleId, newDescription(descriptionId), matches);
    }

    public RuleConfiguration(Class ruleClass, String ruleId, Description description, Match... matches) {
        Objects.requireNonNull(ruleClass);
        Objects.requireNonNull(ruleId);
        Objects.requireNonNull(description);
        if (!Rule.class.isAssignableFrom(ruleClass)) {
            throw new RuntimeException(String.format("Class: %1$s does not extend %2$s", ruleClass.getName(), Rule.class.getName()));
        }
        this.ruleClass = ruleClass;
        this.ruleId = ruleId;
        this.description = description;
        if (matches != null && matches.length > 0) {
            this.matches = new LinkedHashSet<Match>(Arrays.asList(matches));
        }
    }

    public String getRuleId() {
        return ruleId;
    }

    public static Match newMatch(String id) {
        return new Match(id);
    }

    public static Match newMatch(String id, Class... expectedValueTypes) {
        return new Match(id, expectedValueTypes);
    }

    public static Description newDescription(String id) {
        return new Description(id);
    }

    public static Description newDescription(String id, Class... expectedValueTypes) {
        return new Description(id, expectedValueTypes);
    }

    public static class Description extends Match {
        public Description(String id) {
            super(id);
        }

        public Description(String id, Class... expectedValueTypes) {
            super(id, expectedValueTypes);
        }
    }

    public static class Match {
        private String id;
        private List<Class> expectedValueTypes;

        public Match(String id) {
            this.id = id;
        }

        public Match(String id, Class... expectedValueTypes) {
            this(id);
            if (expectedValueTypes != null && expectedValueTypes.length > 0) {
                this.expectedValueTypes = Arrays.asList(expectedValueTypes);
            }
        }

    }

}
