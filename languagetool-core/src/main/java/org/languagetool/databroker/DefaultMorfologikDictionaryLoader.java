/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import morfologik.fsa.FSA;
import morfologik.fsa.builders.FSABuilder;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.stemming.Dictionary;

public class DefaultMorfologikDictionaryLoader {
    public static Dictionary loadFromURL(URL url) throws IOException {
        return Dictionary.read(url);
    }

    /**
     * Read the set of input streams as plain text and convert each line (as specified by BufferedReader.readLine) to a byte array, append all lines
     * and return the list.
     *
     */
    public List<byte[]> getAsLines(Charset charset, InputStream... streams) throws IOException {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        List<byte[]> lines = new ArrayList<>();
        for (InputStream ins : streams) {
            appendLines(ins, charset, lines);
        }
        return lines;
    }

    public static Dictionary loadFromLines(List<byte[]> lines, InputStream dictInfoStream) throws IOException {
        // Creating the dictionary at runtime can easily take 50ms for spelling.txt files
        // that are ~50KB. We don't want that overhead for every check of a short sentence,
        // so we cache the result:
        List<byte[]> linesCopy = new ArrayList<>(lines);
        Collections.sort(linesCopy, FSABuilder.LEXICAL_ORDERING);
        FSA fsa = FSABuilder.build(linesCopy);
        ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
        ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
        Dictionary dict = Dictionary.read(fsaInStream, dictInfoStream);
        return dict;
    }

    public static void appendLines(InputStream ins, Charset charset, List<byte[]> lines) throws IOException {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(ins, charset));
      String line;
      while ((line = br.readLine()) != null) {
        if (!line.startsWith("#")) {
          lines.add(line.replaceFirst("#.*", "").trim().getBytes(charset));
        }
      }
    }

    private static List<byte[]> getLines(BufferedReader br, Charset charset) throws IOException {
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }
      List<byte[]> lines = new ArrayList<>();
      String line;
      while ((line = br.readLine()) != null) {
        if (!line.startsWith("#")) {
          lines.add(line.replaceFirst("#.*", "").trim().getBytes("utf-8"));
        }
      }
      return lines;
    }

}
