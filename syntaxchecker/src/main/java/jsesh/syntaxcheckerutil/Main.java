package jsesh.syntaxcheckerutil;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Runs on a source tree (passed as argument) and find ".gly" files which contain syntax errors.
 */
public class Main {
    
    public static void main(String[] args) throws IOException {
        Path root = Path.of(args[0]);
        SyntaxCheckerUtil util = new SyntaxCheckerUtil();
        util.checkSyntax(root);
    }
}
