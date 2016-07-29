package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.model.EventModel;
import com.dianping.pigeon.governor.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by shihuashen on 16/7/21.
 */
@Component
public class SignatureUtils {
    @Autowired
    private ProjectService projectService;

    public EventModel signatureAnalyze(Event event){
        EventModel model = new EventModel();
        String signature = event.getSignature();
        String[] strs = signature.split("\\$");
        if(strs[0].equals("ClientSkew")){
            model.setEventType(2);
            model.setProjectId(projectService.findProject(strs[1]).getId());
            model.setRelateProjectId(projectService.findProject(strs[2]).getId());
        }else{
            if(strs[0].equals("ServerSkew")){
                model.setEventType(1);
                model.setProjectId(projectService.findProject(strs[1]).getId());
                model.setRelateProjectId(null);
            }
            else{
                //TODO
            }
        }
        return model;
    }
}
