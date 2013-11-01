#!/bin/bash

export CLASSPATH=.
for jarlib in `ls ./lib/*.jar`
do
  CLASSPATH=$CLASSPATH:$jarlib
done
export CLASSPATH=$CLASSPATH

nohup java -server -Xms128m -Xmx256m  -classpath $CLASSPATH  com.dianping.pigeon.test.client_1.x.benchmark.call.DefaultTest  > ./invoker.log &
