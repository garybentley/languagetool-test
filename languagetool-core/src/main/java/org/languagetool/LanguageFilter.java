package org.languagetool;

@FunctionalInterface
public interface LanguageFilter {
    public boolean accept(Language lang);
    
}
