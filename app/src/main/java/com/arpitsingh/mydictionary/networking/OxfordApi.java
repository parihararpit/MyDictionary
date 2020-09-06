package com.arpitsingh.mydictionary.networking;

/**
 * Created by ARPIT SINGH
 * 11/10/19
 */

import com.arpitsingh.mydictionary.BuildConfig;
import com.arpitsingh.mydictionary.model.SearchResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface OxfordApi {

    String APP_KEY = BuildConfig.APP_KEY;
    String APP_KEY_NAME = "app_key";
    String APP_ID = BuildConfig.APP_ID;
    String APP_ID_NAME = "app_id";

    static final String BASE_URL = "https://od-api.oxforddictionaries.com/api/v2/";

    /**
     * for a word match query the url should be like
     * ENTRY_BASE_URL + language + "/" + word_id;
     */
    static final String ENDPOINT_ENTRIES = "entries";

    /**
     * for a word match query the url should be like
     * SEARCH_BASE_URL + language + "?q=" + word_id;
     */
    static final String ENDPOINT_SEARCH = "search";

    static String DEFAULT_LANG = "en-us";

    static final String OXFORD_HEADER_ACCEPT = "Accept:application/json";

    static final String OXFORD_HEADER_KEY = OxfordApi.APP_KEY_NAME + ":" + OxfordApi.APP_KEY;

    static final String OXFORD_HEADER_ID = OxfordApi.APP_ID_NAME + ":" + OxfordApi.APP_ID;

    //method for matching words query from given language
    @Headers({
            OXFORD_HEADER_ACCEPT, OXFORD_HEADER_ID, OXFORD_HEADER_KEY
    })
    @GET(ENDPOINT_SEARCH + "/{lang}")
    Call<SearchResult> getSearchResults(@Path("lang") String lang, @Query("q") String word);

    /**
     * method for matching words query from default language
     */

    /**
     * @DEPRECATED
     */
    @Headers({
            OXFORD_HEADER_ACCEPT, OXFORD_HEADER_ID, OXFORD_HEADER_KEY
    })
    @GET(ENDPOINT_SEARCH + "/" + DEFAULT_LANG)
    Call<SearchResult> getSearchResults(@Query("q") String word);

    /**
     * Hardcoding since oxford api prototype account does not allow this endpoint
     * https://gist.githubusercontent.com/parihararpit/194b70cb2745b44285e1692301bc4591/raw/42d3e3b98694d4e47a89ca5e80abf5c0d21663dd/search.json
     */
    @GET("parihararpit/194b70cb2745b44285e1692301bc4591/raw/42d3e3b98694d4e47a89ca5e80abf5c0d21663dd/search.json")
    Call<SearchResult> getSearchResults();

    //"https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id;
    //method for matching words query from given language
    @Headers({
            OXFORD_HEADER_ACCEPT, OXFORD_HEADER_ID, OXFORD_HEADER_KEY
    })
    @GET(ENDPOINT_ENTRIES + "/{lang}/{keyword}")
    Call<String> getDefinitions(@Path("lang") String lang, @Path("keyword") String word_id);

    //method for matching words query from default language
    @Headers({
            OXFORD_HEADER_ACCEPT, OXFORD_HEADER_ID, OXFORD_HEADER_KEY
    })
    @GET(ENDPOINT_ENTRIES + "/" + DEFAULT_LANG + "/{keyword}")
    Call<String> getDefinitions(@Path("keyword") String word_id);


    //method for getting word of day
    @GET
    Call<String> getWordOfDay(@Url String url);

}
