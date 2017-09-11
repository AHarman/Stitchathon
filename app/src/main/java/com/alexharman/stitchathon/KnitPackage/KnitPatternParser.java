package com.alexharman.stitchathon.KnitPackage;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Takes a JSON pattern and converts it to a KnitPattern
 */

public class KnitPatternParser {

    public KnitPatternParser() {

    }

    public KnitPattern parseJSON(String stringJsonPattern) throws JSONException {
        return parseJSON(new JSONObject(stringJsonPattern));
    }

    public KnitPattern parseJSON(JSONObject jsonPattern) throws JSONException {
        String patternName = jsonPattern.getString("name");
        Log.d("Parse", "Name: " + patternName);
        ArrayList<String> properties = extractProperties(jsonPattern);
        HashMap<String, KnitPattern> subpatterns = extractSubpatterns(jsonPattern);
        ArrayList<ArrayList<String>> stringPattern = extractPattern(jsonPattern);


        // TODO: RM
        String[][] stitches = new String[stringPattern.size()][0];
        for (int i = 0; i < stringPattern.size(); i++) {
            stitches[i] = stringPattern.get(i).toArray(new String[stringPattern.get(i).size()]);
        }
        return new KnitPattern(stitches);
    }

    private ArrayList<String> extractProperties(JSONObject jsonPattern) throws JSONException {
        ArrayList<String> properties = new ArrayList<>();
        if (!jsonPattern.isNull("properties")) {
            JSONArray jsonProperties = jsonPattern.getJSONArray("properties");
            for (int i = 0; i < jsonProperties.length(); i++) {
                Log.d("Parse", "Property: " + jsonProperties.getString(i));
                properties.add(jsonProperties.getString(i));
            }
        }
        return properties;
    }

    private HashMap<String, KnitPattern> extractSubpatterns(JSONObject jsonPattern) throws JSONException {
        HashMap<String, KnitPattern> subpatterns = new HashMap<>();
        JSONArray subpatternsJSON;

        if (!jsonPattern.isNull("subPatterns")) {
            subpatternsJSON = jsonPattern.getJSONArray("subpatterns");
            for (int i = 0; i < subpatternsJSON.length(); i++) {
                KnitPattern subpattern = parseJSON(subpatternsJSON.getJSONObject(i));

                Log.d("Parse", "Subpattern: " + subpattern.name);
                subpatterns.put(subpattern.name, subpattern);
            }
        }
        return subpatterns;
    }

    private ArrayList<ArrayList<String>> extractPattern(JSONObject jsonPattern) throws JSONException {
        ArrayList<ArrayList<String>> stringPattern = new ArrayList<>();
        JSONArray jsonRawPattern = jsonPattern.getJSONArray("pattern");
        for (int i = 0; i < jsonRawPattern.length(); i++) {
            JSONArray jsonRow = jsonRawPattern.getJSONArray(i);
            stringPattern.add(new ArrayList<String>());
            for (int j = 0; j < jsonRow.length(); j++) {
                stringPattern.get(i).add(jsonRow.getString(j).toUpperCase());
            }
        }

        return stringPattern;
    }
}
