LD_LIBRARY_PATH="/opt/jprofiler6/bin/linux-x64"

export LD_LIBRARY_PATH

#nohup java -server -Xms128m -Xmx256m -agentlib:jprofilerti=port=8849  -Xbootclasspath/a:/opt/jprofiler6/bin/agent.jar -cp .:lib/* com.dianping.pigeon.test.client.benchmark.call.DefaultTest  > ./invoker.log &

nohup java -server -Xms128m -Xmx256m -cp .:lib/*  com.dianping.pigeon.test.client.benchmark.call.DefaultTest  > ./invoker.log &