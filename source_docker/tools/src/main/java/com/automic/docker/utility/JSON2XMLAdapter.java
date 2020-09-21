/**
 * 
 */
package com.automic.docker.utility;

/**
 * Utility Class 
 *
 */
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class converts the given json object to xml.
 * @author shrutinambiar
 *
 */
public final class JSON2XMLAdapter {

    private static final String REGEX = "\\A(?!XML)[a-zA-Z_][\\w0-9-]*";
    
    private JSON2XMLAdapter() {
    }

    /**
     * Method to adopt a json object so that it could be later converted to a valid xml content.
     * 
     * @param json
     * @return JSONObject which is well formed to be converted to xml
     */
    public static JSONObject adoptJsonToXml(JSONObject json) {
        String[] names = JSONObject.getNames(json);
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                // Iterating all the keys for the specified json object and
                // check key should be valid xml element name.
                if (!Pattern.matches(REGEX, names[i])) {
                    // As key is invalid so take json as string and clear it and
                    // put it again under value.
                    String temp = json.toString();
                    emptyJson(json);
                    json.put("value", temp);
                    break;
                } else {
                    // If key is valid then recursively iterate it.
                    Object obj = json.get(names[i]);
                    iterateObject(obj);
                }
            }
        }
        return json;
    }

    private static void iterateObject(Object obj) {
        if (obj instanceof JSONObject) {
            adoptJsonToXml((JSONObject) obj);
        } else if (obj instanceof JSONArray) {
            traverseJSONArray((JSONArray) obj);
        }
    }

    private static void traverseJSONArray(JSONArray jsona) {
        for (int i = 0; i < jsona.length(); i++) {
            Object obj = jsona.get(i);
            iterateObject(obj);
        }
    }

    private static void emptyJson(JSONObject json) {
        String[] names = JSONObject.getNames(json);
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                json.remove(names[i]);
            }
        }
    }

}
