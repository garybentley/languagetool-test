package org.languagetool.tagging;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestLanguage;
import org.languagetool.TestTools;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CombiningTaggerTest {

  @Test
  public void testTagNoOverwrite() throws Exception {
    // Don't include removal, don't overwrite.
    CombiningTagger tagger = TestTools.getTestLanguage().getUseDataBroker().getWordTagger(false, false);
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    assertThat(result.size(), is(2));
    String asString = getAsString(result);
    assertTrue(asString.contains("baseform1/POSTAG1"));
    assertTrue(asString.contains("baseform2/POSTAG2"));
  }

  @Test
  public void testTagOverwrite() throws Exception {
    // Don't include removal, overwrite.
    CombiningTagger tagger = TestTools.getTestLanguage().getUseDataBroker().getWordTagger(false, true);
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    assertThat(result.size(), is(1));
    String asString = getAsString(result);
    assertTrue(asString.contains("baseform2/POSTAG2"));
  }

  @Test
  public void testTagRemoval() throws Exception {
      // Include removal, don't overwrite.
    CombiningTagger tagger = TestTools.getTestLanguage().getUseDataBroker().getWordTagger(true, false);
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    String asString = getAsString(result);
    assertFalse(asString.contains("baseform1/POSTAG1"));  // first tagged, but in removed.txt
    assertTrue(asString.contains("baseform2/POSTAG2"));
  }
/*
GTODO Cleanup
  private CombiningTagger getCombiningTagger(boolean overwrite, boolean includeRemovalTagger) throws Exception {
      // GTODO Need a better way of getting this, it's too low level.
    TestLanguage demo = TestTools.getTestLanguage();
    ManualTagger tagger1 = new ManualTagger(demo.getUseDataBroker().getFromResourceDirAsStream(String.format("/%1$s/added1.txt", demo.getShortCode())));
    ManualTagger tagger2 = new ManualTagger(demo.getUseDataBroker().getFromResourceDirAsStream(String.format("/%1$s/added2.txt", demo.getShortCode())));
    ManualTagger removalTagger = null;
    if (includeRemovalTagger) {
      removalTagger = new ManualTagger(demo.getUseDataBroker().getFromResourceDirAsStream(String.format("/%1$s/removed.txt", demo.getShortCode())));
    }
    return new CombiningTagger(tagger1, tagger2, removalTagger, overwrite);
  }
*/
  private String getAsString(List<TaggedWord> result) {
    StringBuilder sb = new StringBuilder();
    for (TaggedWord taggedWord : result) {
      sb.append(taggedWord.getLemma());
      sb.append("/");
      sb.append(taggedWord.getPosTag());
      sb.append("\n");
    }
    return sb.toString();
  }
/*
GTODO Not needed
  @Test(expected = Exception.class)
  public void testInvalidFile() throws Exception {
      TestLanguage demo = TestTools.getTestLanguage();
      new ManualTagger(demo.getUseDataBroker().getFromResourceDirAsStream(String.format("/%1$s/added-invalid.txt", demo.getShortCode())));
    //GTODO: new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added-invalid.txt"));
  }
*/
}
