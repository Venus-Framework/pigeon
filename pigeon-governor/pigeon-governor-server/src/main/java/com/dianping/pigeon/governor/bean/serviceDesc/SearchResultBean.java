package com.dianping.pigeon.governor.bean.serviceDesc;



import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/5/6.
 * 封装一次搜索的结果.增强搜索的高亮效果.暂时性只以服务和方法为两大层级.支持文本的匹配.
 * TODO 效率暂时不确定.
 */
public class SearchResultBean {
    private List<ServiceMeta> serviceMetas;
    private List<MethodMeta> methodMetas;
    private String keyWord;
    private long consumeTime;

    public SearchResultBean(){
        serviceMetas = new LinkedList<ServiceMeta>();
        methodMetas = new LinkedList<MethodMeta>();
    }


    public void setServiceMetas(List<ServiceMeta> metas){
        this.serviceMetas = metas;
    }

    public void setMethodMetas(List<MethodMeta> metas){
        this.methodMetas = metas;
    }

    public void setKeyWord(String keyWord){
        this.keyWord = keyWord;
    }

    public void setConsumeTime(long time){
        this.consumeTime = time;
    }
}
