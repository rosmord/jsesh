package jsesh.hieroglyphs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Reader for XML sign description files. We use SAX to parse the file, in order
 * to avoid the need for a third party library.
 *
 * @author rosmord
 *
 */
public class SignDescriptionReader {

    private static final String DTD_PUBLIC_HEADER = "-//ORG/QENHERKHOPESHEF//DTD SIGNDESCRIPTION 1.0";

    public class SignDescriptionReaderAux extends DefaultHandler {

        private static final String SIMILAR_TO = "similarTo";
        private static final String PHANTOM = "phantom";
        private static final String LABEL = "label";
        private static final String LANG = "lang";
        private static final String TAG = "tag";
        private static final String CATEGORY = "category";
        private static final String BASE_SIGN = "baseSign";
        private static final String TRANSLITTERATION = "translitteration";
        private static final String TAG_CATEGORY = "tagCategory";
        private static final String DETERMINATIVE_CATEGORY = "determinativeCategory";
        private static final String HAS_TAG = "hasTag";
        private static final String IS_DETERMINATIVE = "isDeterminative";
        private static final String SIGN_DESCRIPTION = "signDescription";
        private static final String IS_PART_OF = "partOf";
        private static final String VARIANT_OF = "variantOf";
        private static final String TYPE = "type";
        private static final String RELEVANCE = "relevance";
        private static final String HAS_TRANSLITTERATION = "hasTranslitteration";
        private static final String SIGNS = "signs";
        private static final int INITIAL = 0;
        private static final int IN_SIGN_DESCRIPTION = 1;
        private static final String CONTAINS = "contains";
        private static final String PART_CODE = "partCode";
        /**
         * Name of the attribute for the sign code.
         */
        private static final String SIGN = "sign";
        private int state = 0;
        private StringBuffer currentText = new StringBuffer();
        {
        }
        /**
         * Current sign code, for the sign description.
         */
        private String currentSign = null;

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (state == IN_SIGN_DESCRIPTION) {
                currentText.append(new String(ch, start, length));
            }
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(SIGNS)) {
                // DO NOTHING
            } else if (qName.equals(SIGN)) {
                updateCurrentSign(attributes);
            } else if (qName.equals(HAS_TRANSLITTERATION)) {
                updateCurrentSign(attributes);
                int relevance = Integer.parseInt(attributes.getValue(RELEVANCE));
                String translitteration = attributes.getValue(TRANSLITTERATION);
                String type = attributes.getValue(TYPE);
                hieroglyphsManager.addTranslitteration(currentSign, translitteration, relevance, type);
            } else if (qName.equals(VARIANT_OF)) {
                updateCurrentSign(attributes);
                String baseSign = attributes.getValue(BASE_SIGN);
                hieroglyphsManager.addVariant(currentSign, baseSign);
            } else if (qName.equals(SIMILAR_TO)) {
                updateCurrentSign(attributes);
                String baseSign = attributes.getValue(BASE_SIGN);
                hieroglyphsManager.addVariant(currentSign, baseSign);
            } else if (qName.equals(IS_PART_OF)) {
                updateCurrentSign(attributes);
                String baseSign = attributes.getValue(BASE_SIGN);
                hieroglyphsManager.addPartOf(currentSign, baseSign);
            } else if (qName.equals(CONTAINS)) {
                updateCurrentSign(attributes);
                String partCode = attributes.getValue(PART_CODE);
                hieroglyphsManager.addPartOf(partCode, currentSign);
            } else if (qName.equals(SIGN_DESCRIPTION)) {
                state = IN_SIGN_DESCRIPTION;
                updateCurrentSign(attributes);
                // Reset the text.
                currentText.setLength(0);
            } else if (qName.equals(IS_DETERMINATIVE)) {
                updateCurrentSign(attributes);
                String category = attributes.getValue(CATEGORY);
                hieroglyphsManager.addDeterminativeValue(currentSign, category);
            } else if (qName.equals(HAS_TAG)) {
                updateCurrentSign(attributes);
                String tag = attributes.getValue(TAG);
                hieroglyphsManager.addTagToSign(currentSign, tag);
            } else if (qName.equals(DETERMINATIVE_CATEGORY)) {
                String category = attributes.getValue(CATEGORY);
                String lang = attributes.getValue(LANG);
                String label = attributes.getValue(LABEL);
                hieroglyphsManager.addDeterminative(category, lang, label);
            } else if (qName.equals(TAG_CATEGORY)) {
                String tag = attributes.getValue(TAG);
                String lang = attributes.getValue(LANG);
                String label = attributes.getValue(LABEL);
                hieroglyphsManager.addTagCategory(tag, lang, label);
            } else if (qName.equals(PHANTOM)) {
                // TODO : do something with phantoms. call ghostbuster.
            } else {
                throw new SAXException("Unnandled element " + qName);
            }
        }

        private void updateCurrentSign(Attributes attributes) {
            String sign = attributes.getValue(SIGN);
            if (sign != null) {
                currentSign = sign;
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(SIGN_DESCRIPTION)) {
                hieroglyphsManager.addSignDescription(currentSign, currentText.toString());
                state = INITIAL;
            }
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            // we shall be quite forgiving about the DTD name and such.
            InputStream inputStream = SignDescriptionReader.class.getResourceAsStream("sign_description.dtd");

            return new InputSource(inputStream);
        }
    }
    /**
     * The hieroglyphsManager which will store the data.
     */
    private HieroglyphsManager hieroglyphsManager;

    public SignDescriptionReader(HieroglyphsManager hieroglyphsManager) {
        super();
        this.hieroglyphsManager = hieroglyphsManager;
    }

    public void readSignDescription(InputStream in) throws SAXException, IOException {
        try {

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setValidating(true);

            saxParserFactory.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
            saxParserFactory.setFeature("http://xml.org/sax/features/namespaces", true);
            saxParserFactory.setNamespaceAware(true);

            SAXParser parser = saxParserFactory.newSAXParser();

            parser.getXMLReader().setEntityResolver(new SignDescriptionReaderAux());

            parser.getXMLReader().setErrorHandler(new ErrorHandler() {

                public void error(SAXParseException exception) throws SAXException {
                    exception.printStackTrace();
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    exception.printStackTrace();
                    throw exception;
                }

                public void warning(SAXParseException exception) throws SAXException {
                    exception.printStackTrace();
                    throw exception;
                }
            });

            System.err.println("VALIDATING " + parser.isValidating());

            // Note : we really need to set the system id below.
            // It's not really used by JSesh, as it relies on the PUBLIC id
            // but SAX insists on it being available.
            // It still work without this in JVM 1.5*, but not in 1.4.
            parser.parse(in, new SignDescriptionReaderAux(), "file://");
        } catch (SAXNotRecognizedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}