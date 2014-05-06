package com.dianping.pigeon.governor.task;

public class Constants { 
    public enum Environment {
        test("127.0.0.1:2181"),
        dev("192.168.7.41:2181"),
        alpha("192.168.7.41:2182"),
        qa("192.168.213.144:2181"),
        prelease("10.2.8.143:2181"),
        product("10.1.2.32:2181,10.1.2.37:2181,10.1.2.62:2181,10.1.2.67:2181,10.1.2.58:2181"),
        performance("192.168.219.211:2181");
        
        private String zkAddress;
    
        private Environment(String zkAddress) {
            this.zkAddress = zkAddress;
        }
        
        public String getZkAddress() {
            return this.zkAddress;
        }
    }

    public enum Action {
        none, log, remove;
    }
}