package com.alexharman.stitchathon.KnitPackage;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes a JSON pattern and converts it to a KnitPattern
 */

public class KnitPatternParser {

    public KnitPatternParser() {

    }

    public ArrayList<ArrayList<String>> parseJSON(String stringJsonPattern) throws JSONException {
        return parseJSON(new JSONObject(stringJsonPattern));
    }

    public ArrayList<ArrayList<String>> parseJSON(JSONObject jsonPattern) throws JSONException {
        String patternName = jsonPattern.getString("name");
        Log.d("Parse", "Name: " + patternName);
        ArrayList<String> properties = extractProperties(jsonPattern);
        HashMap<String, ArrayList<ArrayList<String>>> subpatterns = extractSubpatterns(jsonPattern);
        ArrayList<ArrayList<String>> stringPattern = extractPattern(jsonPattern);

        stringPattern = expandPattern(stringPattern, properties, subpatterns);

        for (int rowCount = 0; rowCount < stringPattern.size(); rowCount++) {
            ArrayList<String> row = stringPattern.get(rowCount);
            for (int colCount = 0; colCount < row.size(); colCount++) {
                if (row.get(colCount).matches(".*[0-9]+")) {
                    ArrayList<String> expandedStitches = expandContractedStitches(row.get(colCount));
                    row.remove(colCount);
                    row.addAll(colCount, expandedStitches);
                    colCount += expandedStitches.size();
                }
            }
        }
        Log.d("parse", "Done on " + patternName);
        return stringPattern;
    }

    private ArrayList<String> expandContractedStitches(String contractedStitches){
        Pattern pattern = Pattern.compile("[0-9]+$");
        Matcher matcher = pattern.matcher(contractedStitches);
        ArrayList<String> expandedStitches = new ArrayList<>();
        if (matcher.find()) {
            int repetitions = Integer.parseInt(matcher.group(0));
            String stitch = contractedStitches.split("[0-9]+$")[0];
            for (int i = 0; i < repetitions; i++) {
                expandedStitches.add(stitch);
            }
        } else {
            expandedStitches.add(contractedStitches);
        }
        return expandedStitches;
    }

    private ArrayList<ArrayList<String>> expandPattern(ArrayList<ArrayList<String>> stringPattern, ArrayList<String> properties, HashMap<String, ArrayList<ArrayList<String>>> subpatterns) {
        ArrayList<String> row;
        String item;

        for (int i = 0; i < stringPattern.size(); i++) {
            row = stringPattern.get(i);
            for (int j = 0; j < row.size(); j++) {
                item = row.get(j);
                if (item.startsWith("pattern")) {
                    Log.d("Parse", "Dealing with " + item);
                    ArrayList<ArrayList<String>> subpattern = getSubpatternAndApplyMods(item, subpatterns);
                    stringPattern.set(i, subpattern.get(0));
                    for (int k = 1; k < subpattern.size(); k++) {
                        i++;
                        stringPattern.add(i, subpattern.get(k));
                    }
                }
            }
        }
        return stringPattern;
    }

    private ArrayList<ArrayList<String>> getSubpatternAndApplyMods(String subpatternText, HashMap<String, ArrayList<ArrayList<String>>> subpatterns) {
        String[] subpatternTextSplit = subpatternText.split(", ?");
        String subpatternName = subpatternTextSplit[0].split(":")[1];
        ArrayList<ArrayList<String>> subpattern = new ArrayList<>();
        Log.d("Parse", "subpattern name " + subpatternName);
        Log.d("Parse", "Exists? " + subpatterns.containsKey(subpatternName));
        ArrayList<ArrayList<String>> subpatternOriginal = subpatterns.get(subpatternName);
        Log.d("Parse", "Is null? " + (subpatternOriginal == null));
        for (int i = 0; i < subpatternOriginal.size(); i++) {
            subpattern.add((ArrayList<String>) subpatternOriginal.get(i).clone());
        }

        String modName;
        String modValue;
        for (int i = 1; i < subpatternTextSplit.length; i++) {
            modName = subpatternTextSplit[i].split(":")[0];
            modValue = subpatternTextSplit[i].split(":")[1];
            switch (modName) {
                // TODO: Support more modifiers
                case "mirror":
                    if (modValue.equals("ud")) {
                        Collections.reverse(subpattern);
                    } else if (modValue.equals("lr")) {
                        for (int j = 1; j < subpattern.size(); j++) {
                            Collections.reverse(subpattern.get(j));
                        }
                    }
                    break;
                case "asymmetric":
                    ArrayList<ArrayList<String>> reverseSide;
                    if (modValue.equals("mirror-ud")) {
                        reverseSide = (ArrayList<ArrayList<String>>) subpattern.clone();
                        Collections.reverse(reverseSide);
                    } else if(modValue.equals("mirror-lr")) {
                        reverseSide = (ArrayList<ArrayList<String>>) subpattern.clone();
                        for (int j = 0; j < reverseSide.size(); j++) {
                            Collections.reverse(reverseSide.get(j));
                        }
                    } else {
                        break;
                    }
                    for (int row = 0; row < subpattern.size(); row++) {
                        for (int col = 0; col < subpattern.get(row).size(); col++) {
                            subpattern.get(row).set(col, subpattern.get(row).get(col) + "/" + reverseSide.get(row).get(col));
                        }
                    }
                    break;
            }
        }
        return subpattern;
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

    private HashMap<String, ArrayList<ArrayList<String>>> extractSubpatterns(JSONObject jsonPattern) throws JSONException {
        HashMap<String, ArrayList<ArrayList<String>>> subpatterns = new HashMap<>();
        JSONArray subpatternsJSON;

        if (!jsonPattern.isNull("subpatterns")) {
            subpatternsJSON = jsonPattern.getJSONArray("subpatterns");
            for (int i = 0; i < subpatternsJSON.length(); i++) {
                JSONObject jsonSubpattern = subpatternsJSON.getJSONObject(i);
                Log.d("Parse", "Subpattern: " + jsonSubpattern.getString("name"));
                ArrayList<ArrayList<String>> subpattern = parseJSON(jsonSubpattern);
                Log.e("Parse", "Here here here " + (subpattern == null));
                subpatterns.put(jsonSubpattern.getString("name"), subpattern);
            }
        } else {
            Log.d("Parse", "No subpatterns");
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
                stringPattern.get(i).add(jsonRow.getString(j));
            }
        }

        return stringPattern;
    }
}
