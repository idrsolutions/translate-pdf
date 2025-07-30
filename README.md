# Translate a PDF into another language using JPedal
A proof of concept which takes a PDF and creates a translated version of the document. It uses [JPedal](https://www.idrsolutions.com/jpedal/) to read the original text, call [Translator](https://github.com/therealbush/translator) and then replace the original text using JPedal's annotations feature.

This project demonstrates how to extract text as paragraphs from PDFs, translate them using a language translation API, and reinsert the translated content back into the document as an overlay to the original content.

This is achieved using our Java PDF toolkit [JPedal](https://www.idrsolutions.com/jpedal/) to extract the text from the document, and write the annotations back to the file.

The content translation can be done with any library. We chose to use [Translator](https://github.com/therealbush/translator).
