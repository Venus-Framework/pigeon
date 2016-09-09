package com.dianping.pigeon.governor.service.impl;

import com.dianping.lion.client.Lion;
import com.dianping.pigeon.governor.service.EsService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.search.function.FiltersFunctionScoreQuery;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.stereotype.Service;
import org.elasticsearch.search.highlight.HighlightBuilder.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by shihuashen on 16/5/26.
 */
@Service
public class EsServiceImpl implements EsService {
    //TODO using XML to config the Client. Which aimed at the es server Ip and other client configs.
    private Client esClient;
    private Logger logger = LogManager.getLogger(EsServiceImpl.class.getName());
    public EsServiceImpl(){
        String clusterName  = Lion.get("pigeon-governor-server.es.clustername","elasticsearch");
        String hostName = Lion.get("pigeon-governor-server.es.hostname","localhost");
        int port  = Integer.parseInt(Lion.get("pigeon-governor-server.es.port","9300"));
        esClient = null;
        Settings setting = Settings.settingsBuilder()
                .put("cluster.name",clusterName).build();
        try {
            esClient = TransportClient.builder().settings(setting).build().addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName(hostName),port));
        } catch (UnknownHostException e) {
            logger.error(e);
            e.printStackTrace();
        }
    }
    @Override
    public SearchResponse search(String indexName,String type,String key,int start,int size) {
        SearchRequestBuilder searchRequestBuilder = parseSearchBuilder(indexName,type,key);
        buildMatchFieldsHighlight(searchRequestBuilder,"highlight_field",50,3);
        searchRequestBuilder.setHighlighterRequireFieldMatch(false);
        SearchResponse response =searchRequestBuilder
                .setHighlighterPreTags("<span class='red'>")
                .setHighlighterPostTags("</span>")
                .setFrom(start).setSize(size).setExplain(true).execute().actionGet();
        return response;
    }

    @Override
    public IndexResponse index(String indexName, String type, String id, String source) {
        return esClient.prepareIndex(indexName,type,id).setSource(source).get();
    }

    @Override
    public SearchResponse countTotalHits(String indexName, String type, String key) {
        SearchRequestBuilder searchRequestBuilder = parseSearchBuilder(indexName,type,key);
        SearchResponse response = searchRequestBuilder
                .setFrom(0).setSize(0).setExplain(true).execute().actionGet();
        return response;
    }

    @Override
    public DeleteResponse remove(String index, String type, Integer serviceId) {
        return esClient.prepareDelete(index,type,String.valueOf(serviceId)).get();
    }

    private void buildMatchFieldsHighlight (SearchRequestBuilder searchRequestBuilder,
                                            String fieldName,
                                            int fragmentSize,
                                            int fragmentsNumber){
        Field  field = new Field(fieldName).fragmentSize(fragmentSize).numOfFragments(fragmentsNumber);
        String[] combinedFields = {fieldName+".english_analyze",fieldName,fieldName+".cn",fieldName+".detail"};
        field.matchedFields(combinedFields);
        searchRequestBuilder.internalBuilder().highlighter().field(field);
    }

    private SearchRequestBuilder parseSearchBuilder(String indexName,String type,String key){
        QueryBuilder serviceFieldQueryBuilder = multiMatchQuery(
                key,
                "serviceName","serviceName.raw^3","serviceName.english_analyze","serviceName.detail",
                "serviceImpl","serviceImpl.raw^3", "serviceImpl.english_analyze","serviceImpl.detail",
                "serviceDesc"
        );
        QueryBuilder methodFieldQueryBuilder = multiMatchQuery(
                key,
                "methodDescBeanList.methodName","methodDescBeanList.methodName.raw^3","methodDescBeanList.methodName.english_analyze","methodDescBeanList.methodName.detail",
                "methodDescBeanList.methodFullname","methodDescBeanList.methodFullname.raw^3","methodDescBeanList.methodFullname.english_analyze","methodDescBeanList.methodFullname.detail",
                "methodDescBeanList.methodReturnType","methodDescBeanList.methodReturnType.raw^3","methodDescBeanList.methodReturnType.english_analyze","methodDescBeanList.methodReturnType.detail",
                "methodDescBeanList.methodDesc",
                "methodDescBeanList.methodReturnDesc"
        );
        QueryBuilder paramQueryBuilder = matchQuery("methodDescBeanList.paramDescBeanArrayList.paramDesc",key);
        QueryBuilder exceptionQueryBuilder = matchQuery("paramDescBeanArrayList.exceptionDescBeanArrayList.exceptionDesc",key);
        QueryBuilder nestedMethodQueryBuilder = nestedQuery("methodDescBeanList",
                boolQuery().must(methodFieldQueryBuilder)).scoreMode("avg");
        QueryBuilder nestedParamQueryBuilder = nestedQuery("methodDescBeanList.paramDescBeanArrayList",
                boolQuery().must(paramQueryBuilder)).scoreMode("avg");
        QueryBuilder nestedExceptionQueryBuilder = nestedQuery("methodDescBeanList.exceptionDescBeanArrayList",
                boolQuery().must(exceptionQueryBuilder)).scoreMode("avg");
        QueryBuilder compoundedQueryBuilder = boolQuery().should(serviceFieldQueryBuilder)
                .should(methodFieldQueryBuilder)
                .should(nestedMethodQueryBuilder)
                .should(nestedParamQueryBuilder)
                .should(nestedExceptionQueryBuilder);

        SearchRequestBuilder searchRequestBuilder = esClient.prepareSearch(indexName)
                .setTypes(type)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(compoundedQueryBuilder);
        return searchRequestBuilder;
    }

}
