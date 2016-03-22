package com.dianping.pigeon.registry.region;

/**
 * Created by chenchongze on 16/2/22.
 */
public class Region {

    private final String name;

    // 优先级，0为local，剩下依次递增1，2，3
    private final int priority;

    public Region(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "Region{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;

        if (priority != region.priority) return false;
        if (!name.equals(region.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + priority;
        return result;
    }
}
