package jsesh.utilitysoftwares.signinfoeditor.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jsesh.hieroglyphs.data.SignDescriptionConstants;
import jsesh.utilitysoftwares.signinfoeditor.model.EditableSignInfo;
import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoModel;
import jsesh.utilitysoftwares.signinfoeditor.model.SignInfoProperty;
import jsesh.utilitysoftwares.signinfoeditor.model.XMLInfoProperty;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SignInfoModelXMLWriter {

    private static final String SYSTEM = "sign_description.dtd";
    private static final String URI = "-//ORG/QENHERKHOPESHEF//DTD SIGNDESCRIPTION 1.0";
    private OutputStream out;
    private Document document;

    public SignInfoModelXMLWriter(OutputStream out) {
        this.out = out;
    }

    public void writeModel(SignInfoModel model) throws IOException,
            ParserConfigurationException, TransformerException {
        try {
            createDocument();
            fillDocument(model);
            writeDOM();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private void fillDocument(SignInfoModel model) {
        for (EditableSignInfo signInfo : model.getSignInfoList()) {
            writeSignInfo(signInfo);
        }
        for (String tag : model.getTags()) {
            writeTagInfo(tag, model);
        }
    }

    private void writeTagInfo(String tag, SignInfoModel model) {
        if (model.isUserTag(tag)) {
            Element tagElement = document.createElement(SignDescriptionConstants.TAG_CATEGORY);
            tagElement.setAttribute(SignDescriptionConstants.TAG, tag);
            document.getDocumentElement().appendChild(tagElement);
        }

        List<XMLInfoProperty> labels = model.getLabelsForTag(tag);
        for (XMLInfoProperty prop : labels) {
            if (prop.isUserDefinition()) {
                Element tagLabelElement = document.createElement(SignDescriptionConstants.TAG_LABEL);
                for (String attrName : prop.getAttributeNames()) {
                    tagLabelElement.setAttribute(attrName, prop.get(attrName));
                }
                document.getDocumentElement().appendChild(tagLabelElement);
            }
        }
    }

    private void writeSignInfo(EditableSignInfo signInfo) {
        // A) check if there is something to write about this sign.
        boolean hasUserContent = false;
        if (signInfo.isAlwaysDisplayProvidedByUser()) {
            hasUserContent = true;
        }
        for (SignInfoProperty prop : signInfo.getPropertyList()) {
            if (prop.isUserDefinition()) {
                hasUserContent = true;
                break;
            }
        }

        if (hasUserContent) {
            // Generate a structure with <sign sign="...">...</sign>
            Element signElement = document.createElement(SignDescriptionConstants.SIGN);
            signElement.setAttribute(SignDescriptionConstants.SIGN, signInfo.getCode());
            if (signInfo.isAlwaysDisplay() && signInfo.isAlwaysDisplayProvidedByUser()) {
                signElement.setAttribute(SignDescriptionConstants.ALWAYS_DISPLAY, "y");
            }
            for (SignInfoProperty prop : signInfo.getPropertyList()) {
                if (prop.isUserDefinition()) {
                    Element propElement = document.createElement(prop.getName());
                    for (String attrName : prop.getAttributeNames()) {
                        propElement.setAttribute(attrName, prop.get(attrName));
                    }
                    if (!"".equals(prop.getContent())) {
                        propElement.appendChild(document.createTextNode(prop.getContent()));
                    }
                    signElement.appendChild(propElement);
                }
            }
            document.getDocumentElement().appendChild(signElement);
        }

    }

    private void createDocument() throws FactoryConfigurationError,
            ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory
                .newDocumentBuilder();
        document = documentBuilder.newDocument();
        document.appendChild(document
                .createElement(SignDescriptionConstants.SIGNS));
    }

    private void writeDOM() throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, URI);
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, SYSTEM);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(out);
        transformer.transform(source, result);
    }

}
