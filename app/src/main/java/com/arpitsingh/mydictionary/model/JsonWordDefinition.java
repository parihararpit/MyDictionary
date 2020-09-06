package com.arpitsingh.mydictionary.model;
/**
 * Created by ARPIT SINGH
 * 13/10/19
 */


import java.util.List;

public class JsonWordDefinition {
    public String id;
    public String word;
    public String language;
    public List<JsonLexicalEntries> lexicalEntries;
}
