package com.dianping.pigeon.registry;

/**
 * Created by chenchongze on 16/2/22.
 */
public class Region {

    private final String name;

    // 优先级，0为local，剩下依次递增1，2，3
    private final Integer priority;

    public Region(String name, Integer priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public Integer getPriority() {
        return priority;
    }
}
