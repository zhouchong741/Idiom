package com.zc741.idiom;

/**
 * Created by jiae on 2016/5/30.
 */
public class SearchWord {
    private String searchWord;

    public SearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public String getSearchWord() {
        return searchWord;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    @Override
    public String toString() {
        return "searchWord=" + searchWord;
    }
}
