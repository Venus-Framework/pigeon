package com.dianping.pigeon.governor.message.impl;

import com.dianping.pigeon.governor.message.Event;
import com.dianping.pigeon.governor.message.EventChannel;
import com.dianping.pigeon.governor.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by shihuashen on 16/7/20.
 */
public class ChannelHandler {
    private int threadNum;
    private EventChannel eventChannel;
    private volatile boolean isStopped = false;
    private ExecutorService executor = null;
    private Thread channelManagerThread;
    //TODO refactor as interface.
    private MessageServiceImpl messageService;
    public void init(int threadNum,EventChannel eventChannel,MessageServiceImpl messageService) throws Exception {
        this.threadNum = threadNum;
        this.eventChannel = eventChannel;
        this.messageService = messageService;
        executor = Executors.newFixedThreadPool(this.threadNum);
        isStopped = false;
    }
    public void doStart() throws Exception {
        this.channelManagerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startChannelHandle();
            }
        });
        this.channelManagerThread.setDaemon(true);
        this.channelManagerThread.start();
    }

    private void startChannelHandle(){
        while(!checkStop()){
            Event event = null;
            try{
                event = eventChannel.take();
                //TODO
                executor.submit(messageService.getEventLifeCycle(event));
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private boolean checkStop() {
        return isStopped || Thread.currentThread().isInterrupted();
    }


    public void doStop() throws Exception {
        isStopped = true;
        this.channelManagerThread.interrupt();
    }
}
