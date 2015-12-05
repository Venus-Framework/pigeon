package com.dianping.piegon.governor.test;

import com.dianping.pigeon.console.Utils;
import com.dianping.pigeon.governor.bean.Result;
import com.dianping.pigeon.governor.util.RestCallUtils;
import com.dianping.pigeon.threadpool.NamedThreadFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenchongze on 15/12/4.
 */
public class ServicePublish {

    static Set<String> ipSet = new HashSet<String>();

    private static ExecutorService workThreadPool = new ThreadPoolExecutor(1000, 2000, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10000),
            new NamedThreadFactory("Pigeon-Publish-Online"),
            new ThreadPoolExecutor.CallerRunsPolicy());

    public static void main(String[] args){
        readFileByLines("/data/appdatas/betaip.txt");
        for(final String ip : ipSet){

            //TODO publish and Online
            workThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    Result result = onClickAdd(ip,"4080");
                    System.out.println(result.getMessage());
                }
            });

        }

        await();
    }

    private static void await() {
        workThreadPool.shutdown();
        try {
            workThreadPool.awaitTermination(Integer.MAX_VALUE,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workThreadPool.shutdownNow();
        }
    }

    private static Result onClickAdd(String ip,String port) {
        Result result = null;
        String urlBase = "http://" + ip + ":" + port + "/services";
        String publishUrl = urlBase + ".publish?force=true&sign=" + Utils.getSign();
        String publishResult = RestCallUtils.getRestCall(publishUrl,String.class,3000,3000);

        if(publishResult != null && publishResult.startsWith("ok")) {
            String onlineUrl = urlBase + ".online?force=true&sign=" + Utils.getSign();
            String onlineResult = RestCallUtils.getRestCall(onlineUrl,String.class,3000,3000);

            if(onlineResult != null && onlineResult.startsWith("ok")) {
                result = Result.createSuccessResult("");
            } else {
                result = Result.createErrorResult("服务权重注册失败，建议重试");
            }

        } else {
            result = Result.createErrorResult("http call error or no services found: " + urlBase);
        }

        return result;
    }

    public static void readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            System.out.println("以行为单位读取文件内容，一次读一行，读取开始");
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            //一次读一行，读入null时文件结束
            while ((tempString = reader.readLine()) != null) {
                //把当前行号显示出来
                //System.out.println("line " + line + ": " + tempString);
                //line++;
                ipSet.add(tempString);
            }
            System.out.println("以行为单位读取文件内容，一次读一行，读取完毕");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }
}
