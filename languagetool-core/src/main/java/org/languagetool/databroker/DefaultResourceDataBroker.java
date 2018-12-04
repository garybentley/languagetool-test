/* LanguageTool, a natural language style checker
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.Scanner;
import java.util.Objects;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.PropertyResourceBundle;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.function.Function;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.base.Optional;

import org.jetbrains.annotations.NotNull;

import org.xml.sax.SAXException;

import net.loomchild.segment.srx.SrxDocument;
import net.loomchild.segment.srx.io.Srx2SaxParser;
import net.loomchild.segment.srx.SrxParser;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;

import morfologik.stemming.Dictionary;

import biz.k11i.xgboost.Predictor;

import org.languagetool.tools.Tools;
import org.languagetool.rules.Rule;
import org.languagetool.rules.ContextWords;
import org.languagetool.rules.ConfusionSet;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.ConfusionString;
import org.languagetool.rules.CompoundRuleData;
import org.languagetool.rules.bitext.BitextRule;
import org.apache.commons.lang3.StringUtils;
import org.languagetool.*;
import org.languagetool.tools.StringTools;
import org.languagetool.rules.spelling.hunspell.*;
import org.languagetool.rules.patterns.*;
import org.languagetool.rules.patterns.bitext.BitextPatternRule;
import org.languagetool.rules.patterns.bitext.BitextPatternRuleHandler;
import org.languagetool.rules.patterns.CaseConverter;
import org.languagetool.rules.patterns.DefaultCaseConverter;
import org.languagetool.rules.neuralnetwork.Word2VecModel;
import org.languagetool.rules.neuralnetwork.NeuralNetworkRule;
import org.languagetool.rules.neuralnetwork.Classifier;
import org.languagetool.rules.neuralnetwork.Embedding;
import org.languagetool.rules.neuralnetwork.Matrix;

import org.languagetool.tagging.*;
import org.languagetool.tagging.disambiguation.*;
import org.languagetool.tagging.disambiguation.rules.*;
import org.languagetool.tokenizers.*;
import org.languagetool.chunking.Chunker;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.languagemodel.*;
import org.languagetool.rules.neuralnetwork.TwoLayerClassifier;
import org.languagetool.rules.neuralnetwork.SingleLayerClassifier;


/**
 * Responsible for getting any items from the grammar checker's resource
 * directories. This default data broker assumes that they are accessible
 * directly via class-path and the directory names are like specified in:
 *
 * <ul>
 * <li>{@link ResourceDataBroker#RESOURCE_DIR}</li>
 * <li>{@link ResourceDataBroker#RULES_DIR}</li>
 * </ul>
 * <p>
 *
 * If you'd like to determine another resource directory location this default
 * data broker provides proper methods.
 * Assuming your {@code /rules} and {@code /resource} directories are accessible
 * via class-path with following path information:
 *
 * <ul>
 * <li>{@code /res/grammarchecker/rulesdirname}</li>
 * <li>{@code /res/grammarchecker/resourcedirname}</li>
 * </ul>
 *
 * In this case you have to use the constructor with the following arguments:
 *
 * <ul>
 * <li>{@code /res/grammarchecker/rulesdirname}</li>
 * <li>{@code /res/grammarchecker/resourcedirname}</li>
 * </ul>
 * <p>
 *
 * Make sure that you never obtain any grammar checker resources by calling
 * {@code Object.class.getResource(String)} or {@code
 * Object.class.getResourceAsStream(String)} directly. If you would like to
 * obtain something from these directories do always use
 * {@link JLanguageTool#getDataBroker()} which provides proper methods for
 * reading the directories above.
 * <p>
 *
 * For example, if you want to get the {@link URL} of {@code
 * /rules/de/grammar.xml} just invoke
 * {@link ResourceDataBroker#getFromRulesDirAsUrl(String)} and pass {@code
 * /de/grammar.xml} as a string. Note: The {@code /rules} directory's name isn't
 * passed because its name might have changed. The same usage does apply for the
 * {@code /resource} directory.
 *
 * @see ResourceDataBroker
 * @author PAX
 * @since 1.0.1
 */
public class DefaultResourceDataBroker implements ResourceDataBroker {

    public static final String MESSAGE_BUNDLE_FILE_NAME = "/org/languagetool/MessagesBundle%1$s.properties";
    public static final String DEFAULT_MESSAGE_BUNDLE_LANGUAGE_CODE = "en";

    /**
     * The default directory name of the {@code /resource} directory.
     */
    public static String DEFAULT_RESOURCE_DIR = "/org/languagetool/resource";

    /**
     * The default directory name of the {@code /rules} directory.
     */
    public static String DEFAULT_RULES_DIR = "/org/languagetool/rules";


    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public static String HUNSPELL_DIC_FILE_NAME_EXT = ".dic";
    public static String HUNSPELL_AFF_FILE_NAME_EXT = ".aff";
    public static String HUNSPELL_BASE_DIR = "%1$s/hunspell/";

    public static String NEURAL_NETWORK_CLASSIFIER_W_FC1_FILE_NAME = "%1$s/W_fc1.txt";
    public static String NEURAL_NETWORK_CLASSIFIER_B_FC1_FILE_NAME = "%1$s/b_fc1.txt";
    public static String NEURAL_NETWORK_CLASSIFIER_W_FC2_FILE_NAME = "%1$s/W_fc2.txt";
    public static String NEURAL_NETWORK_CLASSIFIER_B_FC2_FILE_NAME = "%1$s/b_fc2.txt";
    public static String NEURAL_NETWORK_CONFUSION_SETS_FILE_NAME = "%1$s/neuralnetwork/confusion_sets.txt";

    /*
    GTODO Test, an example potential JSON format for mapping a type of data to its location.
    {
        extends : 'base',
        locale : 'en_GB',
        language : 'en',
        baseDir : '/org/languagetool',
        resourceDir : '${baseDir}/resource/${language}',
        rulesDir : '${baseDir}/rules/${language}',
        disambiguation : '${resourceDir}/disambiguation.xml',
        morfologikDictionaries :
        {
            info : '${resourceDir}/hunspell/${locale}.info',
            binary : [ '${resourceDir}/hunspell/${locale}.dict' ],
            text : [ '${resourceDir}/hunspell/spelling.txt', '${resourceDir}/hunspell/spelling_${locale}.txt' ]
        },
        grammarRules : [ '${rulesDir}/grammar.xml', '${resourceDir}/${locale}/grammar.xml' ],
        falseFriendsRules : '${baseDir}/rules/false-friends.xml',
        multiWords : '',
        ignoreWords : '${resourceDir}/hunspell/ignore.txt',
        wordTagger :
        {
            dictionary : '${resourceDir}/english.dict',
            addedWords : '${resourceDir}/added.txt',
            removedWords : '${resourceDir}/removed.txt'
        }xxx
    }
*/

    /**
     * The xml disambiguation file, we expect to find it in a language subdir, i.e.
     *   /resources/en/disambiguation.xml
     */
    public static String DISAMBIGUATION_FILE = "%1$s/disambiguation.xml";

    public static String GENERAL_PATTERN_RULES_FILE_NAME = "%1$s/grammar.xml";
    public static String LOCALE_VARIANT_PATTERN_RULES_FILE_NAME = "%1$s/%1$s-%2$s/grammar.xml";
    public static String SIMPLE_SEGMENT_SRX_FILE_NAME = "org/languagetool/tokenizers/segment-simple.srx";
    public static String DEFAULT_SEGMENT_SRX_FILE_NAME = "segment.srx";
    public static String LANGUAGE_PROFILE_FILE_NAME = "%1$s/%1$s.profile";
    public static String BITEXT_RULES_FILE_NAME = "%1$s/bitext.xml";
    public static String CONFUSION_SETS_FILE_NAME = "%1$s/confusion_sets.txt";
    public static String FALSE_FRIEND_RULES_FILE_NAME = "false-friends.xml";
    public static String MULTI_WORDS_FILE_NAME = "%1$s/multiwords.txt";
    public static String NGRAM_INDEX_DIR_NAME = "%1$s/ngram-index";

    public static String WORD_TAGGER_ADDED_WORDS_FILE_NAME = "%1$s/added.txt";
    public static String WORD_TAGGER_REMOVED_WORDS_FILE_NAME = "%1$s/removed.txt";

    // GTODO Move the speller_rule rules to the relevant subdir in the relevant langauge-modules.
    public static final String PREDICTOR_RULE_SPC_NGRAM_BASED_MODEL_FILENAME = "spc_ngram.model";
    public static final String PREDICTOR_RULE_NO_NGRAM_BASED_MODEL_FILENAME = "spc_naive.model";
    public static final String PREDICTOR_RULE_MODEL_BASE_PATH = "speller_rule/models/";

    public static String WORD_2_VEC_MODEL_DICT_FILE_NAME = "%1$s/dictionary.txt";
    public static String WORD_2_VEC_MATRIX_FILE_NAME = "%1$s/final_embeddings.txt";

    private static final LoadingCache<PathCacheKey, Dictionary> binaryMorfologikDictCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(new CacheLoader<PathCacheKey, Dictionary>() {
          @Override
          public Dictionary load(@NotNull PathCacheKey key) throws Exception {
            return createMorfologikBinaryDictionary(key.path);
          }
        });

    /**
     * The cache for test file based Morofologik dictionaries.  We wrap the Dictionary instance in an Optional because the text files, while present,
     * may be (effectively) empty.
     */
    private static final LoadingCache<MorfologikTextDictCacheKey, Optional<Dictionary>> textMorfologikDictCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(new CacheLoader<MorfologikTextDictCacheKey, Optional<Dictionary>>() {
          @Override
          public Optional<Dictionary> load(@NotNull MorfologikTextDictCacheKey key) throws Exception {
              // We "know" here that the first path is to the info file, the rest are to the text files.
              return Optional.fromNullable(createMorfologikTextDictionary(key.paths, key.infoPath, key.charset));
          }
        });

    // The associated processor will convert each line of the resource file to a type of object.
    // It is up to clients to determine whether the type is correct and cast accordingly.  There is no
    // way to have a generic type here and prevent the cast since the objects in the list are variable and
    // may be arrays or objects or lists/maps of object objects.
    private static final LoadingCache<WordsCacheKey, List> wordListCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(new CacheLoader<WordsCacheKey, List>() {
          @Override
          public List load(@NotNull WordsCacheKey key) throws Exception {
            return loadWords(key.path, key.charset, key.processor);
          }
        });

    private static final LoadingCache<PatternRuleCacheKey, List<AbstractPatternRule>> patternsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(new CacheLoader<PatternRuleCacheKey, List<AbstractPatternRule>>() {
          @Override
          public List<AbstractPatternRule> load(@NotNull PatternRuleCacheKey key) throws Exception {
            return createPatternRules(key.path, key.ruleFilterCreator, false);
          }
        });

    private static final LoadingCache<PathCacheKey, Map<String, List<ConfusionSet>>> confusionSetsCache = CacheBuilder.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .build(new CacheLoader<PathCacheKey, Map<String, List<ConfusionSet>>>() {
          @Override
          public Map<String, List<ConfusionSet>> load(@NotNull PathCacheKey key) throws IOException {
            return createConfusionSet(key.path, key.charset);
          }
        });

    private static final StringProcessor<String[]> multiWordChunkerStringProcessor = new StringProcessor<String[]>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return (line.isEmpty() || line.charAt(0) == '#');
        }

        @Override
        public Set<String> getErrors(String line) {
            Set<String> errs = null;
            String[] tokenAndTag = line.split("\t");
            if (tokenAndTag.length != 2) {
              errs = new LinkedHashSet<>();
              errs.add(String.format("Invalid format expected two tab-separated parts, found %1$s", tokenAndTag.length));
            }
            return errs;
        }

        @Override
        public String[] getProcessed(String line) {
            return line.split("\t");
        }
    };

    protected static final StringProcessor<byte[]> morfologikSpellerTextWordListStringProcessor = new StringProcessor<byte[]>() {
        @Override
        public boolean shouldSkip(String line) {
            return line.startsWith("#");
        }

        @Override
        public byte[] getProcessed(String line) {
              return line.replaceFirst("#.*", "").trim().getBytes(DEFAULT_CHARSET);
        }
    };

    protected static final StringProcessor<String> spellingWordProcessor = new StringProcessor<String>() {
        @Override
        public boolean shouldSkip(String line) {
            // GTODO: Can a comment have leading spaces before the #?
            return line.startsWith("#");
        }

        @Override
        public Set<String> getErrors(String line) {
            Set<String> errors = new LinkedHashSet<>();
            if (line.trim().length() < line.length()) {
                errors.add("No leading or trailing space expected.");
            }
            return errors;
        }

        @Override
        public String getProcessed(String line) {
            // GTODO: Pretty sure this does nothing since the skip would check it?
            return line.replaceFirst("#.*", "");
        }
    };

    /**
     * Converts a line in the form: <word>;<word> into a bi-directional map.
     * i.e. the line:
     *   word1;word2
     * would return a map popuplated with the:
     *   String(word1) -> String(word2)
     *   String(word2) -> String(word1)
     */
    private static final StringProcessor<Map<String, String>> coherencyWordStringProcessor = new StringProcessor<Map<String, String>>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.startsWith("#");
        }
        @Override
        public Set<String> getErrors(String line) {
            Set<String> errors = null;
            String[] parts = line.split(";");
            if (parts.length != 2) {
                errors = new LinkedHashSet<>();
                errors.add("Expected line to have two parts separated by a semi-colon (;)");
            }
            return errors;
        }
        @Override
        // GTODO Maybe return a tuple?
        public Map<String, String> getProcessed(String line) {
            line = line.trim();
            String[] parts = line.split(";");
            Map<String, String> map = new HashMap<>();
            map.put(parts[0], parts[1]);
            map.put(parts[1], parts[0]);
            return map;
        }
    };

    private static final StringProcessor<String> synthesizerTagWordProcessor = new StringProcessor<String>() {
        @Override
        public boolean shouldSkip(String line) {
            line = line.trim();
            return line.isEmpty() || line.startsWith("#");
        }

        @Override
        public Set<String> getErrors(String line) {
            return null;
        }

        @Override
        public String getProcessed(String line) {
            return line;
        }
    };

    protected static final StringProcessor<String> defaultStringProcessor = new StringProcessor<String>() {
         @Override
         public boolean shouldSkip(String line) {
             return false;
         }
         @Override
         public String getProcessed(String line) {
             return line;
         }
         @Override
         public Set<String> getErrors(String line) {
             return null;
         }
    };

    private Word2VecModel word2VecModel;
    private ResourceBundle messageBundle;
    private List<AbstractPatternRule> patternRules;
    private Disambiguator disambiguator;

    private RuleFilterCreator ruleFilterCreator;

    private SRXSentenceTokenizer defaultSentenceTokenizer;

    private WordTokenizer wordTokenizer;

    private Tagger tagger;

  /**
   * The case converter to use.
   */
  private CaseConverter caseConverter;

  /**
   * The directory's name of the grammar checker's resource directory. The
   * default value equals {@link ResourceDataBroker#RESOURCE_DIR}.
   */
  //private final String resourceDir;

  /**
   * The directory's name of the grammar checker's rules directory. The
   * default value equals {@link ResourceDataBroker#RULES_DIR}.
   */
  //private final String rulesDir;

  //protected final Path resourceDirPath;
  //protected final Path rulesDirPath;

  protected final PathProvider pathProvider;

  /**
   * The class loader to use to load resources.
   */
  protected final ClassLoader classLoader;

  protected final Language language;

  // GTODO Maybe just have a stream provider with a path?

  /**
   * Creates a new default broker using resources from the classpath, as accessed via the passed in class loader.  The default resource and rules paths {@link DEFAULT_RESOURCE_DIR}
   * and {@link DEFAULT_RULES_DIR} are used.
   *
   * @param lang The language to get the broker for.
   * @param classLoader The class loader to use to lookup resources.
   * @return The broker.
   */
  public static DefaultResourceDataBroker newClassPathInstance(Language lang, ClassLoader classLoader) throws Exception {
      return newClassPathInstance(DEFAULT_RESOURCE_DIR, DEFAULT_RULES_DIR, lang, classLoader);
  }

  /**
   * Creates a new default broker using resources from the classpath, as accessed via the passed in class loader.
   *
   * @param resourceDir THe path to use for the resource dir.
   * @param rulesDir The path to use for the rules dir.
   * @param lang The language to get the broker for.
   * @param classLoader The class loader to use to lookup resources.
   * @return The broker.
   */
  public static DefaultResourceDataBroker newClassPathInstance(String resourceDir, String rulesDir, Language lang, ClassLoader classLoader) throws Exception {
      return new DefaultResourceDataBroker(resourceDir, rulesDir, lang, classLoader);
  }

  /**
   * A convenience constructor for sub-classes that want to use the class path lookup and specify the resource and rules dir.  If the resource or rules dirs
   * are null then the defaults are used instead.
   */
  protected DefaultResourceDataBroker(String resourceDir, String rulesDir, Language lang, ClassLoader classLoader) throws Exception {
      this(lang, classLoader, new PathProvider()
      {
          private Path getPath(String path) throws Exception {
              URL url = classLoader.getResource(path);
              if (url == null) {
                  if (path.startsWith("/")) {
                      path = path.substring(1);
                      return getPath(path);
                  } else {
                      return null;
                  }
              }
              // The url won't auto encode spaces or other characters when converting to a URI... sigh...
              URI uri = new URI(url.toExternalForm().replaceAll(" ", "%20"));
              try
              {
                  FileSystems.getFileSystem(uri);
              } catch( FileSystemNotFoundException e ) {
                  Map<String, String> env = new HashMap<>();
                  env.put("create", "true");
                  FileSystems.newFileSystem(uri, env);
              } catch(IllegalArgumentException ee) {
                  FileSystems.getDefault();
              }

              return Paths.get(uri);
          }
          @Override
          public Path getPath(List<String> path) throws Exception {
              String p = String.join("/", path).replace("//", "/");
              return getPath(p);
          }

          @Override
          public Path getResourceDirPath(List<String> path) throws Exception {
              List<String> parts = new ArrayList<>();
              parts.add(resourceDir);
              parts.addAll(path);
              return getPath(parts);
          }
          @Override
          public Path getRulesDirPath(List<String> path) throws Exception {
              List<String> parts = new ArrayList<>();
              parts.add(rulesDir);
              parts.addAll(path);
              return getPath(parts);
          }
      });
  }

  /**
   * A convenience constructor for sub-classes that want to use the class path lookup and default resource and rules dir.
   * Is the same as a call to
   */
  protected DefaultResourceDataBroker(Language lang, ClassLoader classLoader) throws Exception {
      this(DEFAULT_RESOURCE_DIR, DEFAULT_RULES_DIR, lang, classLoader);
  }

  /**
   * Create a new broker from the specified paths meaning that any path from any file system can be used.
   *
   * @param pathProvider When a path is requested for a resource this provider is used to map the string... to a real path that can be used.
   * @param lang The language this broker is for.
   * @param classLoader The class loader to use for loading classes, can be null in which case the brokers own class loader will be used.
   */
  public DefaultResourceDataBroker(Language lang, ClassLoader classLoader, PathProvider pathProvider) throws IOException {
      Objects.requireNonNull(pathProvider);
      Objects.requireNonNull(lang, "Language must be given.");
      this.pathProvider = pathProvider;
      this.classLoader = (classLoader == null) ? this.getClass().getClassLoader() : classLoader;
      this.ruleFilterCreator = new RuleFilterCreator(this.classLoader);
      this.language = lang;
  }

  /**
   * Return a path created using our path provider.
   *
   * @param paths The path parts.
   * @return The path.
   */
  public Path getPath(String... paths) throws Exception {
     return pathProvider.getPath(Arrays.asList(paths));
  }

  public PathProvider getPathProvider() throws Exception {
      return pathProvider;
  }

  private PropertyResourceBundle createMessageBundle(String... ids) throws Exception {
      String id = "";
      if (ids != null && ids.length > 0) {
          id = "_" + String.join("_", ids);
      }

      String fname = String.format(MESSAGE_BUNDLE_FILE_NAME, id);
      return createMessageBundle(pathProvider.getPath(Arrays.asList(fname)));
  }

  /**
   * Get the resource bundle, this uses the language locale to find a file called:
   *   /org/languagetool/MessageBundle_<language>_<country>_<variant>.properties
   * in the classpath.
   *
   * If that can't be found then file:
   *  /org/languagetool/MessageBundle_<language>_<country>.properties
   * is looked for.
   *
   * If that can't be found then file:
   *  /org/languagetool/MessageBundle_<language>.properties
   * is looked for.
   *
   * If that can't be found then file:
   *  /org/languagetool/MessageBundle_en.properties
   * is looked for.
   *
   * If that can't be found then file:
   *  /org/languagetool/MessageBundle.properties
   * is looked for.
   *
   * Whichever is found first is used and returned, the "en" or blank language bundle is then used as the parent for the language bundle.
   */
  @Override
  public ResourceBundle getMessageBundle() throws Exception {
      if (messageBundle != null) {
          return messageBundle;
      }

      Locale loc = language.getLocale();
      String v = loc.getVariant();
      String c = loc.getCountry();
      String l = loc.getLanguage();
      String fname;
      PropertyResourceBundle bundle = null;
      // Check for language/country/variant
      if (!"".equals(v) && !"".equals(c) && !"".equals(l)) {
          bundle = createMessageBundle(l, c, v);
      }

      // Check for language/country
      if (bundle == null && "".equals(v) && !"".equals(c) && !"".equals(l)) {
          bundle = createMessageBundle(l, c);
      }

      // Check for language
      if (bundle == null && "".equals(v) && "".equals(c) && !"".equals(l)) {
          bundle = createMessageBundle(l);
      }

      PropertyResourceBundle parent;
      // Check for "en" default.
      parent = createMessageBundle(DEFAULT_MESSAGE_BUNDLE_LANGUAGE_CODE);
      if (parent == null) {
          parent = createMessageBundle();
      }

      if (bundle == null) {
          if (parent == null) {
              throw new IOException(String.format("Unable to find either the message bundle for: %1$s or the default 'en' or default message bundles.", l));
          }
          messageBundle = parent;
          return messageBundle;
      }

      messageBundle = new ResourceBundleWithFallback(bundle, parent);
      return messageBundle;
  }

  public static PropertyResourceBundle createMessageBundle(Path path) throws IOException {
      if (path == null) {
          return null;
      }
      path = path.toRealPath();
      if (Files.notExists(path)) {
          return null;
      }
      try (Reader r = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
          return new PropertyResourceBundle(r) {
              @Override
              public void setParent(ResourceBundle parent) {
                  super.setParent(parent);
              }
          };
      } catch(Exception e) {
          throw new IOException(String.format("Unable to load resource bundle from path: %1$s", path), e);
      }
  }

  /**
   * This operation is not supported by the default data broker (because it doesn't have the relevant dictionary file).
   *
   * @throws UnsupportedOperationException
   */
  @Override
  public WordTagger getWordTagger() throws Exception {
      throw new UnsupportedOperationException("Not supported by this data broker.");
  }

  /**
   * Returns a Word2VecModel if it exists in the resource dir.  To specify your own location for the model see {@link createWord2VecModel(Path,Path)}.
   *
   * @return The word 2 vec model if it exists in the resource dir, otherwise return null.
   */
  @Override @Nullable
  public Word2VecModel getWord2VecModel() throws Exception {
      if (word2VecModel == null) {
          word2VecModel = createWord2VecModelFromResourceDir();
      }
      return word2VecModel;
  }

  @Nullable
  public Word2VecModel createWord2VecModelFromResourceDir() throws Exception {
      String dictFile = String.format(WORD_2_VEC_MODEL_DICT_FILE_NAME, language.getLocale().getLanguage());
      if (resourceDirPathExists(dictFile)) {
          return createWord2VecModel(getResourceDirPath(dictFile), getResourceDirPath((String.format(WORD_2_VEC_MATRIX_FILE_NAME, language.getLocale().getLanguage()))));
      }
      return null;
  }

  public static Word2VecModel createWord2VecModel(Path dictPath, Path matrixPath) throws Exception {
      Objects.requireNonNull(dictPath, "Path to the neural network dictionary must be provided.");
      Objects.requireNonNull(matrixPath, "Path to the maxtrix mappings must be provided.");
      dictPath = dictPath.toRealPath();
      matrixPath = matrixPath.toRealPath();
      org.languagetool.rules.neuralnetwork.Dictionary dictionary;
      Matrix embedding;
      try (InputStream is = Files.newInputStream(dictPath)) {
          dictionary = new org.languagetool.rules.neuralnetwork.Dictionary(is);
      } catch(Exception e) {
          throw new IOException(String.format("Unable to load neural network dictionary from: %1$s", dictPath), e);
      }
      try (InputStream is = Files.newInputStream(matrixPath)) {
          embedding = new Matrix(is);
      } catch(Exception e) {
          throw new IOException(String.format("Unable to load neural network matrix from: %1$s", matrixPath), e);
      }
      Embedding em = new Embedding(dictionary, embedding);
      return new Word2VecModel(em);
  }

  /**
   * Returns a NullAssignTagger.
   *
   * @return A tagger that will assign a null tag to each word.
   */
  @Override @NotNull
  public Tagger getTagger() throws Exception {
      if (tagger == null) {
          tagger = new NullAssignTagger();
      }
      return tagger;
  }

  /**
   * Returns a null chunker by default, sub-classes should provide their own chunkers.
   *
   * @return null.
   */
  @Override @Nullable
  public Chunker getChunker() throws Exception {
      return null;
  }

  /**
   * Get the case converter, just returns a DefaultCaseConverter instance.
   *
   * @return The case converter.
   */
  @Override
  public CaseConverter getCaseConverter() {
      if (caseConverter == null) {
          caseConverter = new DefaultCaseConverter(language);
      }
      return caseConverter;
  }

  /**
   * Get the word tokenizer, this just returns a standard WordTokenizer instance.
   *
   * @return The tokenizer.
   */
  @Override
  public Tokenizer getWordTokenizer() throws Exception {
      if (wordTokenizer == null) {
          wordTokenizer = new WordTokenizer();
      }
      return wordTokenizer;
  }

  @Override
  public Language getLanguage() {
      return language;
  }

  /**
   * Get the confusion sets for the language from file:
      - /resource/<locale.languagecode>/confusion_sets.txt
   */
  public Map<String,List<ConfusionSet>> getConfusionSets() throws Exception {
      String file = String.format(CONFUSION_SETS_FILE_NAME, language.getLocale().getLanguage());
      Path path = getResourceDirPath(file);
      return getConfusionSetFromResourcePath(file, DEFAULT_CHARSET);
  }

  public Map<String, List<ConfusionSet>> getConfusionSetFromResourcePath(String path, Charset charset) throws Exception {
      if (!resourceDirPathExists(path)) {
          return null;
      }
      return confusionSetsCache.get(new PathCacheKey(getResourceDirPath(path), charset));
  }

  /**
   * Create a set of wrong words from the specified path.  The path should point to a file that contains a number of lines.  Comment (#...) and empty lines are ignored.
   * Each line is expected to be in the format:
   *     [word[|word...]=[replacement[|replacement...]
   *
   * e.g.
   *     two1|one2=one|two
   * The returned map contains an entry for each [word] mapped to a list of all the specified replacements, for the example above
   * this means you would get:
   *     String(two1) -> List(String(one), String(two))
   *     String(one2) -> List(String(one), String(two))
   *
   * @param path The path to load from.
   * @param charset The charset to use to read the input stream.  Defaults to DEFAULT_CHARSET if not specified.
   * @return The wrong word mappings.
   */
  public static Map<String, List<String>> createWrongWords(Path path, Charset charset) throws Exception {
      Objects.requireNonNull(path, "Path must be specified.");
      path = path.toRealPath();
      if (Files.notExists(path)) {
          return null;
      }
      charset = charset != null ? charset : DEFAULT_CHARSET;
      try (InputStream is = Files.newInputStream(path)) {
          // GTODO Change to use a StringProcessor.
          Map<String, List<String>> map = new HashMap<>();
          try (java.util.Scanner scanner = new java.util.Scanner(is, charset.name())) {
            while (scanner.hasNextLine()) {
              String line = scanner.nextLine();
              if (line.isEmpty() || line.charAt(0) == '#') { // # = comment
                continue;
              }
              String[] parts = line.split("=");
              if (parts.length != 2) {
                throw new IOException("Error in line '" + line + "', expected format 'word=replacement'");
              }
              String[] wrongForms = parts[0].split("\\|");
              List<String> replacements = Arrays.asList(parts[1].split("\\|"));
              for (String wrongForm : wrongForms) {
                map.put(wrongForm, replacements);
              }
            }
          }
          return map;
      } catch(Exception e) {
          throw new IOException(String.format("Unable to load wrong words from: %1$s", path), e);
      }
  }

  /**
   * Loads a set of wrong words from the rules path specified.  This creates the rules path resolves it via the pathProvider then
   * calls {@link createWrongWords(Path)}.  created Map is not cached or stored, this is a convenience method for subclasses to use.
   *
   * @param path The path to load from.
   * @return The wrong words created from the path, if the path doesn't exist then {@code null} is returned.
   */
  public Map<String, List<String>> createWrongWordsFromRulesPath(String path) throws Exception {
      if (rulesDirPathExists(path)) {
          return createWrongWords(getRulesDirPath(path), DEFAULT_CHARSET);
      }
      return null;
  }

  public static List<Map<String, String>> createWrongWords2(Path path, Tokenizer wordTokenizer, Charset charset) throws Exception {
      Objects.requireNonNull(path, "Path must be specified.");
      path = path.toRealPath();
      if (Files.notExists(path)) {
          return null;
      }
      charset = charset != null ? charset : DEFAULT_CHARSET;
      List<Map<String, String>> list = new ArrayList<>();
      try (Scanner br = new Scanner(path, charset.name())) {
        String line;
        while (br.hasNextLine()) {
            line = br.nextLine().trim();
          if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
            continue;
          }

          String[] parts = line.split("=");
          if (parts.length != 2) {
            throw new IOException("Expected exactly 1 '=' character. Line: " + line);
          }

          String[] wrongForms = parts[0].split("\\|"); // multiple incorrect forms
          for (String wrongForm : wrongForms) {
            int wordCount = 0;
            List<String> tokens = wordTokenizer.tokenize(wrongForm);
            for (String token : tokens) {
              if (!StringTools.isWhitespace(token)) {
                wordCount++;
              }
            }
            // grow if necessary
            for (int i = list.size(); i < wordCount; i++) {
              list.add(new HashMap<>());
            }
            list.get(wordCount - 1).put(wrongForm, parts[1]);
          }
        }
      }
      // seal the result (prevent modification from outside this class)
      List<Map<String,String>> result = new ArrayList<>();
      for (Map<String, String> map : list) {
        result.add(Collections.unmodifiableMap(map));
      }
      return Collections.unmodifiableList(result);
  }

  /**
   * Loads a set of wrong words from the rules path specified.  This is designed for use with AbstractSimpleReplaceRule2.
   *
   */
  public List<Map<String, String>> createWrongWords2FromRulesPath(String path, Tokenizer tokenizer) throws Exception {
      if (rulesDirPathExists(path)) {
          return createWrongWords2(getRulesDirPath(path), tokenizer, DEFAULT_CHARSET);
      }
      return null;
  }

  /**
   *  We have two files:
   *  - /rules/en/grammar.xml
   *  - /rules/en-<locale.countrycode>/grammar.xml
   */
  @Override
  public List<AbstractPatternRule> getPatternRules() throws Exception {
      if (patternRules != null) {
          return patternRules;
      }
      Locale locale = language.getLocale();
      List<AbstractPatternRule> rules = new ArrayList<>();
      String genFile = String.format(GENERAL_PATTERN_RULES_FILE_NAME, locale.getLanguage());
      if (rulesDirPathExists(genFile)) {
          try {
              rules.addAll(getPatternRules(getRulesDirPath(genFile), getRuleFilterCreator()));
          } catch(Exception e) {
              throw new IOException(String.format("Unable to load pattern rules from: %1$s", genFile), e);
          }
      }

      if (locale != null) {
          String localeFile = String.format(LOCALE_VARIANT_PATTERN_RULES_FILE_NAME, locale.getLanguage(), locale.getCountry());
          if (rulesDirPathExists(localeFile)) {
              try {
                  rules.addAll(getPatternRules(getRulesDirPath(localeFile), getRuleFilterCreator()));
              } catch(Exception e) {
                  throw new IOException(String.format("Unable to load pattern rules from: %1$s", localeFile), e);
              }
          }
      }
      patternRules = rules;
      return patternRules;
  }

  /**
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsStream(String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /resource}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /resource<b>/yourpath</b>.
   * @return An {@link InputStream} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
   /*
  protected InputStream getFromResourceDirAsStream(String path) {
    String completePath = getCompleteResourceUrl(path);
    InputStream resourceAsStream = getResourceAsStream(completePath, classLoader);
    assertNotNull(resourceAsStream, path, completePath);
    return resourceAsStream;
  }

  private InputStream getResourceAsStream(String completePath) {
      return getResourceAsStream(completePath, classLoader);
  }
*/
/*
  private InputStream getResourceAsStream(String completePath, ClassLoader cl) {
      if (cl == null) {
          return null;
      }
      InputStream resourceAsStream = null;
      // Query our parent classloader first.
      if (cl.getParent() != null) {
          resourceAsStream = getResourceAsStream(completePath, cl.getParent());
          if (resourceAsStream == null) {
              if (completePath.startsWith("/")) {
                  resourceAsStream = getResourceAsStream(completePath.substring(1), cl.getParent());
              }
          }
      }
      if (resourceAsStream == null) {
          resourceAsStream = cl.getResourceAsStream(completePath);
          if (resourceAsStream == null) {
              // Check for a leading / which can cause issues with a non-system classloader.
              if (completePath.startsWith("/")) {
                  resourceAsStream = getResourceAsStream(completePath.substring(1), cl);
              }
          }
      }
      return resourceAsStream;
  }
  */
/*
  private URL getResourceAsURL(String completePath) {
      return getResourceAsURL(completePath, classLoader);
  }

  private URL getResourceAsURL(String completePath, ClassLoader cl) {
      if (cl == null) {
          return null;
      }

      URL url = cl.getResource(completePath);
      if (url == null) {
          // Check for a leading / which can cause issues with a non-system classloader.
          if (completePath.startsWith("/")) {
              return getResourceAsURL(completePath.substring(1), cl);
          }
          return getResourceAsURL(completePath, cl.getParent());
      }
      return url;
  }
*/
  /**
  GTODO Clean up
   * See:
   * {@link ResourceDataBroker#getFromResourceDirAsUrl(String)}
   *
   * @param path
   *            The relative path to the item inside of the {@code /resource}
   *            directory. Please start your path information with {@code /}
   *            because it will be concatenated with the directory's name:
   *            /resource<b>/yourpath</b>.
   * @return An {@link URL} object to the requested item
   * @throws RuntimeException if path cannot be found
   */
   /*
  private URL getFromResourceDirAsUrl(String path) {
      Path p = resourceDirPath.resolve(path);
      xxx
      URL resDir
      Path p = Paths.get(resourceDir, path);
    String completePath = getCompleteResourceUrl(path);
    URL resource = getResourceAsURL(completePath, classLoader);
    assertNotNull(resource, path, completePath);
    return resource;
  }
*/

  public Path getResourceDirPath(String... path) throws Exception {
      return getResourceDirPath(Arrays.asList(path));
  }

  /**
   * Resolve the passed in path against the {@link resourceDirPath}, then get the real path and check it exists, if not then throw an IllegalArgumentException.
   *
   * @param path Resolve the path against the resource dir path.
   * @return The resolved path.
   */
  public Path getResourceDirPath(List<String> path) throws Exception {
      Path p = pathProvider.getResourceDirPath(path);
      try {
          p = p.toRealPath();
          if (!Files.notExists(p)) {
              return p;
          }
          return null;
      } catch(Exception e) {
          throw new IllegalArgumentException(String.format("Resource path %1$s does not exist. ", path));
      }
  }

  public InputStream getResourceDirPathStream(String path) throws Exception {
      return Files.newInputStream(getResourceDirPath(path));
  }

  public boolean resourceDirPathExists(String... path) {
      return resourceDirPathExists(Arrays.asList(path));
  }

  public boolean resourceDirPathExists(List<String> path) {
      try {
          // Will throw an exception if not present...
          getResourceDirPath(path);
          return true;
      } catch(Exception e) {
          return false;
      }
  }

  public boolean rulesDirPathExists(String... path) {
      return rulesDirPathExists(Arrays.asList(path));
  }

  public boolean rulesDirPathExists(List<String> path) {
      try {
          // Will throw an exception if not present...
          getRulesDirPath(path);
          return true;
      } catch(Exception e) {
          return false;
      }
  }

  /**
   * Resolve the passed in path against the {@link rulesDirPath}, then get the real path and check it exists, if not then throw an IllegalArgumentException.
   *
   * @param path Resolve the path against the rules dir path.
   * @return The resolved path.
   */
  public Path getRulesDirPath(String... path) throws Exception {
      return getRulesDirPath(Arrays.asList(path));
  }

  public Path getRulesDirPath(List<String> path) throws Exception {
      Path p = pathProvider.getRulesDirPath(path);
      try {
          p = p.toRealPath();
          if (!Files.notExists(p)) {
              return p;
          }
          return null;
      } catch(Exception e) {
          throw new IllegalArgumentException(String.format("Rules path %1$s does not exist.", p));
      }
  }

  public InputStream getRulesDirPathStream(String path) throws Exception {
      return Files.newInputStream(getRulesDirPath(path));
  }

  /**
   * @return The class loader used to load resources.
   */
  public ClassLoader getClassLoader() {
      return classLoader;
  }

  /**
   * @param className The fully qualified name of the class to load.
   * @return The requested class.
   * @throws ClassNotFoundException If the class cannot be loaded.
   */
   @Override
   public Class getClass(String className) throws ClassNotFoundException {
       return Class.forName(className, true, classLoader);
   }

   public static WordTagger createWordTagger(MorfologikTagger morfoTagger, Path addedWords, Path removedWords, boolean overwriteWithAddedTagger) throws Exception {
       ManualTagger removalTagger = null;
       if (removedWords != null && Files.exists(removedWords)) {
           removedWords = removedWords.toRealPath();
           try (InputStream stream = Files.newInputStream(removedWords)) {
             removalTagger = new ManualTagger(stream);
         } catch(Exception e) {
             throw new IOException(String.format("Unable to load removed words from path: %1$s", removedWords), e);
         }
       }

       if (addedWords != null && Files.exists(addedWords)) {
          addedWords = addedWords.toRealPath();
          try (InputStream stream = Files.newInputStream(addedWords)) {
             ManualTagger manualTagger = new ManualTagger(stream);
             return new CombiningTagger(morfoTagger, manualTagger, removalTagger, overwriteWithAddedTagger);
          } catch(Exception e) {
             throw new IOException(String.format("Unable to load added words from path: %1$s", addedWords), e);
          }
       } else {
           return morfoTagger;
       }

   }

   public WordTagger createWordTaggerFromResourcePath(MorfologikTagger morfoTagger, boolean overwriteWithAddedTagger) throws Exception {
       String manualRemovalFileName = String.format(WORD_TAGGER_REMOVED_WORDS_FILE_NAME, language.getLocale().getLanguage());
       String manualAdditionFileName = String.format(WORD_TAGGER_ADDED_WORDS_FILE_NAME, language.getLocale().getLanguage());
       Path manualRemovalPath = null;
       Path manualAdditionPath = null;
       if (resourceDirPathExists(manualRemovalFileName)) {
           manualRemovalPath = getResourceDirPath(manualRemovalFileName);
       }

       if (resourceDirPathExists(manualAdditionFileName)) {
          manualAdditionPath = getResourceDirPath(manualAdditionFileName);
      }
      return createWordTagger(morfoTagger, manualAdditionPath, manualRemovalPath, overwriteWithAddedTagger);
   }

   public Dictionary getMorfologikBinaryDictionaryFromResourcePath(String path) throws Exception {
       if (resourceDirPathExists(path)) {
           return binaryMorfologikDictCache.get(new PathCacheKey(getResourceDirPath(path)));
       }
       return null;
   }

   /**
    * Create a new morfologik dictionary from the specified path.  The morfologik code assumes that there is also a <name>.info file
    * available as a sibling to the specified path.
    *
    * @param path The path to the binary dictionary file.
    * @return The dictionary.
    */
   public static Dictionary createMorfologikBinaryDictionary(Path path) throws IOException {
       Objects.requireNonNull(path, "Path must not be null.");
       path = path.toRealPath();
       if (Files.exists(path)) {
           try {
               return Dictionary.read(path);
           } catch (Exception e) {
               throw new IOException(String.format("Unable to load morfologik dictionary from path: %1$s", path), e);
           }
       }
       return null;
   }

   /**
    * Always return a null model, subclasses should override to return a real model.
    *
    * @return {@code null}.
    */
   @Nullable @Override
   public LanguageModel getLanguageModel() throws Exception {
       return null;
   }

   /**
    * Get the predictor for the specified rule.  Note: this has the side effect of loading the language model.
    * GTODO More doco
    */
   @Nullable
   public Predictor getRulePredictor(Rule rule) throws Exception {
       if (rule == null) {
           return null;
       }
       // GTODO Move the rule config to the relevant language-model subdir.  Then change this to use short code...
        // See if we have a language model...
        LanguageModel model = getLanguageModel();
        String file = (model == null ? PREDICTOR_RULE_NO_NGRAM_BASED_MODEL_FILENAME : PREDICTOR_RULE_SPC_NGRAM_BASED_MODEL_FILENAME);
        List<String> pathParts = Arrays.asList(PREDICTOR_RULE_MODEL_BASE_PATH, rule.getId(), file);

        if (!resourceDirPathExists(pathParts)) {
            // Default to the no-ngram config.
            pathParts = Arrays.asList(PREDICTOR_RULE_MODEL_BASE_PATH, PREDICTOR_RULE_NO_NGRAM_BASED_MODEL_FILENAME);
        }
        if (!resourceDirPathExists(pathParts)) {
            return null;
        }

        Path path = getResourceDirPath(pathParts);
        try (InputStream is = Files.newInputStream(path)) {
            return new Predictor(is);
        } catch(Exception e) {
            throw new IOException(String.format("Unable to create predictor from: %1$s", path));
        }
    }

   /**
    * Get the language model.  This will search the resources dir /[language short code]/ngram-index for subdirs called index-[n] where n is an integer in the range 1-10 (arbitrary limit) and then for each of those call:
    * {@link getLuceneIndexSearchersFromResourcePath()} on the dir, each subdir is treated as a single index and a {@link LuceneSingleIndexLanguageModel} created.
    * If no index-[n] subdirs are found then the /[language short code]/ngram-index is passed to {@link getLuceneIndexSearchersFromResourcePath}
    * instead, i.e. we assume we have just a single index.  If you require more fine grain controlled then consider calling {@link getLuceneIndexSearchersFromResourcePath} yourself.
    *
    * @return A LuceneLanguageModel built from a resource dir with path /[language short code]/ngram-index.
    */
   public LuceneLanguageModel createLanguageModelFromResourcePath() throws Exception {
       final String dir = String.format(NGRAM_INDEX_DIR_NAME, language.getLocale().getLanguage());
       if (!resourceDirPathExists(dir)){
           return null;
       }
       return createLuceneLanguageModel(getResourceDirPath(dir));
   }

   /**
    * Load a mapping of ngram index to IndexSearcher from the resource dir for the associated language.  Subdirs of dir called [n]grams are looked for to load the appropriate
    * ngram index, where n is an integer in the range 1 to 4.
    *
    * Thus if our language has a short code of "xx" then we are looking in resource path: /xx/ngram-index for subdirs called 1grams, 2grams, ... 4grams to load the searchers.
    *
    * @return A mapping of ngram index to lucene IndexSearcher to use for the index.  Returns {@code null} if there is no ngram-index dir for the language.
    */
   public Map<Integer, IndexSearcher> getLuceneIndexSearchersFromResourcePath(String dir) throws Exception {
       Path path = getResourceDirPath(dir);
       return getLuceneIndexSearchers(path);
   }

   /**
    * Creates a LuceneLanguageModel from a directory in the file system.  It optionally supports multiple indexes, expecting each index to be in a subdir
    * called index-[n], {@link getLuceneIndexSearchers(File)} is then called on each dir and a LuceneSingleIndexLanguageModel created.
    *
    * If no index-[n] subdirs are found then a LuceneSingleIndexLanguageModel is created from the index in {@code dir}.  Note: these are mutually exclusive,
    * if any index-[n] subdirs are found then they are used for the indexes, only if no index-[n] dirs are found then dir is used to create the index.
    *
    */
   public static LuceneLanguageModel createLuceneLanguageModel(Path dir) throws Exception {
       Objects.requireNonNull(dir, "The directory must be provided.");
       dir = dir.toRealPath();
       if (Files.notExists(dir)) {
           throw new IllegalArgumentException(String.format("Path %1$s does not exist.", dir));
       }

       if (!Files.isDirectory(dir)) {
           throw new IllegalArgumentException(String.format("Path %1$s is not a directory.", dir));
       }
       Function<Path, LuceneSingleIndexLanguageModel> mapper = p -> {
           try {
               return new LuceneSingleIndexLanguageModel(getLuceneIndexSearchers(p));
           } catch(Exception e) {
               throw new RuntimeException(String.format("Unable to create language model from path: %1$s", p), e);
           }
       };

       PathMatcher filter = dir.getFileSystem().getPathMatcher("regex:^index-\\d+$");
       Set<LuceneSingleIndexLanguageModel> indexes = Files.list(dir)
            // Only interested in dirs ending with called index-[n].
            .filter(p -> Files.isDirectory(p) && filter.matches(p.getFileName()))
            // Map the dir name to a searcher.
            .map(mapper)
            .collect(Collectors.toSet());

       if (indexes.size() == 0) {
           indexes = new LinkedHashSet<>();
           indexes.add(mapper.apply(dir));
       }
       return new LuceneLanguageModel(indexes);
   }

   /**
    * Creates n Lucene IndexSearchers from the dir path passed in.  Each searcher is loaded from a [n]grams subdir of dir.
    * So if the dir path is: /example/ then subdir 1grams will be used to create an IndexSearcher and then 1 set as the index
    * in the returned map, 2grams will become index 2 and so on.
    */
   public static Map<Integer, IndexSearcher> getLuceneIndexSearchers(Path dir) throws Exception {
       Objects.requireNonNull(dir, "The directory must be provided.");
       Path path = dir.toRealPath();
       if (Files.notExists(dir)) {
           throw new IllegalArgumentException(String.format("Path %1$s does not exist.", dir));
       }

       if (!Files.isDirectory(dir)) {
           throw new IllegalArgumentException(String.format("Path %1$s is not a directory.", dir));
       }

       PathMatcher filter = path.getFileSystem().getPathMatcher("regex:^\\d*grams$");
       return Files.list(path)
            // Only interested in dirs ending with [n]grams.
            .filter(p -> Files.isDirectory(p) && filter.matches(p.getFileName()))
            // Map the dir name to a searcher.
            .collect(Collectors.toMap(p -> {
                String n = p.getFileName().toString();
                int ind = n.indexOf("grams");
                n = n.substring(0, ind);
                return Integer.parseInt(n);
            },
            p -> createLuceneIndexSearcher(p)));
   }

   /**
    * Create a new Lucene IndexSearcher for the specified path.
    *
    * @param path The path to use, this assumes that the path is a directory.
    * @return The searcher.
    */
   public static IndexSearcher createLuceneIndexSearcher(Path path) {
       try {
           path = path.toRealPath();
           FSDirectory directory = FSDirectory.open(path);
           IndexReader reader = DirectoryReader.open(directory);
           return new IndexSearcher(reader);
       } catch(Exception e) {
           throw new RuntimeException(String.format("Unable to create index searcher from path: %1$s", path.toString()), e);
       }
   }

   public List<String> loadSpellingIgnoreWordsFromResourcePath(String baseSpellingFile, String ignoreFile) throws Exception {
       List<String> words = new ArrayList<>();
       if (resourceDirPathExists(baseSpellingFile)) {
           words.addAll((List<String>) getWordListFromResourcePath(baseSpellingFile, spellingWordProcessor));
       }
       // GTODO Dodgy cast
       if (resourceDirPathExists(ignoreFile)) {
           words.addAll((List<String>) getWordListFromResourcePath(ignoreFile, spellingWordProcessor));
       }
       return words;
   }

   public List<String> loadSpellingProhibitedWordsFromResourcePath(String file) throws Exception {
       List<String> words = new ArrayList<>();
       if (resourceDirPathExists(file)) {
           // GTODO Dodgy cast
           words.addAll((List<String>) getWordListFromResourcePath(file, spellingWordProcessor));
       }
       return words;
   }

   public Dictionary getMorfologikTextDictionaryFromResourcePath(String wordsPath, String infoPath, Charset charset) throws Exception {
       return getMorfologikTextDictionaryFromResourcePaths(Arrays.asList(wordsPath), infoPath, charset);
   }

   /**
    * Create a new Morofologik dictionary from the text files pointed to be {@code paths}.  The paths are expected to exist and be foudn in the
    * resource dir.  This method uses the textMorfologikDictCache for caching of the created dictionary against the resolved paths.
    */
   public Dictionary getMorfologikTextDictionaryFromResourcePaths(List<String> paths, String infoPath, Charset charset) throws Exception {
       Objects.requireNonNull(infoPath, "Path to dictionary info file must be provided.");
       Objects.requireNonNull(paths, "Paths to text dictionary files must be provided.");
       if (paths.size() == 0) {
           throw new IllegalArgumentException("At least one path to a text dictionary file must be provided.");
       }
       Path ip = getResourceDirPath(infoPath);
       List<Path> rpaths = paths.stream()
            .map(p ->
            {
                try {
                    return getResourceDirPath(p);
                } catch(Exception e) {
                    throw new RuntimeException(String.format("Unable to get resource path: %1$s", p), e);
                }
            })
            .collect(Collectors.toList());
       Optional<Dictionary> dict = textMorfologikDictCache.get(new MorfologikTextDictCacheKey(rpaths, ip, charset));
       return dict.isPresent() ? dict.get() : null;
   }

   /**
    * Create a new Morfologik Dictionary from the list of text word files passed in.  The info path should point to the information for the
    * dictionary.
    * It uses the morfologikSpellerTextWordListStringProcessor processor and accesses the wordListCache for the word list files.
    */
   public static Dictionary createMorfologikTextDictionary(List<Path> wordsPaths, Path infoPath, Charset charset) throws Exception {
       Objects.requireNonNull(wordsPaths, "Paths to text dictionary files must be provided.");
       Objects.requireNonNull(infoPath, "Path to the info file must be provided.");
       Path ip = infoPath.toRealPath();
       if (Files.notExists(ip)) {
           throw new IllegalArgumentException(String.format("Unable to find info file: %1$s", infoPath));
       }
       if (wordsPaths.size() == 0) {
           throw new IllegalArgumentException("At least one path to a text dictionary file must be provided.");
       }
       List<byte[]> lines = new ArrayList<>();
       for (Path wp : wordsPaths) {
           if (wp == null) {
               throw new IllegalArgumentException("A null path has been passed.");
           }
           wp = wp.toRealPath();
           if (Files.notExists(wp)) {
               throw new IOException(String.format("Unable to find morfologik text dictionary at path: %1$s", wp));
           }
           try {
               lines.addAll((List<byte[]>) wordListCache.get(new WordsCacheKey(wp, charset, morfologikSpellerTextWordListStringProcessor)));
           } catch (Exception e) {
               throw new IOException(String.format("Unable to load morfologik text dictionary lines for text resource path: %1$s", wp), e);
           }
       }
       if (lines.size() > 0) {
           try (InputStream infoStream = Files.newInputStream(ip)) {
               return DefaultMorfologikDictionaryLoader.loadFromLines(lines, infoStream);
           } catch (Exception e) {
               throw new IOException(String.format("Unable to load morfologik dictionary for text resource paths: %1$s and info resource path: %2$s", wordsPaths, ip), e);
           }
       }
       return null;

   }

   // GTODO Implement a cache for these...
   public Hunspell.Dictionary createHunspellDictionaryFromResourcePath(String fileNamePrefix) throws Exception {
       return createHunspellDictionary(getResourceDirPath(String.format(HUNSPELL_BASE_DIR, language.getLocale().getLanguage())), fileNamePrefix);
   }

   /**
    * Load a Hunspell dictionary from files:
    *    <basePath>/<fileNamePrefix>.dic (dictionary file)
    *    <basePath>/<fileNamePrefix>.aff (affixes file)
    */
   public static Hunspell.Dictionary createHunspellDictionary(Path basePath, String fileNamePrefix) throws Exception {
       Objects.requireNonNull(basePath, "Base path must be provided.");
       Objects.requireNonNull(fileNamePrefix, "File name prefix must be provided.");
       basePath = basePath.toRealPath();
       String dicFileName = fileNamePrefix + HUNSPELL_DIC_FILE_NAME_EXT;
       Path dicFilePath = basePath.resolve(dicFileName);
       try {
           dicFilePath = dicFilePath.toRealPath();
       } catch(Exception e) {
           // This isn't an error, it just means the file doesn't exist.
           return null;
       }

       String affFileName = fileNamePrefix + HUNSPELL_AFF_FILE_NAME_EXT;
       Path affFilePath = basePath.resolve(affFileName);
       try {
           affFilePath = affFilePath.toRealPath();
       } catch(Exception e) {
           // This IS an error since the .dic file exists.
           throw new IllegalArgumentException(String.format("The path to the dictionary file exists: %1$s, but the path to the affix file does not: %2$s", dicFilePath, affFilePath));
       }

       URL dictFileURL = dicFilePath.toUri().toURL();
       String dictionaryPath;
       if (StringUtils.equalsAny(dictFileURL.getProtocol(), "jar", "vfs", "bundle", "bundleresource")) {
           // GTODO Move this to use paths.
           // Copy the .dic and .aff files to temporary files outside of the resource bundle, then use those files instead.
           File tempDicFile = File.createTempFile("hunspell-" + fileNamePrefix, HUNSPELL_DIC_FILE_NAME_EXT);
           tempDicFile.deleteOnExit();
           JLanguageTool.addTemporaryFile(tempDicFile);
           String tempDicFileName = tempDicFile.getName();
           // Copy the .dic file to the new temporary file location.
           try (InputStream dicStream = Files.newInputStream(dicFilePath)) {
             fileCopy(dicStream, tempDicFile);
           } catch(Exception e) {
               throw new IOException(String.format("Unable to copy %1$s to temporary file: %2$s", dicFilePath, tempDicFile), e);
           }

           String tempDicFileNameWithoutSuffix = tempDicFile.getName().substring(0, tempDicFileName.lastIndexOf(HUNSPELL_DIC_FILE_NAME_EXT));
           // Now create a temporary file for the .aff and copy it next to the .dic file.
           File tempAffFile = new File(tempDicFile.getParentFile(), tempDicFileNameWithoutSuffix + HUNSPELL_AFF_FILE_NAME_EXT);
           tempAffFile.deleteOnExit();
           JLanguageTool.addTemporaryFile(tempAffFile);
           try (InputStream affStream = Files.newInputStream(affFilePath)) {
             fileCopy(affStream, tempAffFile);
           } catch(Exception e) {
               throw new IOException(String.format("Unable to copy %1$s to temporary file: %2$s", affFilePath, tempAffFile), e);
           }
           dictionaryPath = tempDicFile.getParentFile().toPath().toRealPath().resolve(tempDicFileNameWithoutSuffix).toString();
       } else {

           // Just use the .dic file with the suffix cut off.
           String p = dicFilePath.toString();
           dictionaryPath = p.substring(0, p.lastIndexOf(HUNSPELL_DIC_FILE_NAME_EXT));
       }
       return Hunspell.getInstance().getDictionary(dictionaryPath);
   }

   // GTODO Move to a better place, some kind of utils.
     private static void fileCopy(InputStream in, File targetFile) throws IOException {
       try (OutputStream out = new FileOutputStream(targetFile)) {
         byte[] buf = new byte[1024];
         int len;
         while ((len = in.read(buf)) > 0) {
           out.write(buf, 0, len);
         }
         in.close();
       }
     }

     private static String addContextWordBoundaries(String str) {
       String ignoreCase = "";
       if (str.startsWith("(?i)")) {
         str = str.substring(4);
         ignoreCase = "(?i)";
       }
       return ignoreCase + "\\b(" + str + ")\\b";
     }

     public Map<String, List<MultiWordEntry>> createMultiWordChunker2MappingFromResourcePath(String path, Charset charset) throws IOException {
         charset = (charset == null ? DEFAULT_CHARSET : charset);
         try {
           List<String[]> posTokens = (List<String[]>) loadWordsFromResourcePath(path, charset, multiWordChunkerStringProcessor);
           Map<String, List<MultiWordEntry>> map = new HashMap<>();
           for (String[] posToken : posTokens) {
             String[] tokens = posToken[0].split(" ");
             String posTag = posToken[1];

             List<MultiWordEntry> multiwordItems;
             if( map.containsKey(tokens[0]) ) {
               multiwordItems = map.get(tokens[0]);
             }
             else {
               multiwordItems = new ArrayList<>();
               map.put(tokens[0], multiwordItems);
             }
             multiwordItems.add(new MultiWordEntry(Arrays.asList(tokens), posTag));
           }
           return map;
         } catch (Exception e) {
           throw new IOException(String.format("Unable to convert file: %1$s to multi word chunker.", path), e);
         }
     }

     public MultiWordChunker2 createMultiWordChunker2FromResourcePath(String path, Charset charset, boolean allowFirstCapitalized) throws IOException {
         return new MultiWordChunker2(createMultiWordChunker2MappingFromResourcePath(path, charset), allowFirstCapitalized);
     }

     // GTODO Use a string processor.
     public static List<ContextWords> loadContextWords(Path path) throws IOException {
         Objects.requireNonNull(path, "Path must be specified.");
         path = path.toRealPath();
       try (InputStream is = Files.newInputStream(path);
            Scanner scanner = new Scanner(is, DEFAULT_CHARSET.name()))
       {
         List<ContextWords> set = new ArrayList<>();
         while (scanner.hasNextLine()) {
           String line = scanner.nextLine();
           if (line.trim().isEmpty() || line.charAt(0) == '#') {
             continue;
           }
           String[] column = line.split("\t");
           if (column.length >= 6) {
               ContextWords contextWords = new ContextWords(Pattern.compile(addContextWordBoundaries(column[0])),
                     Pattern.compile(addContextWordBoundaries(column[1])),
                     column[2],
                     column[3],
                     Pattern.compile(addContextWordBoundaries(column[4])),
                     Pattern.compile(addContextWordBoundaries(column[5])),
                     column.length > 6 ? column[6] : null,
                     column.length > 7 ? column[7] : null);

             set.add(contextWords);
           } // if (column.length >= 6)
         }
         return Collections.unmodifiableList(set);
        } catch(Exception e) {
            throw new IOException(String.format("Unable to load context words from: %1$s", path), e);
        }
     }

     /**
      *
      */
     public List getWordListFromResourcePath(String path, StringProcessor processor) throws Exception {
         return getWordListFromResourcePath(path, DEFAULT_CHARSET, processor);
     }

     public List getWordListFromResourcePath(String path, Charset charset, StringProcessor processor) throws Exception {
         Objects.requireNonNull(path, "Path must be provided.");
         Objects.requireNonNull(processor, "String processor must be provided.");
         Path p = getResourceDirPath(path);
         return wordListCache.get(new WordsCacheKey(p, charset, processor));
     }

     public List getWordListFromRulesPath(String path, StringProcessor processor) throws Exception {
         return getWordListFromRulesPath(path, DEFAULT_CHARSET, processor);
     }

     public List getWordListFromRulesPath(String path, Charset charset, StringProcessor processor) throws Exception {
         Objects.requireNonNull(path, "Path must be provided.");
         Objects.requireNonNull(processor, "String processor must be provided.");
         Path p = getRulesDirPath(path);
         return wordListCache.get(new WordsCacheKey(p, charset, processor));
     }

     /**
      * Load a set of words from the path using the specified charset and processing them with the {@code processor} to return a type.
      */
     public static <T> List<T> loadWords(Path path, Charset charset, StringProcessor<T> processor) throws IOException {
        Objects.requireNonNull(path, "Path must be provided.");
        Objects.requireNonNull(processor, "String processor must be provided.");
        if (Files.notExists(path)) {
            throw new IllegalArgumentException(String.format("Path: %1$s does not exist.", path));
        }
        path = path.toRealPath();
        charset = charset != null ? charset : DEFAULT_CHARSET;

        try (InputStream is = Files.newInputStream(path);
             Scanner scanner = new Scanner(is, charset.name()))
        {
          List<T> lines = new ArrayList<>();
          String line;
          int lineNo = -1;
          while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            lineNo++;
            if (processor.shouldSkip(line)) {
                continue;
            }
            Set<String> errors = processor.getErrors(line);
            if (errors != null && errors.size() > 0) {
                throw new IOException(String.format("Error in line[%1$s]: %2$s - %3$s", lineNo, line, errors));
            }
            try {
                lines.add(processor.getProcessed(line));
            } catch(Exception e) {
                throw new IOException(String.format("Error in line[%1$s]: %2$s", lineNo, line), e);
            }
          }
          return lines;
        } catch(Exception e) {
            throw new IOException(String.format("Unable to read words from file: %1$s", path), e);
        }
      }

    protected <T> List<T> loadWordsFromResourcePath(String path, Charset charset, StringProcessor<T> processor) throws Exception {
        return loadWords(getResourceDirPath(path), charset, processor);
     }

     protected <T> List<T> loadWordsFromRulesPath(String path, Charset charset, StringProcessor<T> processor) throws Exception {
         return loadWords(getRulesDirPath(path), charset, processor);
     }

     public Map<String, String> createCoherencyMappingsFromRulesPath(String path) throws Exception {
         if (rulesDirPathExists(path)) {
             return createCoherencyMappings(getRulesDirPath(path), DEFAULT_CHARSET);
         }
         return null;
     }

     /**
      * Create a new set of coherency mappings from the path passed in.  This uses the coherencyWordStringProcessor to convert entries
      * in the file to each entry in the returned map.
      *
      * @param path The path to read from.
      * @param charset The charset to use, defaults to DEFAULT_CHARSET if not specified.
      * @return The mappings or null if the path doesn't exist.
      */
     public static Map<String, String> createCoherencyMappings(Path path, Charset charset) throws Exception {
         Objects.requireNonNull(path, "Path must be provided.");
         path = path.toRealPath();
         if (Files.notExists(path)) {
             return null;
         }
         charset = charset != null ? charset : DEFAULT_CHARSET;
         // GTODO Another sort of dodgy cast...
         List<Map<String, String>> lines = (List<Map<String, String>>) wordListCache.get(new WordsCacheKey(path, charset, coherencyWordStringProcessor));
         Map<String, String> data = new LinkedHashMap<>();

         // GTODO Fix this later, use stream processing?
         for (Map<String, String> d : lines) {
             data.putAll(d);
         }
         return data;
     }

     @Nullable
     public CompoundRuleData createCompoundRuleDataFromResourcePaths(String... paths) throws Exception {
         Objects.requireNonNull(paths, "At least one path must be provided.");

         Set<String> incorrectCompounds = new HashSet<>();
         Set<String> noDashSuggestion = new HashSet<>();
         Set<String> noDashLowerCaseSuggestion = new HashSet<>();
         Set<String> onlyDashSuggestion = new HashSet<>();

         for (int i = 0; i < paths.length; i++) {
             String path = paths[i];
             if (resourceDirPathExists(path)) {
                 Path p = getResourceDirPath(path);
                 // GTODO Get rid of compound rule data loader
                 try {
                     CompoundRuleDataLoader.loadCompoundRuleData(p, incorrectCompounds, noDashSuggestion, noDashLowerCaseSuggestion, onlyDashSuggestion, DEFAULT_CHARSET);
                 } catch(Exception e) {
                     throw new IOException(String.format("Unable to load compound rule data from: %1$s", p), e);
                 }
             }
         }
         return new CompoundRuleData(incorrectCompounds, noDashSuggestion, noDashLowerCaseSuggestion, onlyDashSuggestion);
     }

     // GTODO Convert to use getWordList.
     // GTODO Add static method for basic parsing.
     public List<PatternRule> loadCompoundPatternRulesFromResourcePath(String path, Charset charset, String message) throws Exception {
         Objects.requireNonNull(message, "Message must be provided.");
         charset = charset != null ? charset : DEFAULT_CHARSET;
         /*
         GTODO Finish this, counter is messing it up, maybe use Line class in getProcessed.
         return loadWords(getResourceDirPath(path), DEFAULT_CHARSET, new StringProcessor<PatternRule>() {
            @Override
            public boolean shouldSkip(String line) {
                return line.isEmpty() || line.charAt(0) == '#' || line.endsWith("+");
            }

            // No errors?

            @Override
            public PatternRule getProcessed(String line) {
                if (line.endsWith("*")) {
                  line = line.substring(0, line.length() - 1);
                }
                List<PatternToken> tokList = new ArrayList<>();
                String[] tokens = line.split("-");
                int tokenCounter = 0;
                for (String token : tokens) {
                  tokenCounter++;
                    // token
                  tokList.add(new PatternToken(token, true, false, false));
                  if (tokenCounter < tokens.length) {
                    // add dash
                    tokList.add(new PatternToken("[—–]", false, true, false));
                  }
                }
                PatternRule dashRule = new PatternRule
                    ("DASH_RULE" + counter, language, tokList,
                        "", msg + "<suggestion>"+line.replaceAll("[–—]", "-")+"</suggestion>.", line.replaceAll("[–—]", "-"));
                rules.add(dashRule);

            }
         })
         */
         List<PatternRule> rules = new ArrayList<>();
         Path p = getResourceDirPath(path);
         try (
             InputStream is = Files.newInputStream(p);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
         ) {
           String line;
           int counter = 0;
           while ((line = br.readLine()) != null) {
             counter++;
             if (line.isEmpty() || line.charAt(0) == '#') {
               continue;     // ignore comments
             }
             if (line.endsWith("+")) {
               continue; // skip non-hyphenated suggestions
             } else if (line.endsWith("*")) {
               line = line.substring(0, line.length() - 1);
             }
             List<PatternToken> tokList = new ArrayList<>();
             String[] tokens = line.split("-");
             int tokenCounter = 0;
             for (String token : tokens) {
               tokenCounter++;
                 // token
               tokList.add(new PatternToken(token, true, false, false));
               if (tokenCounter < tokens.length) {
                 // add dash
                 tokList.add(new PatternToken("[—–]", false, true, false));
               }
             }
             PatternRule dashRule = new PatternRule
                 ("DASH_RULE" + counter, language, tokList,
                     "", message + "<suggestion>"+line.replaceAll("[–—]", "-")+"</suggestion>.", line.replaceAll("[–—]", "-"));
             rules.add(dashRule);
           }
           return rules;
         } catch (IOException e) {
           throw new IOException(String.format("Unable to load compound pattern rules from: %1$s", p), e);
         }
     }

     public static List<AbstractPatternRule> getPatternRules(Path path, RuleFilterCreator ruleFilterCreator) throws Exception {
         return patternsCache.get(new PatternRuleCacheKey(path, ruleFilterCreator));
     }

     @Override @Nullable
     public Disambiguator getDisambiguator() throws Exception {
         // GTODO: Check this is appropriate for all subclasses of language... gl has it's own hybrid disambiguator...
         if (disambiguator == null) {
             String file = String.format(DISAMBIGUATION_FILE, language.getLocale().getLanguage());
             if (resourceDirPathExists(file)) {
                 Path path = getResourceDirPath(file);
                 try {
                     // GTODO Cache?
                     disambiguator = new XmlRuleDisambiguator(createDisambiguationPatternRules(path, getRuleFilterCreator()));
                 } catch(Exception e) {
                     throw new IOException(String.format("Unable to load disambiguation file: %1$s", path), e);
                 }
             }
         }
         return disambiguator;
     }

     @Override
     public LanguageProfile getLanguageProfile() throws Exception {
         // See if we have a .profile file, if so load from that, otherwise load from
         // LanguageProfileReader.
         String profileFile = String.format (LANGUAGE_PROFILE_FILE_NAME, language.getLocale().getLanguage());
         if (resourceDirPathExists(profileFile)) {
           Path path = getResourceDirPath(profileFile);
           try (InputStream profile = Files.newInputStream(path)) {
             return new LanguageProfileReader().read(profile);
            } catch(Exception e) {
                throw new IOException(String.format("Unable to read profile file: %1$s", path), e);
            }
        }
        /*
        GTODO Clean up
        try {
            Set<String> codes = new HashSet<>();
            codes.add(language.getShortCode());
           List<LanguageProfile> profs = new LanguageProfileReader().read(codes);
           if (profs != null && profs.size() > 0) {
               return profs.get(0);
           }
       } catch(Exception e) {
           // Not really an issue since the profile reader may not know about the language.
       }
       */
       return null;
     }

     /**
      * Get the bitext rules for the language, these are loaded from file: {@link BITEXT_RULES_FILE_NAME} in the rules dir with the language short code replaced to give the
      * real path.
      *
      * @return The bitext rules for the language.
      */
     @Override
     public List<BitextRule> getBitextRules() throws Exception {
         String name = String.format(BITEXT_RULES_FILE_NAME, language.getLocale().getLanguage());
         if (rulesDirPathExists(name)) {
             Path path = getRulesDirPath(name);
             try {
                 // GTODO Cache?
                 return new ArrayList<BitextRule>(createBittextPatternRules(path, getRuleFilterCreator()));
             } catch(Exception e) {
                throw new IOException(String.format("Unable to load bittext pattern rules from: %1$s", path), e);
             }
         }
         return new ArrayList<>();
     }

     public static final List<BitextPatternRule> createBittextPatternRules(Path path, RuleFilterCreator ruleFilterCreator) throws Exception {
         BitextPatternRuleHandler handler = new BitextPatternRuleHandler(ruleFilterCreator);
         return createPatternRules(path, handler);
     }

     @Override
     public List<FalseFriendPatternRule> getFalseFriendPatternRules(Language otherLanguage) throws Exception {
         Path path = getRulesDirPath(FALSE_FRIEND_RULES_FILE_NAME);
         try{
             // GTODO Cache?
             return createFalseFriendPatternRules(path, language, otherLanguage);
         } catch(Exception e) {
             throw new IOException(String.format("Unable to load false friend pattern rules from: %1$s", path), e);
         }
     }

     public static List<FalseFriendPatternRule> createFalseFriendPatternRules(Path path, Language textLanguage, Language motherTongue) throws Exception {
         Objects.requireNonNull(path, "Path to rules must be specified.");
         Objects.requireNonNull(textLanguage, "Text language must be specified.");
         Objects.requireNonNull(motherTongue, "Mother tongue language must be specified.");
         path = path.toRealPath();
         if (Files.notExists(path)) {
             throw new IllegalArgumentException(String.format("Path does not exist: %1$s", path));
         }
         FalseFriendRuleHandler handler = new FalseFriendRuleHandler(textLanguage, motherTongue);
         List<FalseFriendPatternRule> rules = createPatternRules(path, handler);
         // Add suggestions to each rule:
         ResourceBundle messages = motherTongue.getUseDataBroker().getMessageBundle();
         MessageFormat msgFormat = new MessageFormat(messages.getString("false_friend_suggestion"));
         for (AbstractPatternRule rule : rules) {
           List<String> suggestions = handler.getSuggestionMap().get(rule.getId());
           if (suggestions != null) {
             String[] msg = { suggestions.stream().map(o -> "<suggestion>" + o + "</suggestion>").collect(Collectors.joining(", ")) };
             rule.setMessage(rule.getMessage() + " " + msgFormat.format(msg));
           }
         }
         return rules;
     }

     public List<FalseFriendPatternRule> getFalseFriendPatternRulesFromResourcePath(String path, Language textLanguage, Language motherTongue) throws Exception {
         return createFalseFriendPatternRules(getResourceDirPath(path), textLanguage, motherTongue);
     }

     /**
      * Load a number of rules from the path, the handler is used to convert the input stream to the rules, a stream of xml data is expected.
      * If the path doesn't exist then null is returned.
      *
      * @param path The path to use to get the rule data.
      * @param handler The xml rule handler to use to conver the input stream into rules.
      * @return The built rules or null if the path doesn't exist.
      */
     public static final <T extends Rule> List<T> createPatternRules(Path path, XMLRuleHandler<T> handler) throws Exception {
         Objects.requireNonNull(path, "Path must be provided.");
         if (Files.notExists(path)) {
             return null;
         }
         path = path.toRealPath();
         if (Files.isDirectory(path)) {
             throw new IllegalArgumentException(String.format("Path: %1$s, is a directory, expected a file containing xml.", path));
         }
         Objects.requireNonNull(handler, "Handler must be provided.");
         try (InputStream is = Files.newInputStream(path)) {
           SAXParserFactory factory = SAXParserFactory.newInstance();
           SAXParser saxParser = factory.newSAXParser();
           Tools.setPasswordAuthenticator();
           saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
           saxParser.parse(is, handler);
           return handler.getRules();
         } catch(Exception e) {
           throw new IOException(String.format("Unable to load pattern rules from: %1$s", path), e);
         }
     }

     public static List<AbstractPatternRule> createPatternRules(Path path, RuleFilterCreator ruleFilterCreator, boolean relaxedMode) throws Exception {
         return createPatternRules(path, new PatternRuleHandler(ruleFilterCreator, relaxedMode));
     }

     public static List<DisambiguationPatternRule> createDisambiguationPatternRules(Path path, RuleFilterCreator ruleFilterCreator) throws Exception {
         DisambiguationRuleHandler handler = new DisambiguationRuleHandler(ruleFilterCreator);
         return createPatternRules(path, handler);
     }

     /**
      * Create a new multiword chunker, loads from file {@link MULTI_WORDS_FILE_NAME} with the short code for the associated
      * language replacing the %1$s value, the {@link DEFAULT_CHARSET} is used for reading the file.
      *
      * @return The new chunker inited from the multi words in file {@link MULTI_WORDS_FILE_NAME}.
      * @throws IOException If the file cannot be found in the classpath or cannot be read or if there is an issue with any of the words.
      */
     public MultiWordChunker createMultiWordChunkerFromResourcePath(boolean allowFirstCapitalized) throws IOException {
         return createMultiWordChunkerFromResourcePath(String.format(MULTI_WORDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, allowFirstCapitalized);
     }

     /**
      * Creates a multiword chunker 2, loads from file {@link MULTI_WORDS_FILE_NAME} with the short code for the associated
      * language replacing the %1$s value, the {@link DEFAULT_CHARSET} is used for reading the file.
      *
      * @return The new chunker inited from the multi words in file {@link MULTI_WORDS_FILE_NAME}.
      * @throws IOException If the file cannot be found in the classpath or cannot be read or if there is an issue with any of the words.
      */
     public MultiWordChunker2 createMultiWordChunker2FromResourcePath(boolean allowFirstCapitalized) throws IOException {
         return createMultiWordChunker2FromResourcePath(String.format(MULTI_WORDS_FILE_NAME, language.getLocale().getLanguage()), DEFAULT_CHARSET, allowFirstCapitalized);
     }

     public MultiWordChunker createMultiWordChunkerFromResourcePath(String path, Charset charset, boolean allowFirstCapitalized) throws IOException {
         try {
           List<String[]> posTokens = (List<String[]>) loadWordsFromResourcePath(path, charset, multiWordChunkerStringProcessor);
           Map<String, Integer> mStartSpace = new HashMap<>();
           Map<String, Integer> mStartNoSpace = new HashMap<>();
           Map<String, String> mFull = new HashMap<>();
           for (String[] tokenAndTag : posTokens) {
/*
             String[] tokenAndTag = posToken.line.split("\t");
             if (tokenAndTag.length != 2) {
               throw new IOException(String.format("Invalid format at line %1$s - expected two tab-separated parts", posToken));
             }
             */
             boolean containsSpace = tokenAndTag[0].indexOf(' ') > 0;
             String firstToken;
             String[] firstTokens;
             if (!containsSpace) {
               firstTokens = new String[tokenAndTag[0].length()];
               firstToken = tokenAndTag[0].substring(0, 1);
               for (int i = 1; i < tokenAndTag[0].length(); i++) {
                 firstTokens[i] = tokenAndTag[0].substring(i - 1, i);
               }
               if (mStartNoSpace.containsKey(firstToken)) {
                 if (mStartNoSpace.get(firstToken) < firstTokens.length) {
                   mStartNoSpace.put(firstToken, firstTokens.length);
                 }
               } else {
                 mStartNoSpace.put(firstToken, firstTokens.length);
               }
             } else {
               firstTokens = tokenAndTag[0].split(" ");
               firstToken = firstTokens[0];

               if (mStartSpace.containsKey(firstToken)) {
                 if (mStartSpace.get(firstToken) < firstTokens.length) {
                   mStartSpace.put(firstToken, firstTokens.length);
                 }
               } else {
                 mStartSpace.put(firstToken, firstTokens.length);
               }
             }
             mFull.put(tokenAndTag[0], tokenAndTag[1]);
           }
           return new MultiWordChunker(mStartSpace, mStartNoSpace, mFull, allowFirstCapitalized);
         } catch (Exception e) {
           throw new IOException(String.format("Unable to convert file: %1$s to a multi word chunker", path), e);
         }
     }

     /**
      * Get the sentence tokenizer, is just a wrapper call around {@link getDefaultSentenceTokenizer()}.
      *
      * @return The sentence tokenizer.
      */
     @Override
     public SentenceTokenizer getSentenceTokenizer() throws Exception {
         return createSimpleSentenceTokenizerFromResourcePath();
     }

     /**
      * Get the default srx sentence tokenizer.
      *
      * @return The default sentence tokenizer.
      */
     public SRXSentenceTokenizer getDefaultSentenceTokenizer() throws Exception {
         if (defaultSentenceTokenizer == null) {
             SrxDocument doc = getSrxDocumentFromResourcePath(DEFAULT_SEGMENT_SRX_FILE_NAME, null);
             if (doc == null) {
                 throw new IOException(String.format("Unable to load srx document from path: %1$s", getResourceDirPath(DEFAULT_SEGMENT_SRX_FILE_NAME)));
             }
            defaultSentenceTokenizer = new SimpleSentenceTokenizer(language, doc);
         }
         return defaultSentenceTokenizer;
     }

     public SimpleSentenceTokenizer createSimpleSentenceTokenizerFromResourcePath() throws Exception {
         return new SimpleSentenceTokenizer(language, getSrxDocumentFromResourcePath(SIMPLE_SEGMENT_SRX_FILE_NAME, DEFAULT_CHARSET));
     }

     /**
      * Get the synthesizer, always returns null, sub-classes should provide their own implementations.
      * @return null
      */
     @Override @Nullable
     public Synthesizer getSynthesizer() throws Exception {
        return null;
     }

     public Set<String> createSynthesizerWordTagsFromResourcePath(String file) throws Exception {
         if (resourceDirPathExists(file)) {
             return new LinkedHashSet<String>(getWordListFromResourcePath(file, synthesizerTagWordProcessor));
         }
         return null;
     }

     @Override
     public RuleFilterCreator getRuleFilterCreator() throws Exception {
         return ruleFilterCreator;
     }

     public static List<NeuralNetworkRule> createNeuralNetworkRules(ResourceBundle messages, Language language, Classifier classifier, List<ScoredConfusionSet> confusionSets) throws Exception {
         List<NeuralNetworkRule> rules = new ArrayList<>();
         if (confusionSets == null || confusionSets.size () == 0) {
             return rules;
         }

         for (ScoredConfusionSet s : confusionSets) {
             rules.add(new NeuralNetworkRule(messages, language, s, classifier));
         }
         return rules;
     }

     /**
      * Get the scored confusion sets used for neural network rules.  Loads from file {@link NEURAL_NETWORK_CONFUSION_SETS_FILE_NAME}.
      *
      * @return The scored confusion sets, or null if the resource file doesn't exist.
      */
     public List<ScoredConfusionSet> getNeuralNetworkScoredConfusionSetsFromResourcePath(Charset charset) throws Exception {
         String file = String.format(NEURAL_NETWORK_CONFUSION_SETS_FILE_NAME, language.getLocale().getLanguage());
         if (resourceDirPathExists(file)) {
             return loadScoredConfusionSetFromResourcePath(file, charset != null ? charset : DEFAULT_CHARSET);
         }
         return null;
     }

     /**
      * Creates a neural network rule Classifier.  It creates a SingleLayerClassifier is only the following files are available from the resource path:
      *    {@link NEURAL_NETWORK_CLASSIFIER_W_FC1_FILE_NAME}
      *    {@link NEURAL_NETWORK_CLASSIFIER_B_FC1_FILE_NAME}
      *
      * It creates a TwoLayerClassifier if the above files AND the files below are available from the resource path:
      *    {@link NEURAL_NETWORK_CLASSIFIER_W_FC2_FILE_NAME}
      *    {@link NEURAL_NETWORK_CLASSIFIER_B_FC2_FILE_NAME}
      *
      * If any of the files are missing then it returns null.
      *
      * @param model The model to use for creating the classifier.
      * @return A single or two layer classifier depending upon what resource files are available.
      */
     public Classifier createNeuralNetworkRuleClassifierFromResourceDir(Word2VecModel model) throws Exception {
         String l = language.getLocale().getLanguage();
         String wfc1File = String.format(NEURAL_NETWORK_CLASSIFIER_W_FC1_FILE_NAME, l);
         String bfc1File = String.format(NEURAL_NETWORK_CLASSIFIER_B_FC1_FILE_NAME, l);
         if (resourceDirPathExists(wfc1File) && resourceDirPathExists(bfc1File)) {
             Path w1p = getResourceDirPath(wfc1File);
             Path b1p = getResourceDirPath(bfc1File);

             String wfc2File = String.format(NEURAL_NETWORK_CLASSIFIER_W_FC2_FILE_NAME, l);
             String bfc2File = String.format(NEURAL_NETWORK_CLASSIFIER_B_FC2_FILE_NAME, l);

             if (resourceDirPathExists(wfc2File) && resourceDirPathExists(bfc2File)) {
                 Path w2p = getResourceDirPath(wfc2File);
                 Path b2p = getResourceDirPath(bfc2File);
                 try (InputStream w1 = Files.newInputStream(w1p);
                      InputStream b1 = Files.newInputStream(b1p);
                      InputStream w2 = Files.newInputStream(w2p);
                      InputStream b2 = Files.newInputStream(b2p);
                 ) {
                     return new TwoLayerClassifier(model.getEmbedding(), w1, b1, w2, b2);
                 } catch(Exception e) {
                     throw new IOException(String.format("Unable to create new two layer classifier from: %1$s, %2$s, %3$s, %4$s", w1p, b1p, w2p, b2p), e);
                 }
             }

             try (InputStream w1 = Files.newInputStream(w1p);
                  InputStream b1 = Files.newInputStream(b1p);
             ) {
                 return new SingleLayerClassifier(model.getEmbedding(), w1, b1);
             } catch(Exception e) {
                 throw new IOException(String.format("Unable to create single two layer classifier from: %1$s, %2$s", w1p, b1p), e);
             }
         }
         return null;
     }

     @Nullable
     public SrxDocument getSrxDocumentFromResourcePath(String path, Charset charset) throws Exception {
         if (!resourceDirPathExists(path)) {
             return null;
         }
         return createSrxDocument(getResourceDirPath(path), charset);
     }

     public static SrxDocument createSrxDocument(Path path, Charset charset) throws IOException {
         Objects.requireNonNull(path, "Path must be provided.");
         path = path.toRealPath();
         if (charset == null) {
             charset = DEFAULT_CHARSET;
         }
        try (
           InputStream inputStream = Files.newInputStream(path);
           BufferedReader srxReader = new BufferedReader(new InputStreamReader(inputStream, charset));
        ) {
           Map<String, Object> parserParameters = new HashMap<>();
           parserParameters.put(Srx2SaxParser.VALIDATE_PARAMETER, true);
           SrxParser srxParser = new Srx2SaxParser(parserParameters);
           return srxParser.parse(srxReader);
        } catch (IOException e) {
           throw new IOException(String.format("Could not load SRX rules from: %1$s", path), e);
        }
     }

     // GTODO Use a string processro
     public List<ScoredConfusionSet> loadScoredConfusionSetFromResourcePath(String path, Charset charset) throws Exception {
       charset = charset != null ? charset : DEFAULT_CHARSET;
       Path p = getResourceDirPath(path);
       List<ScoredConfusionSet> list = new ArrayList<>();
       try (
         InputStream inputStream = Files.newInputStream(p);
         BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset));
       ) {
         String line;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("#") || line.trim().isEmpty()) {
             continue;
           }

           String[] parts = line.replaceFirst("\\s*#.*", "").split(";\\s*");
           if (parts.length != 3) {
             throw new IllegalArgumentException("Unexpected format: '" + line + "' - expected three semicolon-separated values: word1; word2; factor");
           }

           List<ConfusionString> confusionStrings = new ArrayList<>();
           Set<String> loadedForSet = new HashSet<>();
           for (String part : Arrays.asList(parts).subList(0, parts.length-1)) {
             String[] subParts = part.split("\\|");
             String word = subParts[0];
             String description = subParts.length == 2 ? subParts[1] : null;
             if (loadedForSet.contains(word)) {
               throw new IllegalArgumentException("Word appears twice in same confusion set: '" + word + "'");
             }
             confusionStrings.add(new ConfusionString(word, description));
             loadedForSet.add(word);
           }
           list.add(new ScoredConfusionSet(Float.parseFloat(parts[parts.length - 1]), confusionStrings));
         }
     } catch(Exception e) {
         throw new IOException(String.format("Unable to load scored confusion sets from: %1$s", p), e);
     }
       return list;
     }

     /**
      * Load a set of confusion sets from a path.
      *
      */
      //GTODO Use a StringProcessor
     @Nullable
     public static Map<String, List<ConfusionSet>> createConfusionSet(Path path, Charset charset) throws IOException {
         Objects.requireNonNull(path, "Path must be provided.");
         path = path.toRealPath();
         if (Files.notExists(path)) {
             return null;
         }
         charset = charset != null ? charset : DEFAULT_CHARSET;
       try (
            InputStream is = Files.newInputStream(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
        ) {
         Map<String, List<ConfusionSet>> map = new HashMap<String, List<ConfusionSet>>();
         String line;
         int lineNo = -1;
         while ((line = reader.readLine()) != null) {
             lineNo++;
           if (line.startsWith("#") || line.trim().isEmpty()) {
             continue;
           }
           // GTODO: Change to use word list?
           String[] parts = line.replaceFirst("\\s*#.*", "").split(";\\s*");
           if (parts.length != 3) {
             throw new IOException(String.format("Unexpected format for line(%1$s): %2$s - expected three semicolon-separated values: word1; word2; factor", lineNo, line));
           }
           List<ConfusionString> confusionStrings = new ArrayList<ConfusionString>();
           Set<String> loadedForSet = new HashSet<String>();
           String prevWord = null;
           for (String part : Arrays.asList(parts).subList(0, parts.length - 1)) {
             String[] subParts = part.split("\\|");
             String word = subParts[0];
             if (prevWord != null && word.compareTo(prevWord) < 0) {
               // Quick hack for reordering lines
               //System.err.println("Delete: " + line);
               //String comment = line.substring(line.indexOf("#"));
               //String newLine = parts[1] + "; " + parts[0] + "; " + parts[2] + "; " + comment;
               //System.err.println("Add: " + newLine);

               // GTODO: Should order words?

               throw new IOException(String.format("Expected words to be ordered alphabetically in line(%1$s): %2$s - expected %3$s before %4$s", lineNo, line, prevWord, word));
             }
             prevWord = word;
             String description = subParts.length == 2 ? subParts[1] : null;
             if (loadedForSet.contains(word)) {
               throw new IOException(String.format("Duplicate word found in line(%1$s): %2$s - duplicate is %3$s", lineNo, line, word));
             }
             confusionStrings.add(new ConfusionString(word, description));
             loadedForSet.add(word);
           }
           ConfusionSet confusionSet = new ConfusionSet(Long.parseLong(parts[parts.length-1]), confusionStrings);
           for (ConfusionString confusionString : confusionStrings) {
             String key = confusionString.getString();
             List<ConfusionSet> existingEntry = map.get(key);
             if (existingEntry != null) {
               existingEntry.add(confusionSet);
             } else {
               List<ConfusionSet> sets = new ArrayList<ConfusionSet>();
               sets.add(confusionSet);
               map.put(key, sets);
             }
           }
         }
         return map;
       } catch(Exception e) {
           throw new IOException (String.format("Unable to load confusion sets from resource file: %1$s", path), e);
       }
     }

     /**
      * Close any resources we have open.
      */
     public void close() throws Exception {
         // GTODO Maybe clear the caches?
     }

     static class MorfologikTextDictCacheKey extends MultiPathsCacheKey {
         Path infoPath;
         Charset charset;

         MorfologikTextDictCacheKey(List<Path> textFilePaths, Path infoPath, Charset charset) {
             super(textFilePaths);
             this.infoPath = infoPath;
             this.charset = charset;
         }

         @Override
         protected Object[] get() {
             List<Object> objs = new ArrayList<>(Arrays.asList(super.get()));
             //objs.add(broker.language.getLocale());
             objs.add(infoPath);
             objs.add(charset);
             return objs.toArray();
         }

     }

     static class PatternRuleCacheKey extends CacheKey {
         Path path;
         RuleFilterCreator ruleFilterCreator;

         PatternRuleCacheKey(Path path, RuleFilterCreator r) {
             this.path = path;
             this.ruleFilterCreator = r;
         }

         @Override
         protected Object[] get() {
             List<Object> objs = new ArrayList<>(Arrays.asList(super.get()));
             objs.add(path);
             objs.add(ruleFilterCreator);
             return objs.toArray();
         }

     }

     static class MultiPathsCacheKey extends CacheKey {
         List<Path> paths;

         MultiPathsCacheKey(List<Path> paths) {
             this.paths = paths;
         }

         @Override
         protected Object[] get() {
             List<Object> objs = new ArrayList<>(Arrays.asList(super.get()));
             //objs.add(broker.language.getLocale());
             objs.addAll(paths);
             return objs.toArray();
         }

     }

     static class PathCacheKey extends CacheKey {
         Path path;
         Charset charset;

         PathCacheKey(Path path) {
             this.path = path;
         }

         PathCacheKey(Path path, Charset charset) {
             this.path = path;
             this.charset = charset;
         }

         @Override
         protected Object[] get() {
             List<Object> objs = new ArrayList<>(Arrays.asList(super.get()));
             //objs.add(broker.language.getLocale());
             objs.add(path);
             objs.add(charset);
             return objs.toArray();
         }

     }

     static class WordsCacheKey extends PathCacheKey {
         StringProcessor processor;

         WordsCacheKey(Path path, Charset charset, StringProcessor processor) {
             super(path, charset);
             this.processor = processor;
         }

         @Override
         protected Object[] get() {
             List<Object> objs = new ArrayList<>(Arrays.asList(super.get()));
             //objs.add(broker.language.getLocale());
             objs.add(processor);
             return objs.toArray();
         }

     }

     static abstract class CacheKey {

         protected Object[] get() {
             List<Object> objs = new ArrayList<>();
             return objs.toArray();
         }

         @Override
         public int hashCode() {
             return Objects.hash(get());
         }

         @Override
         public boolean equals (Object o) {
             if (o instanceof CacheKey) {
                 CacheKey p = (CacheKey) o;
                 return Arrays.equals(get(), p.get());
             }
             return false;
         }
     }

}
