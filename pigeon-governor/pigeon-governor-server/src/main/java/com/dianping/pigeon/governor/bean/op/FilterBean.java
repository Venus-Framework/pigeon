package com.dianping.pigeon.governor.bean.op;

import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.ProjectService;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by shihuashen on 16/8/10.
 */
public class FilterBean {
    private int type;
    private int projectId;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Timestamp startTime;
    private Timestamp endTime;
    public FilterBean(HttpServletRequest request, ProjectService projectService) throws ParseException {
        String type = request.getParameter("type");
        String projectName = request.getParameter("projectName");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        if(type.equals(""))
            this.type=-1;
        else
            this.type = Integer.valueOf(type);
        if(projectName.equals(""))
            this.projectId = -1;
        else{
            Project project = projectService.findProject(projectName);
            if(project!=null)
                this.projectId = project.getId();
            else
                this.projectId = 0;
        }
        this.startTime = format(startTime);
        this.endTime = format(endTime);
    }

    private Timestamp format(String time) throws ParseException {
        Date date = formatter.parse(time);
        Timestamp ts = Timestamp.valueOf(timestampFormatter.format(date));
        return ts;
    }

    public int getType(){
        return this.type;
    }
    public int getProjectId(){
        return this.projectId;
    }
    public Timestamp getStartTime(){
        return this.startTime;
    }
    public Timestamp getEndTime(){
        return this.endTime;
    }

    public String toString() {
        return "type: "+this.type+" projectId: "+this.projectId+" startTime: "+this.startTime.toString()+" endTime: "+this.endTime.toString();
    }
}
