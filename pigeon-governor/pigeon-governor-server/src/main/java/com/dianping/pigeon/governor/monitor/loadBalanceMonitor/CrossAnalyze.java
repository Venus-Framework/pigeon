package com.dianping.pigeon.governor.monitor.loadBalanceMonitor;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.config.ConfigManagerLoader;
import com.dianping.pigeon.governor.monitor.loadBalanceMonitor.skewMessage.ClientSkewMessage;
import com.dianping.pigeon.governor.monitor.loadBalanceMonitor.skewMessage.ServerSkewMessage;
import com.dianping.pigeon.governor.util.CatReportXMLUtils;
import com.dianping.pigeon.governor.util.Constants;
import com.dianping.pigeon.governor.util.HttpCallUtils;

import java.util.*;


/**
 * Created by shihuashen on 16/7/12.
 */
public class CrossAnalyze implements Runnable{
    private ConfigManager configManager;
    private CrossReport crossReport;
    private ServerClientDataComparator comparator;
    private BalanceAnalyzer analyzer;
    private String projectName;
    private String dateTime;
    private String url;
    public CrossAnalyze(String projectName,
                        String dateTime,
                        ServerClientDataComparator comparator,
                        BalanceAnalyzer analyzer){
        this.projectName = projectName;
        this.dateTime = dateTime;
        this.url =  getCatAddress()+"cat/r/cross?domain="+projectName+"&ip=All&date="+dateTime+"&forceDownload=xml";
        this.configManager = ConfigManagerLoader.getConfigManager();
        this.comparator = comparator;
        this.analyzer = analyzer;
    }
    @Override
    public void run() {
        this.crossReport = getCrossReport(this.projectName,this.dateTime);
        serverAndClientCountCheck(this.crossReport);
        loadBalanceCheck(this.crossReport);
    }




    private CrossReport getCrossReport(String projectName,String dateTime){
        String url = getCatAddress()+"cat/r/cross?domain="+projectName+"&ip=All&date="+dateTime+"&forceDownload=xml";
        String xml = HttpCallUtils.httpGet(url);
        return CatReportXMLUtils.XMLToCrossReport(xml);
    }
    private String getCatAddress(){
        String env = configManager.getEnv();
        if(env.equals("qa"))
            return Constants.qaCatAddress;
        if(env.equals("prelease"))
            return Constants.ppeCatAddress;
        return Constants.onlineCatAddress;
    }
    //扫描CrossReport,确认服务端收到的调用请求和客户端汇总的调用请求数是否一致.
    public void serverAndClientCountCheck(CrossReport report){
        Map<String,Local> locals = report.getLocals();
        for(Iterator<String> iterator = locals.keySet().iterator(); iterator.hasNext();){
            Local local = locals.get(iterator.next());
            Map<String,Remote> remotes = local.getRemotes();
            Map<String,Long> tmpMap = new HashMap<String,Long>();
            for(Iterator<String> iter = remotes.keySet().iterator();iter.hasNext();){
                Remote remote = remotes.get(iter.next());
                String role = remote.getRole();
                String ip = remote.getIp();
                if(role.equals("Pigeon.Caller")||role.equals("Pigeon.Client")){
                    if(tmpMap.containsKey(ip)){
                        long count =  tmpMap.get(ip);
                        if(!this.comparator.compare(remote.getType().getTotalCount(),count)){
                            System.out.println(remote.getApp()+":"+ip+" count1 as :"+remote.getType().getTotalCount()
                                    +" count2 as :"+count);
                            tmpMap.remove(ip);
                        }
                    }else{
                        tmpMap.put(ip,remote.getType().getTotalCount());
                    }
                }
            }
        }
    }
    class AnalyzeResult{
        private long hostAccessCount;
        private Map<String,Long> projectsAccessCount;
        private String ip ;
        AnalyzeResult(Local local){
            this.ip = local.getId();
            this.projectsAccessCount = new HashMap<String,Long>();
            Map<String,Remote> remoteMap = local.getRemotes();
            for(Iterator<String> iterator = remoteMap.keySet().iterator();iterator.hasNext();){
                Remote remote = remoteMap.get(iterator.next());
                if(remote.getRole().equals("Pigeon.Caller")){
                    String  projectName = remote.getApp();
                    long count = remote.getType().getTotalCount();
                    hostAccessCount+=count;
                    addProjectAccess(projectName,count);
                }
            }
        }
        private void addProjectAccess(String projectName,long count){
            if(this.projectsAccessCount.containsKey(projectName)){
                long tmp = this.projectsAccessCount.get(projectName);
                this.projectsAccessCount.put(projectName,tmp+count);
            }else{
                this.projectsAccessCount.put(projectName,count);
            }
        }

        public long getHostAccessCount() {
            return hostAccessCount;
        }

        public void setHostAccessCount(long hostAccessCount) {
            this.hostAccessCount = hostAccessCount;
        }

        public Map<String, Long> getProjectsAccessCount() {
            return projectsAccessCount;
        }

        public void setProjectsAccessCount(Map<String, Long> projectsAccessCount) {
            this.projectsAccessCount = projectsAccessCount;
        }
    }



    private  void loadBalanceCheck(CrossReport crossReport){
        Map<String,Local> locals = crossReport.getLocals();
        Map<String,AnalyzeResult> results = new HashMap<String,AnalyzeResult>();
        for(Iterator<Local> iterator = locals.values().iterator();iterator.hasNext();){
            Local local = iterator.next();
            results.put(local.getId(),new AnalyzeResult(local));
        }
        Map<String,Long> serverFlowDistribute = new HashMap<String, Long>();
        Map<String,Map<String,Long>> clientFlowDistribute = new HashMap<String,Map<String,Long>>();
        Map<String,List<Long>> clientData = new LinkedHashMap<String, List<Long>>();
        for(Iterator<AnalyzeResult> iterator = results.values().iterator();iterator.hasNext();){
            AnalyzeResult analyzeResult = iterator.next();
            serverFlowDistribute.put(analyzeResult.ip,analyzeResult.getHostAccessCount());
            for(Iterator<String> projectNameIter = analyzeResult.getProjectsAccessCount().keySet().iterator();
                    projectNameIter.hasNext();) {
                String projectName = projectNameIter.next();
                if(clientFlowDistribute.containsKey(projectName))
                    clientFlowDistribute.get(projectName).put(analyzeResult.ip,
                            analyzeResult.getProjectsAccessCount().get(projectName));
                else {
                    Map<String, Long> data = new HashMap<String, Long>();
                    data.put(analyzeResult.ip, analyzeResult.getProjectsAccessCount().get(projectName));
                    clientFlowDistribute.put(projectName, data);
                }
            }
        }
        if(analyzer.balanceAnalysis(serverFlowDistribute.values())){
            ServerSkewMessage serverSkewMessage = new ServerSkewMessage();
            serverSkewMessage.setProjectName(this.projectName);
            serverSkewMessage.setFlowDistributed(serverFlowDistribute);
        }
        for(Iterator<String> iterator = clientFlowDistribute.keySet().iterator();iterator.hasNext();){
            String projectName = iterator.next();
            if(analyzer.balanceAnalysis(clientFlowDistribute.get(projectName).values())){
                ClientSkewMessage clientSkewMessage = new ClientSkewMessage();
                clientSkewMessage.setServerProjectName(this.projectName);
                clientSkewMessage.setClientProjectName(projectName);
                clientSkewMessage.setFlowDistributed(clientFlowDistribute.get(projectName));
            }
        }

    }
}
