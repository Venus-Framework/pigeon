package com.dianping.pigeon.governor.bean.Event;

import com.dianping.pigeon.governor.model.Project;
import com.dianping.pigeon.governor.service.ProjectService;
import com.dianping.pigeon.governor.util.GsonUtils;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by shihuashen on 16/8/8.
 */
public class FilterBean {
    private List<Integer> types;
    private List<Integer> levels;
    private int projectId;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat timestampFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Timestamp startTime;
    private Timestamp endTime;
    public FilterBean(String levels,String types,int projectId,String startTime,String endTime) throws ParseException {
        this.levels = split(levels);
        this.types = split(types);
        this.projectId = projectId;
        this.startTime = format(startTime);
        this.endTime = format(endTime);

    }

    public FilterBean(HttpServletRequest request, ProjectService projectService) throws ParseException {
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        String projectName = request.getParameter("projectName");
        String types = request.getParameter("type");
        String levels = request.getParameter("level");
        this.startTime = format(startTime);
        this.endTime = format(endTime);
        if(projectName.equals(""))
            this.projectId = -1;
        else{
            Project project = projectService.findProject(projectName);
            if(project==null)
                this.projectId = 0;
            else
                this.projectId = project.getId();
        }
        this.types = split(types);
        this.levels = split(levels);
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }


    private Timestamp format(String time) throws ParseException {
        Date date = formatter.parse(time);
        Timestamp ts = Timestamp.valueOf(timestampFormatter.format(date));
        return ts;
    }
    public List<Integer> getTypes() {
        return types;
    }

    public void setTypes(List<Integer> types) {
        this.types = types;
    }

    public List<Integer> getLevels() {
        return levels;
    }

    public void setLevels(List<Integer> levels) {
        this.levels = levels;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }



    private List<Integer> split(String str){
        List<Integer> list = new LinkedList<Integer>();
        if(str.equals(""))
            return list;
        String[] strs = GsonUtils.fromJson(str,String[].class);
        for(int i=0;i<strs.length;i++)
            list.add(Integer.valueOf(strs[i]));
        return list;
    }
}
