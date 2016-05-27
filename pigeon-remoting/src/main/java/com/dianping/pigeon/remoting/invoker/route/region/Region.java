package com.dianping.pigeon.remoting.invoker.route.region;

/**
 * Created by chenchongze on 16/2/22.
 */
public class Region implements Comparable<Region> {

    private final String name;

    // 优先级，0为local，剩下依次递增1，2，3
    private final int priority;

    private final int weight;

    public Region(String name, int priority, int weight) {
        this.name = name;
        this.priority = priority;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public int compareTo(Region anotherRegion) {
        if(anotherRegion != null) {
            Integer thisVal = this.getPriority();
            Integer anotherVal = anotherRegion.getPriority();
            return thisVal.compareTo(anotherVal);
        } else {
            throw new NullPointerException("Region parameter cannot be null!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Region region = (Region) o;

        if (priority != region.priority) return false;
        if (weight != region.weight) return false;
        if (name != null ? !name.equals(region.name) : region.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + priority;
        result = 31 * result + weight;
        return result;
    }

    @Override
    public String toString() {
        return "Region{" +
                "name='" + name + '\'' +
                ", priority=" + priority +
                ", weight=" + weight +
                '}';
    }
}
