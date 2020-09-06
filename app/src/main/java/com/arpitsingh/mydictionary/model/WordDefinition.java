package com.arpitsingh.mydictionary.model;
/**
 * Created by ARPIT SINGH
 * 13/10/19
 */


import java.util.List;

public class WordDefinition {
    //id is same as word id
    public String id;
    public String word;
    public String language;
    public String pronunciationUrl;
    public String pronunciationText;
    public String single_definition;
    public boolean isFav;
    public List<WordLexicalEntry> lexicalEntries;

}
