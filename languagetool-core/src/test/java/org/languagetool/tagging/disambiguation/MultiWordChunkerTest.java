package org.languagetool.tagging.disambiguation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.nio.charset.*;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.TestLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.databroker.DefaultResourceDataBroker;

public class MultiWordChunkerTest {

  private TestLanguage lang;

  private JLanguageTool lt;

  @Before
  public void setUp() throws Exception {
      lang = new TestLanguage() {
        public org.languagetool.tagging.Tagger getTagger() {
          return new DemoTagger() {
            public java.util.List<AnalyzedTokenReadings> tag(java.util.List<String> sentenceTokens) {
              List<AnalyzedTokenReadings> tokenReadings = super.tag(sentenceTokens);
              for (AnalyzedTokenReadings readings : tokenReadings) {
                if( readings.isWhitespace() )
                  continue;

                readings.addReading(new AnalyzedToken(readings.getToken(), "FakePosTag", readings.getToken()));
              }
              return tokenReadings;
            }
          };
        }
      };
      lt = new JLanguageTool(lang);
  }

  @Test
  public void testDisambiguate1() throws Exception {
    MultiWordChunker multiWordChunker = lang.getUseDataBroker().createMultiWordChunkerFromResourcePath(true);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();

    assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    assertFalse(tokens[3].getReadings().toString().contains("adv"));
    assertTrue(tokens[5].getReadings().toString().contains("</adv>"));

    assertTrue(tokens[1].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[3].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testDisambiguate2() throws Exception {
    MultiWordChunker2 multiWordChunker = lang.getUseDataBroker().createMultiWordChunker2FromResourcePath(true);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("Ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();

    assertTrue(tokens[1].getReadings().toString().contains("<adv>"));
    assertTrue(tokens[3].getReadings().toString().contains("<adv>"));
    assertTrue(tokens[5].getReadings().toString().contains("<adv>"));

    assertTrue(tokens[1].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[3].getReadings().toString().contains("FakePosTag"));
    assertTrue(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

  @Test
  public void testDisambiguate2NoMatch() throws Exception {
    MultiWordChunker2 multiWordChunker = lang.getUseDataBroker().createMultiWordChunker2FromResourcePath(true);
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ahh for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();

    assertFalse(tokens[1].getReadings().toString().contains("<adv>"));
  }

  @Test
  public void testDisambiguate2RemoveOtherReadings() throws Exception {
    MultiWordChunker2 multiWordChunker = lang.getUseDataBroker().createMultiWordChunker2FromResourcePath(true);
    multiWordChunker.setRemoveOtherReadings(true);
    multiWordChunker.setWrapTag(false);

    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence("ah for shame");
    AnalyzedSentence disambiguated = multiWordChunker.disambiguate(analyzedSentence);
    AnalyzedTokenReadings[] tokens = disambiguated.getTokens();
    assertTrue(tokens[1].getReadings().toString().contains("adv"));
    assertTrue(tokens[3].getReadings().toString().contains("adv"));
    assertTrue(tokens[5].getReadings().toString().contains("adv"));

    assertFalse(tokens[1].getReadings().toString().contains("FakePosTag"));
    assertFalse(tokens[3].getReadings().toString().contains("FakePosTag"));
    assertFalse(tokens[5].getReadings().toString().contains("FakePosTag"));
  }

}
