package com.dianping.pigeon.governor.util;

/**
 * Created by chenchongze on 15/11/11.
 */
public enum OpType {

    CREATE_PIGEON_SERVICE(1),
    UPDATE_PIGEON_SERVICE(2),
    DELETE_PIGEON_SERVICE(3),
    PICK_OFF_PROVIDER_HEARTBEAT(4),
    PROJECT_INFO_UPDATE(5),
    SERVICE_DEGRADE(6);


    private Short value;

    private OpType(int value){
        this.value = (short) value;
    }

    public Short getValue(){
        return value;
    }
}
