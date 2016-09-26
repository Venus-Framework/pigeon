package com.dianping.pigeon.remoting.test

/**
 * Created by chenchongze on 16/9/21.
 */
class MyScript extends Script {

    Object customScript() {
        return new Person(['name':'ccz', 'age':20]);
    }

    @Override
    Object run() {
        println "myScript"
        return null
    }
}
