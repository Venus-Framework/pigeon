package com.dianping.pigeon.governor.bean.serviceDesc;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/6/8.
 */
public class SearchResults{
    private long count;
    private long timeConsume;
    private List<SearchResult> searchResults;
    public void setCount(long count){
        this.count = count;
    }
    public void setTimeConsume(long time){
        this.timeConsume = time;
    }

    public SearchResults (SearchHits searchHits){
        this.searchResults = new LinkedList<SearchResult>();
        Iterator<SearchHit> searchHitIterator = searchHits.iterator();
        while(searchHitIterator.hasNext()){
            SearchHit searchHit = searchHitIterator.next();
            this.searchResults.add(new SearchResult(searchHit));
        }
    }
}
