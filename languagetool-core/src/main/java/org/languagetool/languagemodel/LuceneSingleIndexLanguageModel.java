/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.languagemodel;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.Experimental;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Information about ngram occurrences, taken from Lucene indexes (one index per ngram level).
 * This is not a real language model as it only returns information
 * about occurrence counts but has no probability calculation, especially
 * not for the case with 0 occurrences.
 * @since 3.2
 */
public class LuceneSingleIndexLanguageModel extends BaseLanguageModel {

  //GTODO private static final Map<File,LuceneSearcher> dirToSearcherMap = new HashMap<>();  // static to save memory for language variants

  //GTODO private final List<File> indexes = new ArrayList<>();
  private Map<Integer,IndexSearcher> luceneSearcherMap = new HashMap<>();
  //GTODO private final File topIndexDir;
  private final long maxNgram;

  /**
   * Throw RuntimeException is the given directory does not seem to be a valid ngram top directory
   * with sub directories {@code 1grams} etc.
   * @since 3.0
   */
   /*
    GTODO Clean up
  public static void validateDirectory(File topIndexDir) {
    if (!topIndexDir.exists() || !topIndexDir.isDirectory()) {
      throw new RuntimeException("Not found or is not a directory:\n" +
              topIndexDir + "\n" +
              "As ngram directory, please select the directory that has a subdirectory like 'en'\n" +
              "(or whatever language code you're using).");
    }
    List<String> dirs = new ArrayList<>();
    for (String name : topIndexDir.list()) {
      if (name.matches("[123]grams")) {
        dirs.add(name);
      }
    }
    if (dirs.size() == 0) {
      throw new RuntimeException("Directory must contain at least '1grams', '2grams', and '3grams': " + topIndexDir.getAbsolutePath());
    }
    if (dirs.size() < 3) {
      throw new RuntimeException("Expected at least '1grams', '2grams', and '3grams' sub directories but only got " + dirs + " in " + topIndexDir.getAbsolutePath());
    }
  }
*/
  /**
   * Only used internally.
   * @since 3.2
   */
/*
 GTODO Clean up
  @Experimental
  public static void clearCaches() {
    dirToSearcherMap.clear();
  }
*/
  public LuceneSingleIndexLanguageModel(Map<Integer, IndexSearcher> searchers) {
      if (searchers.size() == 0) {
          throw new IllegalArgumentException("Expected at least one searcher.");
      }
      // TODO: Check to make sure all indexes are contiguous?
      luceneSearcherMap = searchers;
      maxNgram = Collections.<Integer>max(luceneSearcherMap.keySet());

  }

  /**
   * @param topIndexDir a directory which contains at least another sub directory called {@code 3grams},
   *                    which is a Lucene index with ngram occurrences as created by
   *                    {@code org.languagetool.dev.FrequencyIndexCreator}.
   */
   /*
    GTODO Clean up
  public LuceneSingleIndexLanguageModel(File topIndexDir)  {
    doValidateDirectory(topIndexDir);
    this.topIndexDir = topIndexDir;
    addIndex(topIndexDir, 1);
    addIndex(topIndexDir, 2);
    addIndex(topIndexDir, 3);
    addIndex(topIndexDir, 4);
    if (luceneSearcherMap.size() == 0) {
      throw new RuntimeException("No directories '1grams' ... '3grams' found in " + topIndexDir);
    }
    maxNgram = Collections.<Integer>max(luceneSearcherMap.keySet());
  }
*/
  @Experimental
  public LuceneSingleIndexLanguageModel(int maxNgram) {
    this.maxNgram = maxNgram;
    //GTODO this.topIndexDir = null;
  }
/*
GTODO Clean up
  protected void doValidateDirectory(File topIndexDir) {
    validateDirectory(topIndexDir);
  }
*/
/*
GTODO Clean up
  private void addIndex(File topIndexDir, int ngramSize) {
    File indexDir = new File(topIndexDir, ngramSize + "grams");
    if (indexDir.exists() && indexDir.isDirectory()) {
      if (luceneSearcherMap.containsKey(ngramSize)) {
        throw new RuntimeException("Searcher for ngram size " + ngramSize + " already exists");
      }
      luceneSearcherMap.put(ngramSize, getCachedLuceneSearcher(indexDir));
      indexes.add(indexDir);
    }
  }
*/
  @Override
  public long getCount(List<String> tokens) {
    if (tokens.size() > maxNgram) {
      throw new IllegalArgumentException("Requested " + tokens.size() + "gram but index has only up to " + maxNgram + "gram: " + tokens);
    }
    Objects.requireNonNull(tokens);
    IndexSearcher searcher = luceneSearcherMap.get(tokens.size());
    if (searcher == null) {
        throw new UnsupportedOperationException(String.format("No index available for ngram size: %1$s", tokens.size()));
    }

    Term term = new Term("ngram", String.join(" ", tokens));
    return getCount(term, searcher);
  }

  @Override
  public long getCount(String token1) {
    Objects.requireNonNull(token1);
    //TODO: move this into the document? It's not there currently...
    //if (token1.equals(LanguageModel.GOOGLE_SENTENCE_START)) {
    //  return 42_107_029_039L;  // see StartTokenCounter, run with 2grams (3grams: 124_541_229_392)
    //}
    return getCount(Arrays.asList(token1));
  }

  @Override
  public long getTotalTokenCount() {
      IndexSearcher searcher = luceneSearcherMap.get(1);
      if (searcher == null) {
          throw new UnsupportedOperationException("No index available for ngram size: 1");
      }

    try {
      RegexpQuery query = new RegexpQuery(new Term("totalTokenCount", ".*"));
      TopDocs docs = searcher.search(query, 1000);  // Integer.MAX_VALUE might cause OOE on wrong index
      if (docs.totalHits == 0) {
        throw new RuntimeException("Expected 'totalTokenCount' meta documents not found for ngram size: 1");
      } else if (docs.totalHits > 1000) {
        throw new RuntimeException("Did not expect more than 1000 'totalTokenCount' meta documents: " + docs.totalHits);
      } else {
        long result = 0;
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
          long tmp = Long.parseLong(searcher.getIndexReader().document(scoreDoc.doc).get("totalTokenCount"));
          if (tmp > result) {
            // due to the way FrequencyIndexCreator adds these totalTokenCount fields, we must not sum them,
            // but take the largest one:
            result = tmp;
          }
        }
        return result;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
/*
GTODO Clean up
  protected LuceneSearcher getLuceneSearcher(int ngramSize) {
    LuceneSearcher luceneSearcher = luceneSearcherMap.get(ngramSize);
    if (luceneSearcher == null) {
      throw new RuntimeException("No " + ngramSize + "grams directory found in " + topIndexDir);
    }
    return luceneSearcher;
  }
*/
/*
GTODO Clean up
  private LuceneSearcher getCachedLuceneSearcher(File indexDir) {
    LuceneSearcher luceneSearcher = dirToSearcherMap.get(indexDir);
    if (luceneSearcher == null) {
      try {
        LuceneSearcher newSearcher = new LuceneSearcher(indexDir);
        dirToSearcherMap.put(indexDir, newSearcher);
        return newSearcher;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      return luceneSearcher;
    }
  }
*/
  private long getCount(Term term, IndexSearcher searcher) {
    long result = 0;
    try {
      // GTODO Make the max configurable
      TopDocs docs = searcher.search(new TermQuery(term), 2000);
      if (docs.totalHits > 2000) {
        throw new RuntimeException(String.format("More than 2000 matches for '%1$s not supported for performance reasons: %2$s", term, docs.totalHits));
      }
      for (ScoreDoc scoreDoc : docs.scoreDocs) {
        String countStr = searcher.getIndexReader().document(scoreDoc.doc).get("count");
        result += Long.parseLong(countStr);
      }
      //System.out.println(term + " -> " + result);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  // GTODO Investigate LanguageModel and why this is even a thing
  public void close () {

  }

/*
GTODO Clean up, the searcher resources are already auto closeable
  @Override
  public void close() {
    for (LuceneSearcher searcher : luceneSearcherMap.values()) {
      try {
        searcher.reader.close();
        searcher.directory.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
*/
/*
GTODO Clean up
  @Override
  public String toString() {
    return indexes.toString();
  }
*/
/*
GTODO Clean up
  protected static class LuceneSearcher {
    final FSDirectory directory;
    final IndexReader reader;
    final IndexSearcher searcher;
    private LuceneSearcher(File indexDir) throws IOException {
      Path path = indexDir.toPath();
      // symlinks are not supported here, see https://issues.apache.org/jira/browse/LUCENE-6700,
      // so we resolve the link ourselves:
      if (Files.isSymbolicLink(path)) {
        path = indexDir.getCanonicalFile().toPath();
      }
      this.directory = FSDirectory.open(path);
      this.reader = DirectoryReader.open(directory);
      this.searcher = new IndexSearcher(reader);
    }
    public IndexReader getReader() {
      return reader;
    }
  }
  */
}
