package com.dianping.pigeon.governor.service;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchResponse;

/**
 * Created by shihuashen on 16/5/26.
 */
public interface EsService {
    //TODO 仅为测试用.后续需要考虑分页,高亮,是否将其封装为一个方便前端展示的Bean.
    SearchResponse search(String indexName, String type, String key,int start,int size);
    IndexResponse index(String indexName, String type, String id, String source);
    SearchResponse countTotalHits(String indexName,String type,String key);
    DeleteResponse remove(String index, String type, Integer serviceId);
}
