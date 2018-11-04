/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.nio.file.*;

import org.languagetool.databroker.*;
import org.languagetool.tagging.*;
import org.languagetool.chunking.*;
import org.languagetool.tagging.disambiguation.*;
import org.languagetool.languagemodel.LuceneLanguageModel;

public class DefaultTestResourceDataBroker extends DefaultResourceDataBroker {

    public static String ADDED_1_WORD_TAGGER_FILE_NAME = "%1$s/added1.txt";
    public static String ADDED_2_WORD_TAGGER_FILE_NAME = "%1$s/added2.txt";
    public static String REMOVED_WORD_TAGGER_FILE_NAME = "%1$s/removed.txt";
    public static String MORFOLOGIK_TAGGER_DICT_FILE_NAME = "%1$s/tagging/test.dict";

    private Tagger tagger;
    private Chunker chunker;

    public DefaultTestResourceDataBroker(TestLanguage lang, ClassLoader loader) throws Exception {
        super(lang, loader);
        /*
        super(Paths.get(DEFAULT_RESOURCE_DIR), Paths.get(DEFAULT_RULES_DIR), lang, loader, new PathProvider()
        {
            @Override
            public Path resolve(Path path) {
                return path;
            }
        });
        */
    }

    @Override
    public LuceneLanguageModel getLanguageModel() throws Exception {
        return createLanguageModelFromResourcePath();
    }

    /**
     * Returns a chunker that assigns chunk {@code B-NP-singular} to the word {@code chunkbar}.
     */
    @Override
    public Chunker getChunker() {
        if (chunker == null) {
            chunker = new Chunker() {
              @Override
              public void addChunkTags(List<AnalyzedTokenReadings> tokenReadings) {
                for (AnalyzedTokenReadings tokenReading : tokenReadings) {
                  if ("chunkbar".equals(tokenReading.getToken())) {
                    tokenReading.setChunkTags(Collections.singletonList(new ChunkTag("B-NP-singular")));
                  }
                }
              }
          };
      }
      return chunker;
    }

    @Override
    public WordTagger getWordTagger() throws Exception {
            return getWordTagger(true, false);
    }

    public MorfologikTagger getMorfologikTagger() throws Exception {
        return new MorfologikTagger(getMorfologikBinaryDictionaryFromResourcePath(String.format(MORFOLOGIK_TAGGER_DICT_FILE_NAME, language.getLocale().getLanguage())));
    }

    public CombiningTagger getWordTagger(boolean includeRemovalTagger, boolean overwrite) throws Exception {
        ManualTagger tagger1 = new ManualTagger(getResourceDirPathStream(String.format(ADDED_1_WORD_TAGGER_FILE_NAME, language.getLocale().getLanguage())));
        ManualTagger tagger2 = new ManualTagger(getResourceDirPathStream(String.format(ADDED_2_WORD_TAGGER_FILE_NAME, language.getLocale().getLanguage())));
        ManualTagger removalTagger = null;
        if (includeRemovalTagger) {
          removalTagger = new ManualTagger(getResourceDirPathStream(String.format(REMOVED_WORD_TAGGER_FILE_NAME, language.getLocale().getLanguage())));
        }
        return new CombiningTagger(tagger1, tagger2, removalTagger, overwrite);

    }

}
