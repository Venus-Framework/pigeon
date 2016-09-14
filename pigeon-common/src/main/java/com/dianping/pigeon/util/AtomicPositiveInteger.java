package com.dianping.pigeon.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author qi.yin
 *         2016/02/17  上午10:12.
 */
public class AtomicPositiveInteger extends Number {

    private static final long serialVersionUID = -1L;

    private final AtomicInteger i;

    public AtomicPositiveInteger() {
        i = new AtomicInteger();
    }

    public AtomicPositiveInteger(int initialValue) {
        i = new AtomicInteger(initialValue);
    }

    public final int getAndIncrement() {
        for (;;) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE ? 0 : current + 1);
            if (i.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    public final int getAndDecrement() {
        for (;;) {
            int current = i.get();
            int next = (current <= 0 ? Integer.MAX_VALUE : current - 1);
            if (i.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    public final int incrementAndGet() {
        for (;;) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE ? 0 : current + 1);
            if (i.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public final int decrementAndGet() {
        for (;;) {
            int current = i.get();
            int next = (current <= 0 ? Integer.MAX_VALUE : current - 1);
            if (i.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public final int get() {
        return i.get();
    }

    public final void set(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        i.set(newValue);
    }

    public final int getAndSet(int newValue) {
        if (newValue < 0) {
            throw new IllegalArgumentException("new value " + newValue + " < 0");
        }
        return i.getAndSet(newValue);
    }

    public final int getAndAdd(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta " + delta + " < 0");
        }
        for (;;) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta);
            if (i.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    public final int addAndGet(int delta) {
        if (delta < 0) {
            throw new IllegalArgumentException("delta " + delta + " < 0");
        }
        for (;;) {
            int current = i.get();
            int next = (current >= Integer.MAX_VALUE - delta + 1 ? delta - 1 : current + delta);
            if (i.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public final boolean compareAndSet(int expect, int update) {
        if (update < 0) {
            throw new IllegalArgumentException("update value " + update + " < 0");
        }
        return i.compareAndSet(expect, update);
    }

    public final boolean weakCompareAndSet(int expect, int update) {
        if (update < 0) {
            throw new IllegalArgumentException("update value " + update + " < 0");
        }
        return i.weakCompareAndSet(expect, update);
    }

    @Override
    public int intValue() {
        return i.intValue();
    }

    @Override
    public long longValue() {
        return i.longValue();
    }

    @Override
    public float floatValue() {
        return i.floatValue();
    }

    @Override
    public double doubleValue() {
        return i.doubleValue();
    }

    @Override
    public String toString() {
        return "AtomicPositiveInteger[ i= " + i + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AtomicPositiveInteger that = (AtomicPositiveInteger) o;

        return !(i != null ? !i.equals(that.i) : that.i != null);

    }

    @Override
    public int hashCode() {
        return i != null ? i.hashCode() : 0;
    }
}
