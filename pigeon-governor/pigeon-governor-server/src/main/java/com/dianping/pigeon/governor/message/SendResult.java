package com.dianping.pigeon.governor.message;

import com.dianping.pigeon.governor.util.GsonUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by shihuashen on 16/7/27.
 */
public class SendResult {
    private List<Tuple> tuples;
    public SendResult(){
        this.tuples = new LinkedList<Tuple>();
    }

    public List<Tuple> getTuples(){
        return this.tuples;
    }

    public SendResult add(SendResult result){
        this.getTuples().addAll(result.getTuples());
        return this;
    }

    public void add(String address,SenderType type,boolean isSuccess){
        this.getTuples().add(new Tuple(address,type,isSuccess));
    }
    public String toString(){
        return GsonUtils.prettyPrint(GsonUtils.toJson(this,false),false);
    }

    public class Tuple{
        private String address;
        private SenderType type;
        private boolean isSuccess;
        public Tuple(String address,SenderType type,boolean isSuccess){
            this.address = address;
            this.type = type;
            this.isSuccess = isSuccess;
        }
        public String toString(){
            return address+" "+type+" "+(isSuccess?"success":"fail");
        }
        public boolean isSuccess(){
            return this.isSuccess;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public SenderType getType() {
            return type;
        }

        public void setType(SenderType type) {
            this.type = type;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }
    }
}
