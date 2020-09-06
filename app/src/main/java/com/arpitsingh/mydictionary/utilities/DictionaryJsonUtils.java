package com.arpitsingh.mydictionary.utilities;
/**
 * Created by ARPIT SINGH
 * 17/10/19
 */

import com.arpitsingh.mydictionary.model.JsonEntries;
import com.arpitsingh.mydictionary.model.JsonExample;
import com.arpitsingh.mydictionary.model.JsonLexicalEntries;
import com.arpitsingh.mydictionary.model.JsonSense;
import com.arpitsingh.mydictionary.model.JsonWordDefinition;
import com.arpitsingh.mydictionary.model.JsonWordSearchResult;
import com.arpitsingh.mydictionary.model.WordDefinition;

import com.arpitsingh.mydictionary.model.WordLexicalEntry;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class DictionaryJsonUtils {
    private static final String LOG_TAG = DictionaryJsonUtils.class.getSimpleName();

    public static WordDefinition getSingleWord(String json) {
        Gson gson = new Gson();

        JsonWordSearchResult jsonWordSearchResult = gson.fromJson(json, JsonWordSearchResult.class);
        if (jsonWordSearchResult == null)
            return null;
        WordDefinition word = new WordDefinition();
        JsonWordDefinition jsonWordDefinition = jsonWordSearchResult.results.get(0);

        if (jsonWordDefinition == null)
            return null;

        word.id = jsonWordDefinition.id;
        word.language = jsonWordDefinition.language;
        word.word = jsonWordDefinition.word;
        word.lexicalEntries = new ArrayList<>();

        if (jsonWordDefinition.lexicalEntries != null && jsonWordDefinition.lexicalEntries.get(0).pronunciations != null) {
            word.pronunciationUrl = jsonWordDefinition.lexicalEntries.get(0).pronunciations.get(0).audioFile;
            word.pronunciationText = jsonWordDefinition.lexicalEntries.get(0).pronunciations.get(0).phoneticSpelling;
        }

        WordLexicalEntry lexicalEntry;
        for (JsonLexicalEntries jsonLexicalEntries : jsonWordDefinition.lexicalEntries) {
            lexicalEntry = new WordLexicalEntry();
            lexicalEntry.definitions = new ArrayList<>();
            lexicalEntry.examples = new ArrayList<>();
            lexicalEntry.lexCategory = jsonLexicalEntries.lexicalCategory;

            for (int i = 0; jsonLexicalEntries.entries != null && i < jsonLexicalEntries.entries.size(); i++) {
                JsonEntries entries = jsonLexicalEntries.entries.get(i);
                if (entries.senses == null || entries.senses.size() <= 0)
                    continue;
                for (JsonSense sense : entries.senses) {
                    if (sense.definitions != null)
                        lexicalEntry.definitions.addAll(sense.definitions);
                    if (sense.examples != null) {
                        for (JsonExample example : sense.examples) {
                            lexicalEntry.examples.add(example.text);
                        }
                    }
                }
            }
            word.lexicalEntries.add(lexicalEntry);
        }
        if (word.lexicalEntries != null && word.lexicalEntries.get(0).definitions != null)
            word.single_definition = word.lexicalEntries.get(0).definitions.get(0);

        return word;
    }

    public static String getLexJsonString(WordDefinition word) {
        Gson gson = new Gson();
        TempModel model = new TempModel();
        model.lexList = word.lexicalEntries;
        return gson.toJson(model);
    }

    public static List<WordLexicalEntry> getLexList(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, TempModel.class).lexList;
    }

    static class TempModel {
        List<WordLexicalEntry> lexList;
    }

}
