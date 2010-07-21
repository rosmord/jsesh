/*
 * Created on 21 mai 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package jsesh.mdc.output.dom;

import java.io.File;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.TopItemList;

import org.w3c.dom.Document;

/**
 * @author rosmord
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MDCDomTest {

	public static void main(String[] args) throws MDCSyntaxError, TransformerException {
		// Simple tests.
		jsesh.mdc.MDCParserModelGenerator gen;
		gen= new jsesh.mdc.MDCParserModelGenerator();
		String s;
		s= "<-ra-ms-s->-i-W -pA -x:r-xr -aA:a*Y1 -n :x*(t:tA)-xAst -d:y-wAt -p\\r1*t\\80:pt\\?-!";
		s+= "-a-[[-p*t-]]-pt-[[*p*]]*t:pt-A1\\\\80\\s\\truc";
		// Tests :
		TopItemList t= gen.parse(new java.io.StringReader(s));
		
		MDCDOMBuilder domBuilder= new MDCDOMBuilder();
		Document d= domBuilder.buildMDCDOM(t);
		
		TransformerFactory transformerFactory= TransformerFactory.newInstance();
		Transformer trans= transformerFactory.newTransformer();
		StreamResult r= new StreamResult(new File("/tmp/test.xml"));
		trans.transform(new DOMSource(d), r);
		
		
		s= "<-ra-ms-s->-i-W -pA -x:r-xr -aA:a*Y1 -n :x*(t:tA)-xAst -d:y-wAt -p\\r1*t\\80:pt\\?-!\n";
		s+= "-a-[[-p*t-]]-pt-[[-p-]]*t:pt-A1\\\\80\\s\\truc";
		gen.setPhilologyAsSigns(false);
		t=gen.parse(new java.io.StringReader(s));
		d= domBuilder.buildMDCDOM(t);
		trans.transform(new DOMSource(d), r);
	}
}
