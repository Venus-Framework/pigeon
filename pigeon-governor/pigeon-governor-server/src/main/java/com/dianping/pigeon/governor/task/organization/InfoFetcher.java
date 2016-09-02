package com.dianping.pigeon.governor.task.organization;

import com.dianping.pigeon.governor.bean.*;
import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.model.ProjectOrg;
import com.dianping.pigeon.governor.util.CmdbUtils;
import com.dianping.pigeon.governor.util.GsonUtils;
import com.dianping.pigeon.governor.util.HttpCallUtils;

/**
 * Created by shihuashen on 16/8/11.
 */
public class InfoFetcher {
    private String urlPrefix = "http://api.cmdb.dp/api/v0.1/projects";
    public  Project getProject(String name){
        return CmdbUtils.getProjectInfoOrNot(name);
    }
    public  ProjectOrg getProjectOrg(String name){
        ProjectOrg projectOrg = new ProjectOrg();
        String productName = getProjectProductName(name);
        String buName = getProjectBuName(name);
        projectOrg.setName(name);
        projectOrg.setBu(buName);
        projectOrg.setProduct(productName);
        return projectOrg;
    }

     private String getProjectBuName(String projectName) {
        String buName = "Undefined";
        String url= urlPrefix+"/"+projectName+"/bu";
        String response = HttpCallUtils.httpGet(url);
        CmdbSingleBu cmdbSingleBu = GsonUtils.fromJson(response,CmdbSingleBu.class);
        if(cmdbSingleBu!=null){
            CmdbBuBean bean = cmdbSingleBu.getBu();
            if(bean!=null){
                buName = bean.getBu_name();
            }
        }
        return buName;
    }

    private String getProjectProductName(String projectName){
        String productName = "Undefined";
        String url = urlPrefix+"/"+projectName+"/product";
        String response = HttpCallUtils.httpGet(url);
        CmdbSingleProduct cmdbSingleProduct = GsonUtils.fromJson(response,CmdbSingleProduct.class);
        if(cmdbSingleProduct!=null){
            CmdbProductBean bean = cmdbSingleProduct.getProduct();
            if(bean!=null){
                productName = bean.getProduct_name();
            }
        }
        return productName;
    }
}
