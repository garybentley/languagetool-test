This branching of LanguageTool (based off of SNAPSHOT 4.3) aims to remove a number of the assumptions that curently underpin LanguageTool.

Some of these assumptions are:

1. All data for configuring/executing rules will be found on the classpath.

2. All languages that will be used for the lifetime of the JVM instance will be available (on the classpath).

3. That rules configure themselves from classpath available information.

4. That rules are designed to be executed as a group via JLanguageTool.

5. We are operating in a fixed environment (such as a server).

What changes does this project make to remove these assumptions?

## Loading languages at runtime (not on classpath)

A [LanguageProvider](language-core/src/main/java/org/languagetool/LanguageProvider.java) and a default classpath implementation
[DefaultResourceLanguageProvider](languagetool-core/src/main/java/org/languagetool/DefaultResourceLanguageProvider.java).

DefaultResourceLanguageProvider.loadLanguages(URL) can be used to load a language or set of languages from a non-classpath source.

[Languages](languagetool-core/src/main/java/org/languagetool/Languages.java).setLanguageProvider(LanguageProvider) can be used to
set the language provider to use for loading languages.  Just as with standard LanguageTool classpath detection of languages is still
present and DefaultResourceLanguageProvider.loadLanguages(URL) will use the same mechanism to find and load languages.

## Loading resources

The resources a rule needs for configuration has been decoupled from the classes used to represent the rule.  

The LanguageTool concept of a ResourceDataBroker has been extended to allow the user to provide a ResourceDataBroker that will be used
by the language to configure the rules it supports.  If no ResourceDataBroker is provided by the user then the language will create its
own default broker that can be used.  These default brokers expect information to be available and loaded from the classpath (the
same as the current LanguageTool).

For example the following code will load resources from the classpath:
```java
BritishEnglish en = new BritishEnglish();
JLanguageTool lt = new JLanguageTool(en);
List<Rule> rules = en.getRelevantRules();
```
In this case a [DefaultEnglishResourceDataBroker](languagetool-language-modules/en/src/main/java/org/languagetool/databroker/DefaultEnglishResourceDataBroker.java) will be created by the English class.  The default English broker implements interface [EnglishResourceDataBroker](languagetool-language-modules/en/src/main/java/org/languagetool/databroker/EnglishResourceDataBroker.java) which
defines what information is needed by English to construct the rules English (and it's variants) supports.

However it is possible to override this behavior, for example:
```java
BritishEnglish en = new BritishEnglish();
en.setDataBroker(new MyEnglishDataBroker());
```
in this case requests for data will be sent to MyEnglishDataBroker.

## Decoupling rules from Languages

Nearly all rules have been decoupled from the Language (and subclasses) object.  This is to:

    1. Allow clearer configuration of the rule, i.e. what data is needed to make it work.
    2. To make rules more easily reusable.
    3. To decouple them from the configuration data.

The rules have also been changed to have the data they require passed in to the constructor.

An example is:

    en/CompoundRule.java

in LanguageTool the CompoundRuleData the rule uses is loaded as a static variable, in this project the rule constructor is:

    public CompoundRule(ResourceBundle, CompoundRuleData)

the data the rule uses is passed in and loaded elsewhere.  The EnglishResourceDataBroker specifies a method getCompounds that can
be used to get the English compound rule data.  DefaultEnglishResourceDataBroker implements this method and will load from the classpath.

## Languages defer to the data broker

Each of the currently changes languages defers to its data broker for resources and relevant helper objects.  It is the responsiblity
of the data broker implementation to return the correct object.  Where appropriate the current resource data broker interfaces narrow the
type that the data broker should return.  For example the German data broker interface requires a GermanTagger to be returned.

## Languages now support creating individual rules

Each of the currently changed languages introduces a number of new methods that allow users to create each rule as needed.  The language
then queries its data broker to retrieve the information required by the rule.

For example English now has method:
```java
public AvsAnRule createAvsAnRule(ResourceBundle messages) throws Exception {
    return new AvsAnRule(getUseMessages(messages), getUseDataBroker().getRequiresAWords(), getUseDataBroker().getRequiresANWords());
}
```
which as can be seen queries the data broker for the lists of A and AN words that should be used by the rule.

The getRelevantRules method now calls out to createAvsAnRule to create the rule.

## Languages use Locales

Languages now use a Locale object to define their language, country and variant.  Short code and related data have been removed.  New
method isVariant has been added to allow users to determine whether their Language object is a variant.

## DefaultResourceDataBroker

Note: Section WIP

   * Uses a PathProvider
   * Has a default classpath provider

## Currently completed Languages

de, en, fa and fr, it have been modified and tested.

Note: ca has been removed (via pom.xml) due to a number of missing files that prevent construction of Catalan and its rules.

## Future work

Note: Section WIP

    * Rule ID and rule config
    * Decouple the resource brokers from file names, have a json file that defines resources and where to find them

## TODO:

This is a list of things that need to be changed/looked at:

* Check change getDefaultLanguageVariant to return a Locale.
* Check en/CompoundRule - uses AmericanEnglish.
* Check en/CompoundRule - uses IOException
* en/CompoundRule - move anitDismabiguationPatterns to be passed in - they are static
* classes that extends BaseSynthesizer -> remove throws Exception from synthesize methods
* rename org.languagetool.rules.pl.DashRule to PolishDashRule to prevent confusion with org.language.rules.DashRule and following other language naming conventions, i.e. EnglishDashRule.
* org.langaugetool.rules.pl.DashRule should use values from ResourceBundle rather than hard code Polish text.
* consider renaming language specific CompoundRule classes to <language>CompoundRule for clarity (not a big issue since no core CompoundRule).
* change English file names to use %1$s for language rather than hard coded en.
* move resource locations to a json/xml configuration file rather than hard coded in data broker classes.
* en/CompoundRule pass in the anti pattern rules, remove use of "AmericanEnglish".
* core/AbstractCompoundRule pass in CompoundRuleData to constructor, remove "getCompoundRuleData" method.
* Move CaseConverter to .rules instead of .rules.patterns (also move DefaultCaseConverter)
* core/MultipleWhitespaceRule consider using CaseConverter for isNonBreakingWhitespace call.
* core/UppercaseSentenceStartRule remove dutch special case, handle with Dutch specific rule.
* pl/test/PolishDisambiguationRuleTest make use of polish hyrbird disambiguator not a multi word chunker.
* modify grammar.xml to allow specification of default enabled/disabled rules.  At present they are hard coded in the language (see ValencianCatalan)
* CatalanTagger can't be created since org/languagetool/resource/ca/ca-ES-valencia.dict doesn't exist.
* CatalanSynthesizer can't be created since org/languagetool/resource/ca/[ca-ES-valencia_synth.dict, ca-ES-valencia_tags.txt] don't exist.  Synthesizer also refers to ValenciaCatalan rather than Catalan, should probably be changed.
* CatalanWordTokenizer can't be created since org/languagetool/resource/ca/ca-ES-valencia.dict doesn't exist.  Also Tokenizer refers to ValenciaCatalan rather than Catalan (default variant is standard Catalan not Valencia Catalan).
* Catalan has NOT been ported because of these serious issues.  It is removed from the pom.xml file.
* it/MorfologikItalianSpellerRule doesn't set the category or add an example pair like other languages.
* it/ItalianRuleDisambiguator remove class, is only a wrapper around XmlRuleDisambiguator and not needed.
* it needs more tests.
* consider creating a HybridDisambiguator, polish and portuguese both use a hybrid with the same semantics, i.e. a multiwordchunker+xmlruledisambiguator.  Could have a HyrbridDisambiguator(Disambiguator...) that then chains the disambiguate method.
* pt consider passing an IStemmer to the PortugueseTagger, it's creating one for every tag call.
* Maybe change ResourceDataBroker.getWordTokenizer to return a Tokenizer rather than WordTokenizer.
* pt/PortugueseTagger is creating duplicate DictionaryLookups within it's own call stack.  Remove the duplication.
* inconsistent rules created in getRelevantRules, for instance some languages return a EmptyLineRule some don't, why?
* pt/PortugueseUnitConversionRule uses a Locale.GERMAN for the number format.
* Rules like AbstractCompoundRule could be created from a config file, see pl/CompoundRule, de/CompoundRule for examples.
* PortugalPortuguese/BrazilianPortuguese getRelevantRules contains duplicate rule PostReformPortugueseCompoundRule addition.
* en/EnglishDashRule doesn't set a category.
* extensions of AbstractDashRule apply conflicting categories, de it is COMPOUNDING, pt it is TYPOGRAPHICAL, en doesn't set it, pl doesn't set it, ru doesn't set it.
* en/DefaultEnglishResourceDataBroker cache spelling ignore/prohibit words.
* core/UppercaseSentenceStartRule remove dutch special case.  Remove Language.
* rt/Russian RussianDashRule has been removed from the getRelevantRules call since it causes an exhaustion of heap space error and no amount of heap allocation seems to sate it.  There are 26k compound pattern rules however.  This is probably something I'm doing wrong, if anyone can tell me what it is please let me know.  An alternative approach would be to create PatternRules on a demand basis, i.e. iterate through the file and create the rules one at a time rather than creating them all at once.
* ast/rules dir is a subdir of language, doesn't match other languages.
* ast/ast.profile file is never referenced?
* ast/test there are two rules dirs, org/languagetool/rules, org/languagetool/languages/rules
* be has no speller test.
* da has very few tests.
* pl why doesn't polish have a double punctuation rule?
* Create a hybrid disambiguator that removes needs for per language hybrid disambiguator.
* gl/GalicianSynthesizer remove file, not needed.
* gl/GalicianTagger (also pt/PortugueseTagger and maybe others) consider passing in IStemmer instead of creating one for each tag call.
* el/Greek doesn't pass userConfig to morfo speller rule.
* el virtually no tests, no speller check.
* lt/speller rule, there is no hunspell subdir or lt_LT.dict file.  Speller rule removed.
* a number of languages use GenericUnpairedBracketsRule but don't specify an id.
* ml is supposed to have been removed in version 3.6
* ro contains a resource file "coduri.html" which is 700KB but has no instructions or information on what it is.
* sk file grammar-nezaradene.xml is present in the rules dir but no reference is made to it.
* Investigate BaseTagger does it need to be abstract?
* sr/MorfologikEkavianSpellerRule has an example but MorfologikJekavianSpellerRule doesn't.
* sr probably need a EkavianSerbian to provide symmetry with class JekavianSerbian.
* ta breaks the directory naming convention.
* LongSentenceRule (not subclass) will use the same user config parm (via rule id) for the sentence length, however each language should have its own length.
* uk/UkranianTagger uses /uk/ukranian.dict but that file doesn't exist.
* DefaultCaseConverter needs work to remove use of StringTools.
* UkrainianSynthesizer uses /uk/ukrainian_synth.dict and /uk/ukrainian_tags.txt but the files don't exist.
* MorfologikUkrainianSpellerRule uses /uk/hunspell/uk_UA.dict which doesn't exist.
* uk nearly all tests have been @Ignored due to missing .dict files, especially that of tagger.
* server needs to be changed to better handle the language model and word2vec model dirs.  For now those features have been disabled.
* ca/ReflexiveVerbsRule should have configuration files, espcially for verbsPotencialmentPronominals.
* commandline - some tests aren't working but I'm getting very tired of this now!

## The information below is from the standard LanguageTool README

LanguageTool is an Open Source proofreading software for English, French, German,
Polish, Russian, and [more than 20 other languages](https://languagetool.org/languages/).
It finds many errors that a simple spell checker cannot detect.

LanguageTool is freely available under the LGPL 2.1 or later.

For more information, please see our homepage at https://languagetool.org,
[this README](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/README.md),
and [CHANGES](https://github.com/languagetool-org/languagetool/blob/master/languagetool-standalone/CHANGES.md).

#### Contributions

[The development overview](http://wiki.languagetool.org/development-overview) describes
how you can contribute error detection rules.

See ['easy fix' issues](https://github.com/languagetool-org/languagetool/issues?q=is%3Aopen+is%3Aissue+label%3A%22easy+fix%22)
for issues to get started.

For more technical details, see [our wiki](http://wiki.languagetool.org).

#### Scripted installation and building
To install or build using a script, simply type:
```
curl -L https://raw.githubusercontent.com/languagetool-org/languagetool/master/install.sh | sudo bash <options>
sudo bash install.sh <options>
```

```
Usage: install.sh <option> <package>
Options:
   -h --help                   Show help
   -b --build                  Builds packages from the bleeding edge development copy of LanguageTool
   -c --command <command>      Specifies post-installation command to run (default gui when screen is detected)
   -q --quiet                  Shut up LanguageTool installer! Only tell me important stuff!
   -t --text <file>            Specifies what text to be spellchecked by LanguageTool command line (default spellcheck.txt)
   -d --depth <value>          Specifies the depth to clone when building LanguageTool yourself (default 1).
   -p --package <package>      Specifies package to install when building (default all)
   -o --override <OS>          Override automatic OS detection with <OS>
   -a --accept                 Accept the oracle license at http://java.com/license. Only run this if you have seen the license and agree to its terms!
   -r --remove <all/partial>   Removes LanguageTool install. <all> uninstalls the dependencies that were auto-installed. (default partial)

Packages(only if -b is specified):
   standalone                  Installs standalone package
   wikipedia                   Installs Wikipedia package
   office-extension            Installs the LibreOffice/OpenOffice extension package

Commands:
   GUI                         Runs GUI version of LanguageTool
   commandline                 Runs command line version of LanguageTool
   server                      Runs server version of LanguageTool
```

#### Alternate way to build from source

Before start: you will need to clone from GitHub and install Java 8 and Apache Maven.

Warning: a complete clone requires downloading more than 360 MB and needs more than 500 MB on disk.
This can be reduced if you only need the last few revisions of the master branch
by creating a shallow clone:

    git clone --depth 5 https://github.com/languagetool-org/languagetool.git

A shallow clone downloads less than 60 MB and needs less than 200 MB on disk.

In the root project folder, run:

    mvn clean test

(sometimes you can skip Maven step for repeated builds)

    ./build.sh languagetool-standalone package -DskipTests

Test the result in `languagetool-standalone/target/`.

    ./build.sh languagetool-wikipedia package -DskipTests

Test the result in `languagetool-wikipedia/target`.

    ./build.sh languagetool-office-extension package -DskipTests

Test the result in `languagetool-office-extension/target`, rename the `*.zip` to `*.oxt` to install it in LibreOffice/OpenOffice.

Now you can use the bleeding edge development copy of LanguageTool `*.jar` files, be aware that it might contain regressions.

#### License

Unless otherwise noted, this software is distributed under the LGPL, see file [COPYING.txt](https://github.com/languagetool-org/languagetool/blob/master/COPYING.txt).
