package jsesh.hieroglyphs.data.io;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.hieroglyphs.resources.HieroglyphResources;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Reader class for XML description of sign data.
 * Uses the Director/Builder Design Pattern.
 * @author rosmord
 */

public class SignDescriptionReader {

    private static final String DTD_PUBLIC_HEADER = "-//ORG/QENHERKHOPESHEF//DTD SIGNDESCRIPTION 1.0";

    /**
     * The builder used to actually deal with the informations.
     */
    private SignDescriptionBuilder signDescriptionBuilder;
    
    
    public class SignDescriptionReaderAux extends DefaultHandler {

    	private static final int INITIAL = 0;
        private static final int IN_SIGN_DESCRIPTION = 1;

        /**
         * Name of the attribute for the sign code.
         */
        private static final String SIGN = "sign";
        private int state = 0;
        private StringBuffer currentText = new StringBuffer();
       
        
        /**
         * Current sign code, for the sign description.
         */
        private String currentSign = null;
		private String currentLang= "en";

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (state == IN_SIGN_DESCRIPTION) {
                currentText.append(new String(ch, start, length));
            }
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (qName.equals(SignDescriptionConstants.SIGNS)) {
                // DO NOTHING
            } else if (qName.equals(SIGN)) {
                updateCurrentSign(attributes);
            } else if (qName.equals(SignDescriptionConstants.HAS_TRANSLITERATION)) {
                updateCurrentSign(attributes);
                String use= attributes.getValue(SignDescriptionConstants.USE);
                String transliteration = attributes.getValue(SignDescriptionConstants.TRANSLITERATION);
                String type = attributes.getValue(SignDescriptionConstants.TYPE);
                signDescriptionBuilder.addTransliteration(currentSign, transliteration, use,  type);
            } else if (qName.equals(SignDescriptionConstants.VARIANT_OF)) {
                updateCurrentSign(attributes);
                String baseSign = attributes.getValue(SignDescriptionConstants.BASE_SIGN);
                String isSimilar= attributes.getValue(SignDescriptionConstants.IS_SIMILAR);
                String degree= attributes.getValue(SignDescriptionConstants.LINGUISTIC);
                signDescriptionBuilder.addVariant(currentSign, baseSign, isSimilar, degree);
            } else if (qName.equals(SignDescriptionConstants.IS_PART_OF)) {
                updateCurrentSign(attributes);
                String baseSign = attributes.getValue(SignDescriptionConstants.BASE_SIGN);
                signDescriptionBuilder.addPartOf(currentSign, baseSign);
            } else if (qName.equals(SignDescriptionConstants.CONTAINS)) {
                updateCurrentSign(attributes);
                String partCode = attributes.getValue(SignDescriptionConstants.PART_CODE);
                signDescriptionBuilder.addPartOf(partCode, currentSign);
            } else if (qName.equals(SignDescriptionConstants.SIGN_DESCRIPTION)) {
                state = IN_SIGN_DESCRIPTION;
                updateCurrentSign(attributes);
                currentLang= attributes.getValue(SignDescriptionConstants.LANG);
                // Reset the text.
                currentText.setLength(0);
            } else if (qName.equals(SignDescriptionConstants.IS_DETERMINATIVE)) {
                updateCurrentSign(attributes);
                String category = attributes.getValue(SignDescriptionConstants.CATEGORY);
                signDescriptionBuilder.addDeterminativeValue(currentSign, category);
            } else if (qName.equals(SignDescriptionConstants.HAS_TAG)) {
                updateCurrentSign(attributes);
                String tag = attributes.getValue(SignDescriptionConstants.TAG);
                signDescriptionBuilder.addTagToSign(currentSign, tag);
            } else if (qName.equals(SignDescriptionConstants.DETERMINATIVE_CATEGORY)) {
                String category = attributes.getValue(SignDescriptionConstants.CATEGORY);
                String lang = attributes.getValue(SignDescriptionConstants.LANG);
                String label = attributes.getValue(SignDescriptionConstants.LABEL);
                signDescriptionBuilder.addDeterminative(category, lang, label);
            } else if (qName.equals(SignDescriptionConstants.TAG_CATEGORY)) {
            	String tag = attributes.getValue(SignDescriptionConstants.TAG);
            	signDescriptionBuilder.addTagCategory(tag);
            } else if (qName.equals(SignDescriptionConstants.TAG_LABEL)) {
                String tag = attributes.getValue(SignDescriptionConstants.TAG);
                String lang = attributes.getValue(SignDescriptionConstants.LANG);
                String label = attributes.getValue(SignDescriptionConstants.LABEL);
                signDescriptionBuilder.addTagLabel(tag, lang, label);
            } else if (qName.equals(SignDescriptionConstants.PHANTOM)) {
            	updateCurrentSign(attributes);
                // TODO : do something with phantoms. call ghostbuster.
            	String baseSign = attributes.getValue(SignDescriptionConstants.BASE_SIGN);
            	String existsIn = attributes.getValue(SignDescriptionConstants.EXISTS_IN);
                
            	signDescriptionBuilder.addPhantom(currentSign, baseSign, existsIn);
            	
            } else {
                throw new SAXException("Unnandled element " + qName);
            }
        }

        private void updateCurrentSign(Attributes attributes) {
            String sign = attributes.getValue(SIGN);
            if (sign != null) {
                currentSign = sign;
            }
            String alwaysDisplay= attributes.getValue(SignDescriptionConstants.ALWAYS_DISPLAY);
            if (alwaysDisplay != null && "y".equals(alwaysDisplay)) {
            	signDescriptionBuilder.setSignAlwaysDisplay(currentSign);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (qName.equals(SignDescriptionConstants.SIGN_DESCRIPTION)) {
                signDescriptionBuilder.addSignDescription(currentSign, currentText.toString(), currentLang);
                state = INITIAL;
            }
        }

            @Override
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
            // we shall be quite forgiving about the DTD name and such.
            InputStream inputStream = HieroglyphResources.getSignDescriptionDTD();
            return new InputSource(inputStream);
        }
    }

	

    public  SignDescriptionReader(SignDescriptionBuilder signDescriptionBuilder) {
        super();
        this.signDescriptionBuilder= signDescriptionBuilder;
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

            //System.err.println("VALIDATING " + parser.isValidating());

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
