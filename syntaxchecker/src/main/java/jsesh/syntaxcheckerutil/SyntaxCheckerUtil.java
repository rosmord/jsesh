package jsesh.syntaxcheckerutil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.constants.Dialect;
import jsesh.mdc.constants.JSeshInfoConstants;
import jsesh.mdc.file.MDCDocument;
import jsesh.mdc.file.MDCDocumentReader;
import jsesh.utils.ByteArraysUtils;

/**
 * SyntaxCheckerUtil
 */
public class SyntaxCheckerUtil {

    public void checkSyntax(Path root) throws IOException {
        Files.walk(root).filter(
                p -> p.getFileName().toString().toLowerCase().endsWith(".gly")).forEach(p -> parseFile(p));
    }

    public void parseFile(Path path) {
        MDCDocumentReader documentReader = new MDCDocumentReader();
        documentReader.failFast();
        InputStream input;
        try {
            input = Files.newInputStream(path);
            File file = path.toFile();
            try {
                documentReader.readStream(input, file);
            } catch (MDCSyntaxError e) {
                System.out.println(path + " : error at line " + e.getLine() );
            } catch (IOException e) {
                System.out.println(path + " : file problem" );
            }
        } catch (IOException e) {            
            e.printStackTrace();
        }
    }

}
