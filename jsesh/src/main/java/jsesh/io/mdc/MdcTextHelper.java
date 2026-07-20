package jsesh.io.mdc;

import java.io.StringWriter;

import jsesh.model.TopItemList;
import jsesh.model.tools.MDCNormalizer;
import jsesh.parser.MDCParserModelGenerator;
import jsesh.parser.MDCSyntaxError;

/**
 * Utility class for quick manipulation of MDC text.
 * MdcTextHelper
 */
public class MdcTextHelper {
    private MdcTextHelper() {
        super();
    }

    /**
     * Parse a standard JSesh MDC text into a TopItemList.
     * @param mdc a mdc String in the current JSesh version.
     * @return a TopItemList representing the text.
     * @throws MDCSyntaxError
     */
    public static TopItemList parse(String mdc) throws MDCSyntaxError {
        MDCParserModelGenerator mdcParser = new MDCParserModelGenerator();
        return mdcParser.parse(mdc);
    }

    /**
     * Convert a TopItemList into a standard JSesh MDC text.
     * @param top a top item list.
     * @return a MDC String.
     */
    public static String toMdc(TopItemList top) {
        MdCModelWriter mdCModelWriter = new MdCModelWriter();
        StringWriter writer = new StringWriter();
        mdCModelWriter.write(writer, top);
        return writer.toString();
    }

    /**
     * Normalize a MDC String, replacing all phonetic codes by their canonical Gardiner codes.
     * @param mdc
     * @return a normalized MDC String.
     * @throws MDCSyntaxError
     */
    
    public static String normalize(String mdc) throws MDCSyntaxError {
        TopItemList t = parse(mdc);
        new MDCNormalizer().normalize(t);
        return toMdc(t);
    }

}
