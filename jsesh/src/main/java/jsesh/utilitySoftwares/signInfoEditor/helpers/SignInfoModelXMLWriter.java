package jsesh.utilitySoftwares.signInfoEditor.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
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
import jsesh.utilitySoftwares.signInfoEditor.model.EditableSignInfo;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoModel;
import jsesh.utilitySoftwares.signInfoEditor.model.SignInfoProperty;
import jsesh.utilitySoftwares.signInfoEditor.model.XMLInfoProperty;

import org.w3c.dom.DOMImplementation;
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
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		}
	}

	private void fillDocument(SignInfoModel model) {
		Iterator it = model.getSignInfoList().iterator();
		while (it.hasNext()) {
			EditableSignInfo signInfo = (EditableSignInfo) it.next();
			writeSignInfo(signInfo);
		}
		// Now, the tags...
		it= model.getTags().iterator();
		while (it.hasNext()) {
			String tag= (String) it.next();
			writeTagInfo(tag, model);
		}
	}

	private void writeTagInfo(String tag, SignInfoModel model) {
		if (model.isUserTag(tag)) {
			Element tagElement= document.createElement(SignDescriptionConstants.TAG_CATEGORY);
			tagElement.setAttribute(SignDescriptionConstants.TAG, tag);
			document.getDocumentElement().appendChild(tagElement);
		}

		List l = model.getLabelsForTag(tag);
		Iterator it= l.iterator();
		while (it.hasNext()) {
			XMLInfoProperty prop= (XMLInfoProperty) it.next();
			if (prop.isUserDefinition()) {
				Element tagLabelElement= document.createElement(SignDescriptionConstants.TAG_LABEL);
				Iterator attrIt= prop.getAttributeNames().iterator();
				while (attrIt.hasNext()) {
					String attrName= (String) attrIt.next();
					tagLabelElement.setAttribute(attrName, prop.get(attrName));
				}
				document.getDocumentElement().appendChild(tagLabelElement);
			}
		}
	}

	private void writeSignInfo(EditableSignInfo signInfo) {
		// A) check if there is something to write about this sign.
		boolean hasUserContent= false;
		if (signInfo.isAlwaysDisplayProvidedByUser())
			hasUserContent= true;
		Iterator it= signInfo.getPropertyList().iterator();
		while (it.hasNext() && ! hasUserContent) {
			SignInfoProperty prop= (SignInfoProperty) it.next();
			if (prop.isUserDefinition())
				hasUserContent= true;
		}
		
		if (hasUserContent) {
			// Generate a structure with <sign sign="...">...</sign>
			Element signElement = document.createElement(SignDescriptionConstants.SIGN);
			signElement.setAttribute(SignDescriptionConstants.SIGN, signInfo.getCode());
			if (signInfo.isAlwaysDisplay() && signInfo.isAlwaysDisplayProvidedByUser())
				signElement.setAttribute(SignDescriptionConstants.ALWAYS_DISPLAY, "y");
			it= signInfo.getPropertyList().iterator();
			while (it.hasNext()) {
				SignInfoProperty prop= (SignInfoProperty) it.next();
				if (prop.isUserDefinition()) {
					Element propElement = document.createElement(prop.getName());
					Iterator attributesIterator = prop.getAttributeNames().iterator();
					while (attributesIterator.hasNext()) {
						String attrName= (String) attributesIterator.next();
						propElement.setAttribute(attrName, prop.get(attrName));
					}
					if (! "".equals(prop.getContent())) {
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
		DOMImplementation domImpl = documentBuilder.getDOMImplementation();
		// DocumentType d =
		// domImpl.createDocumentType(SignDescriptionConstants.SIGNS, URI,
		// SYSTEM);
		// document = domImpl.createDocument(null,
		// SignDescriptionConstants.SIGNS, d);

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

		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(out);
		transformer.transform(source, result);
	}


}
