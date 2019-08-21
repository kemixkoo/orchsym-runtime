/*
 * Licensed to the Orchsym Runtime under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * 
 * this file to You under the Orchsym License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * https://github.com/orchsym/runtime/blob/master/orchsym/LICENSE
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orchsym.processor.jsonxml;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLTokener;

public class XMLConverter extends XML{
    /** Attribute Mark. */
    private String ATTRIBUTEMARK = null;
    
    /** Namespace. */
    private String NAMESPACE = null;
    
    /** Data Tag Name */
    private String DATATAGNAME = "content";
    
    public void setAttributeMark(String s) {
        ATTRIBUTEMARK = s;
    }
    
    public void setNameSpace(String s) {
        NAMESPACE = s;
    }
    
    public void setDataTagName(String s) {
            DATATAGNAME = s;
    }

    public JSONObject toJSONObject_p(String string) throws JSONException {
        return toJSONObject_internal(new StringReader(string), false);
    }

    public JSONObject toJSONObject_p(Reader reader) throws JSONException {
        return toJSONObject_internal(reader, false);
    }

    public JSONObject toJSONObject_p(Reader reader, boolean keepStrings) throws JSONException {
        return toJSONObject_internal(reader, keepStrings);
    }

    public JSONObject toJSONObject_internal(Reader reader, boolean keepStrings) throws JSONException {
        JSONObject jo = new JSONObject();
        XMLTokener x = new XMLTokener(reader);
        while (x.more()) {
            x.skipPast("<");
            if(x.more()) {
                parse_p(x, jo, null, keepStrings);
            }
        }
        return jo;
    }

    public String toString_p(final Object object, final String tagName) {
        return toString(object, tagName, false);
    }

    private boolean parse_p(XMLTokener x, JSONObject context, String name, boolean keepStrings)
            throws JSONException {
        char c;
        int i;
        JSONObject jsonobject = null;
        String string;
        String tagName;
        Object token;
        token = x.nextToken();
        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                            context.accumulate(DATATAGNAME, string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {

            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {

            // Close tag </

            token = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <

        } else {
            tagName = (String) token;
            token = null;
            jsonobject = new JSONObject();
            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }
                // attribute = value
                if (token instanceof String) {
                    string = (String) token;
                    token = x.nextToken();
                    if (ATTRIBUTEMARK != null) {
                        string = ATTRIBUTEMARK + string;
                    }
                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }
                        jsonobject.accumulate(string,
                                keepStrings ? ((String)token) : stringToValue((String) token));
                        token = null;
                    } else {
                        jsonobject.accumulate(string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (jsonobject.length() > 0) {
                        context.accumulate(tagName, jsonobject);
                    } else {
                        context.accumulate(tagName, "");
                    }
                    return false;

                } else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();
                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {
                            string = (String) token;
                            if (string.length() > 0) {
                                jsonobject.accumulate(DATATAGNAME,
                                        keepStrings ? string : stringToValue(string));
                            }

                        } else if (token == LT) {
                            // Nested element
                            if (parse_p(x, jsonobject, tagName,keepStrings)) {
                                if (jsonobject.length() == 0) {
                                    context.accumulate(tagName, "");
                                } else if (jsonobject.length() == 1
                                        && jsonobject.opt(DATATAGNAME) != null) {
                                    context.accumulate(tagName,
                                            jsonobject.opt(DATATAGNAME));
                                } else {
                                    context.accumulate(tagName, jsonobject);
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }
    
    private String toString(Object object, final String tagName, boolean attrInvolved)
            throws JSONException {
        StringBuilder sb = new StringBuilder();
        JSONArray ja;
        JSONObject jo;
        String string;
        
        if (attrInvolved) {
                return object.toString();
        }
        boolean containAttr = false;
        if (object instanceof JSONObject) {

            // Emit <tagName>
            if (tagName != null) {
                sb.append('<');
                sb.append(tagName);
                //第一个tag, 添加namespce
                if (NAMESPACE != null) {
                        sb.append(" xmlns=\"").append(NAMESPACE).append("\"");
                        NAMESPACE = null;
                }
                Map<String, Object> map = ((JSONObject)object).toMap();
                for (Object key : map.keySet()) {
                        if (ATTRIBUTEMARK != null && ((String)key).contains(ATTRIBUTEMARK)) {  
                            containAttr = true;
                            break;
                        } 
                }
                //包含attributes
                if (containAttr) {
                        ArrayList<String> removeKeyArr = new ArrayList<String>();
                        Map<String, Object> objMap = null;
                        for (Object key : map.keySet()) {
                            if (((String)key).contains(ATTRIBUTEMARK)) {
                                removeKeyArr.add((String)key);
                                String keyStr = ((String)key).replace(ATTRIBUTEMARK, "");
                                sb.append(" ").append(keyStr).append("=\"").append(map.get(key)).append("\"");;
                            } else if (map.containsKey(DATATAGNAME)){
                                //single item, get the content
                                objMap = new HashMap<String, Object>();
                                objMap.put(tagName, map.get(DATATAGNAME));
                                object = new JSONObject(objMap);
                            }
                    }
                        if (objMap == null) {
                            for (String key : removeKeyArr) {
                                //remove the attribute items
                                map.remove(key);
                            }
                            objMap = map;
                            containAttr = false;
                        }
                        object = new JSONObject(objMap);
                }
                sb.append('>');
            }

            // Loop thru the keys.
            // don't use the new entrySet accessor to maintain Android Support
            jo = (JSONObject) object;
            for (final String key : jo.keySet()) {
                Object value = jo.opt(key);
                if (value == null) {
                    value = "";
                } else if (value.getClass().isArray()) {
                    value = new JSONArray(value);
                }

                // Emit content in body
                if ("content".equals(key)) {
                    if (value instanceof JSONArray) {
                        ja = (JSONArray) value;
                        int jaLength = ja.length();
                        // don't use the new iterator API to maintain support for Android
                        for (int i = 0; i < jaLength; i++) {
                            if (i > 0) {
                                sb.append('\n');
                            }
                            Object val = ja.opt(i);
                            sb.append(escape(val.toString()));
                        }
                    } else {
                        sb.append(escape(value.toString()));
                    }

                    // Emit an array of similar keys

                } else if (value instanceof JSONArray) {
                    ja = (JSONArray) value;
                    int jaLength = ja.length();
                    // don't use the new iterator API to maintain support for Android
                    for (int i = 0; i < jaLength; i++) {
                        Object val = ja.opt(i);
                        if (val instanceof JSONArray) {
                            sb.append('<');
                            sb.append(key);
                            sb.append('>');
                            sb.append(toString(val));
                            sb.append("</");
                            sb.append(key);
                            sb.append('>');
                        } else {
                            sb.append(toString(val, key, containAttr));
                        }
                    }
                } else if ("".equals(value)) {
                    sb.append('<');
                    sb.append(key);
                    sb.append("/>");

                    // Emit a new tag <k>

                } else {
                    sb.append(toString(value, key, containAttr));
                }
            }
            if (tagName != null) {

                // Emit the </tagname> close tag
                sb.append("</");
                sb.append(tagName);
                sb.append('>');
            }
            return sb.toString();

        }

        if (object != null && (object instanceof JSONArray ||  object.getClass().isArray())) {
            if(object.getClass().isArray()) {
                ja = new JSONArray(object);
            } else {
                ja = (JSONArray) object;
            }
            int jaLength = ja.length();
            // don't use the new iterator API to maintain support for Android
            for (int i = 0; i < jaLength; i++) {
                Object val = ja.opt(i);
                // XML does not have good support for arrays. If an array
                // appears in a place where XML is lacking, synthesize an
                // <array> element.
                sb.append(toString(val, tagName == null ? "array" : tagName, containAttr));
            }
            return sb.toString();
        }

        string = (object == null) ? "null" : escape(object.toString());
        return (tagName == null) ? "\"" + string + "\""
                : (string.length() == 0) ? "<" + tagName + "/>" : "<" + tagName
                        + ">" + string + "</" + tagName + ">";

    }

    public byte[] toBytes_p(final Object object, final String tagName, final String encoding) throws IOException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            baos.write(("<?xml version=\"1.0\" encoding=\"" + encoding + "\"?>").getBytes(StandardCharsets.UTF_8));
            baos.write(toBytes(object, tagName, false));
            return baos.toByteArray();
        }
    }

    private byte[] toBytes(Object object, final String tagName, boolean attrInvolved) throws JSONException {
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            JSONArray ja;
            JSONObject jo;
            if (attrInvolved) {
                return object.toString().getBytes(StandardCharsets.UTF_8);
            }
            boolean containAttr = false;
            if (object instanceof JSONObject) {
                // Emit <tagName>
                if (tagName != null) {
                    baos.write('<');
                    baos.write(tagName.getBytes(StandardCharsets.UTF_8));
                    //第一个tag, 添加namespce
                    if (NAMESPACE != null) {
                        baos.write((" xmlns=\"" + NAMESPACE + "\"").getBytes(StandardCharsets.UTF_8));
                        NAMESPACE = null;
                    }
                    Map<String, Object> map = ((JSONObject)object).toMap();
                    for (Object key : map.keySet()) {
                        if (ATTRIBUTEMARK != null && ((String)key).contains(ATTRIBUTEMARK)) {
                            containAttr = true;
                            break;
                        }
                    }
                    //包含attributes
                    if (containAttr) {
                        ArrayList<String> removeKeyArr = new ArrayList<>();
                        Map<String, Object> objMap = null;
                        for (Object key : map.keySet()) {
                            if (((String)key).contains(ATTRIBUTEMARK)) {
                                removeKeyArr.add((String)key);
                                String keyStr = ((String)key).replace(ATTRIBUTEMARK, "");
                                baos.write((" " + keyStr + "=\"" + map.get(key) + "\"").getBytes(StandardCharsets.UTF_8));
                            } else if (map.containsKey(DATATAGNAME)){
                                //single item, get the content
                                objMap = new HashMap<String, Object>();
                                objMap.put(tagName, map.get(DATATAGNAME));
                                object = new JSONObject(objMap);
                            }
                        }
                        if (objMap == null) {
                            for (String key : removeKeyArr) {
                                //remove the attribute items
                                map.remove(key);
                            }
                            objMap = map;
                            containAttr = false;
                        }
                        object = new JSONObject(objMap);
                    }
                    baos.write('>');
                }

                // Loop thru the keys.
                // don't use the new entrySet accessor to maintain Android Support
                jo = (JSONObject) object;
                for (final String key : jo.keySet()) {
                    Object value = jo.opt(key);
                    if (value == null) {
                        value = "";
                    } else if (value.getClass().isArray()) {
                        value = new JSONArray(value);
                    }

                    // Emit content in body
                    if ("content".equals(key)) {
                        if (value instanceof JSONArray) {
                            ja = (JSONArray) value;
                            int jaLength = ja.length();
                            // don't use the new iterator API to maintain support for Android
                            for (int i = 0; i < jaLength; i++) {
                                if (i > 0) {
                                    baos.write('\n');
                                }
                                Object val = ja.opt(i);
                                baos.write(escape(val.toString()).getBytes(StandardCharsets.UTF_8));
                            }
                        } else {
                            baos.write(escape(value.toString()).getBytes(StandardCharsets.UTF_8));
                        }

                        // Emit an array of similar keys

                    } else if (value instanceof JSONArray) {
                        ja = (JSONArray) value;
                        int jaLength = ja.length();
                        // don't use the new iterator API to maintain support for Android
                        for (int i = 0; i < jaLength; i++) {
                            Object val = ja.opt(i);
                            if (val instanceof JSONArray) {
                                StringBuilder sb = new StringBuilder();
                                sb.append('<').append(key).append('>').append(toString(val)).append("</").append(key).append('>');
                                baos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
                            } else {
                                baos.write(toBytes(val, key, containAttr));
                            }
                        }
                    } else if ("".equals(value)) {
                        baos.write(("<" + key + "/>").getBytes(StandardCharsets.UTF_8));
                        // Emit a new tag <k>

                    } else {
                        baos.write(toBytes(value, key, containAttr));
                    }
                }
                if (tagName != null) {

                    // Emit the </tagname> close tag
                    baos.write(("</" + tagName + ">").getBytes(StandardCharsets.UTF_8));
                }
                return baos.toByteArray();

            }

            if (object != null && (object instanceof JSONArray ||  object.getClass().isArray())) {
                if(object.getClass().isArray()) {
                    ja = new JSONArray(object);
                } else {
                    ja = (JSONArray) object;
                }
                baos.write("<array>".getBytes(StandardCharsets.UTF_8));
                int jaLength = ja.length();
                // don't use the new iterator API to maintain support for Android
                for (int i = 0; i < jaLength; i++) {
                    Object val = ja.opt(i);
                    // XML does not have good support for arrays. If an array
                    // appears in a place where XML is lacking, synthesize an
                    // <array> element.
                    baos.write((toBytes(val, tagName == null ? "element" : tagName, containAttr)));
                }
                baos.write("</array>".getBytes(StandardCharsets.UTF_8));
                return baos.toByteArray();
            }

            // Just a string
            String string = (object == null) ? "null" : escape(object.toString());
            String jsonString2Xml = (tagName == null) ? "\"" + string + "\""
                    : (string.length() == 0) ? "<" + tagName + "/>"
                    : "<" + tagName + ">" + string + "</" + tagName + ">";
            baos.write(jsonString2Xml.getBytes(StandardCharsets.UTF_8));
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "".getBytes(StandardCharsets.UTF_8);
    }
}
