package org.qenherkhopeshef.json.parser;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.qenherkhopeshef.json.model.JSONArray;
import org.qenherkhopeshef.json.model.JSONData;
import org.qenherkhopeshef.json.model.JSONObject;
import org.qenherkhopeshef.json.model.JSONString;
import org.qenherkhopeshef.json.model.JSONfalse;
import org.qenherkhopeshef.json.model.JSONnull;
import org.qenherkhopeshef.json.model.JSONtrue;

public class JSONParserTest {

    private JSONData parse(String json) {
        return new JSONParser().parseJSON(new StringReader(json));
    }

    private String write(JSONData data) throws IOException {
        StringWriter sw = new StringWriter();
        data.write(sw);
        return sw.toString();
    }

    @Test
    public void parseString() {
        JSONData result = parse("\"hello\"");
        assertInstanceOf(JSONString.class, result);
        assertEquals("hello", ((JSONString) result).getValue());
    }

    @Test
    public void parseNumber() throws IOException {
        JSONData result = parse("42");
        assertEquals("42", write(result));
    }

    @Test
    public void parseDecimalNumber() throws IOException {
        JSONData result = parse("3.5");
        assertEquals("3.5", write(result));
    }

    @Test
    public void parseTrue() {
        JSONData result = parse("true");
        assertInstanceOf(JSONtrue.class, result);
    }

    @Test
    public void parseFalse() {
        JSONData result = parse("false");
        assertInstanceOf(JSONfalse.class, result);
    }

    @Test
    public void parseNull() {
        JSONData result = parse("null");
        assertInstanceOf(JSONnull.class, result);
    }

    @Test
    public void parseEmptyObject() {
        JSONData result = parse("{}");
        assertInstanceOf(JSONObject.class, result);
        assertEquals(0, ((JSONObject) result).getPropertyList().length);
    }

    @Test
    public void parseSimpleObject() {
        JSONData result = parse("{\"a\": 34, \"b\": \"hello\"}");
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        assertTrue(obj.hasProperty("a"));
        assertTrue(obj.hasProperty("b"));
        assertEquals("hello", ((JSONString) obj.getProperty("b")).getValue());
    }

    @Test
    public void parseNestedObject() {
        JSONData result = parse("{\"outer\": {\"inner\": 1}}");
        assertInstanceOf(JSONObject.class, result);
        JSONObject outer = (JSONObject) result;
        assertTrue(outer.hasProperty("outer"));
        assertInstanceOf(JSONObject.class, outer.getProperty("outer"));
        JSONObject inner = (JSONObject) outer.getProperty("outer");
        assertTrue(inner.hasProperty("inner"));
    }

    @Test
    public void parseEmptyArray() {
        JSONData result = parse("[]");
        assertInstanceOf(JSONArray.class, result);
        assertEquals(0, ((JSONArray) result).size());
    }

    @Test
    public void parseSimpleArray() {
        JSONData result = parse("[3, 4, 5]");
        assertInstanceOf(JSONArray.class, result);
        assertEquals(3, ((JSONArray) result).size());
    }

    @Test
    public void parseMixedArray() {
        JSONData result = parse("[1, \"two\", true, null]");
        assertInstanceOf(JSONArray.class, result);
        JSONArray array = (JSONArray) result;
        assertEquals(4, array.size());
        assertInstanceOf(JSONString.class, array.get(1));
        assertInstanceOf(JSONtrue.class, array.get(2));
        assertInstanceOf(JSONnull.class, array.get(3));
    }

    @Test
    public void parseComplexObject() {
        JSONData result = parse("{\"a\": 34, \"b\" : [3,4,5], \"toto\": \"un \\ttableau avec des \\\"dedans\"}");
        assertInstanceOf(JSONObject.class, result);
        JSONObject obj = (JSONObject) result;
        assertTrue(obj.hasProperty("a"));
        assertTrue(obj.hasProperty("b"));
        assertTrue(obj.hasProperty("toto"));
        assertInstanceOf(JSONArray.class, obj.getProperty("b"));
        assertEquals(3, ((JSONArray) obj.getProperty("b")).size());
    }

    @Test
    public void parseAndWriteRoundTrip() throws IOException {
        String json = "{\"a\": 34, \"b\": [3, 4, 5]}";
        JSONData first = parse(json);
        String written = write(first);
        JSONData second = parse(written);
        assertInstanceOf(JSONObject.class, second);
        JSONObject obj = (JSONObject) second;
        assertTrue(obj.hasProperty("a"));
        assertTrue(obj.hasProperty("b"));
        assertInstanceOf(JSONArray.class, obj.getProperty("b"));
        assertEquals(3, ((JSONArray) obj.getProperty("b")).size());
    }

    @Test
    public void invalidJsonThrows() {
        assertThrows(JSONException.class, () -> parse("{bad json}"));
    }

    @Test
    public void unterminatedObjectThrows() {
        assertThrows(JSONException.class, () -> parse("{\"a\":"));
    }
}
