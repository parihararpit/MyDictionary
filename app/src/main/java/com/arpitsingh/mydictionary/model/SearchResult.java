package com.arpitsingh.mydictionary.model;
/**
 * Created by ARPIT SINGH
 * 13/10/19
 */




import java.util.List;

/**
 * DO NOT DELETE THIS FILE
 * A search query results in form of this model
 * when ../search?=word query made
 */

public class SearchResult {
    public List<QueryResult> results;
    public class QueryResult {
        public String matchType;
        public String word;
        public String id;
    }
}
