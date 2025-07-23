/*
 * Copyright (C) 2025 IDRsolutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.idrsolutions.examples;

import me.bush.translator.Language;
import me.bush.translator.Translation;
import me.bush.translator.Translator;
import org.jpedal.PdfDecoderServer;
import org.jpedal.annotation.Annotation;
import org.jpedal.annotation.FreeText;
import org.jpedal.constants.BaseFont;
import org.jpedal.constants.Quadding;
import org.jpedal.exception.PdfException;
import org.jpedal.grouping.PdfGroupingAlgorithms;
import org.jpedal.manipulator.PdfManipulator;
import org.jpedal.text.TextLines;
import org.jpedal.utils.Strip;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Translate {

    public static void main(String[] args) {
        final String inputFile = "inputFile.pdf";
        final String outputFile = "outputFile.pdf";
        final int PAGE = 1;

        try {
            // Decode the file to get the text lines
            final PdfDecoderServer pdfDecoderServer = new PdfDecoderServer();
            pdfDecoderServer.openPdfFile(inputFile);
            pdfDecoderServer.decodePage(PAGE);

            // Get an estimate of the paragraphs
            final TextLines textLines = pdfDecoderServer.getTextLines();
            final int[][] paragraphs = textLines.getParagraphAreasAs2dArray(PAGE, 5);

            // Convert from X,Y,W,H format to X1,Y1,X2,Y2
            convertRectangles(paragraphs);

            // Obtain the grouping object which is used to extract text
            final PdfGroupingAlgorithms groupingObject = pdfDecoderServer.getGroupingObject();

            // Create an instance of PdfManipulator which is used to write the annotations
            final PdfManipulator pdfManipulator = new PdfManipulator();
            pdfManipulator.loadDocument(new File(inputFile));

            for (final int[] paragraph : paragraphs) {
                final int x1 = paragraph[0];
                final int y1 = paragraph[1];
                final int x2 = paragraph[2];
                final int y2 = paragraph[3];

                // Get the text contained in each paragraph
                final List<String> words = groupingObject.extractTextAsWordlist(x1, y1, x2, y2, PAGE, true, "&:=()!;.,\\/\"\"''");

                if (words == null) {
                    continue;
                }

                // Concatenate each word
                final StringBuilder paragraphString = new StringBuilder();
                for (int i = 0; i < words.size(); i += 5) {
                    paragraphString.append(words.get(i)).append(" ");
                }

                // Remove XML
                final String pureText = Strip.convertToText(paragraphString.toString(), true);

                // Translate
                final Translator translator = new Translator();
                final Translation translation = translator.translateBlocking(pureText, Language.CHINESE_SIMPLIFIED, Language.ENGLISH);
                final String translatedText = translation.getTranslatedText();

                Thread.sleep(250);

                // Add a text box annotation containing the translated text which covers the original paragraph
                final float[] rect = toFloatArray(paragraph);
                final float[] red = new float[] {0.9f, 0.5f, 0.8f};
                final int flags = Annotation.getFlagsValue(false, false, true, false, true, false, true, true, false, true);
                pdfManipulator.addAnnotation(PAGE, new FreeText(rect, flags, translatedText, red, 1.0f, 1.0f, BaseFont.Helvetica, 10, Quadding.LEFT_JUSTIFIED));
            }

            // Write the annotations to the new file
            pdfManipulator.apply();
            pdfManipulator.writeDocument(new File(outputFile));

            // Close both files
            pdfManipulator.closeDocument();
            pdfDecoderServer.closePdfFile();
        } catch (final PdfException | IOException e) {
            e.printStackTrace(System.err);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void convertRectangles(final int[][] input) {
        for (int i = 0; i < input.length; i++) {
            final int x = input[i][0];
            final int y = input[i][1];
            input[i][2] += x;
            input[i][3] += y;
        }
    }

    private static float[] toFloatArray(final int[] ints) {
        final int len = ints.length;
        final float[] floats = new float[len];
        for (int i = 0; i < ints.length; i++) {
            floats[i] = ints[i];
        }
        return floats;
    }

}
