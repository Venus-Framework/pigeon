package com.dianping.pigeon.governor.bean.serviceDesc;

import com.google.gson.Gson;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.highlight.HighlightField;

import java.util.Map;

/**
 * Created by shihuashen on 16/6/8.
 */
public class SearchResult{
    private String serviceName;
    private String[] fragments;
    private String fragmentsFormat;
    private String serviceId;

    public SearchResult(SearchHit hit){
        this.serviceId = hit.getId();
        Map<String,Object> sourceMap = hit.sourceAsMap();
        String s = new Gson().toJson(sourceMap.get("serviceName"));
        this.serviceName = s.substring(1,s.length()-1);
        Map<String,HighlightField> map = hit.getHighlightFields();
        if(map.containsKey("highlight_field")){
            HighlightField  highlightField = map.get("highlight_field");
            Text[] texts = highlightField.getFragments();
            String[] strs = new String[texts.length];
            for(int i=0;i<texts.length;i++){
                strs[i] = texts[i].string();
            }
            this.fragments = strs;
        }else
            fragments = null;
        fragmentsFormat = "";
        for(int i = 0 ;i<fragments.length;i++){
            if(i==0){
                fragmentsFormat+=fragments[i];
            }else{
                fragmentsFormat+=("...."+fragments[i]);
            }
        }

    }
}
