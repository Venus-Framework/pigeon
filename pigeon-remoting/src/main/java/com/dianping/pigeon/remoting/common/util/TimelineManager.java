package com.dianping.pigeon.remoting.common.util;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.dianping.pigeon.config.ConfigManager;
import com.dianping.pigeon.extension.ExtensionLoader;
import com.dianping.pigeon.log.LoggerLoader;
import com.dianping.pigeon.remoting.common.domain.InvocationSerializable;
import com.dianping.pigeon.util.ContextUtils;

/**
 * TODO log improvement TODO switch to turn on/off
 * 
 * @author enlight
 */
public class TimelineManager {

	public enum Phase {
		Start/* 0 */, ClientEncoded/* 1 */, ClientSent/* 2 */, ClientException/* 3 */, ServerReceived/* 4 */, ServerDecoded/* 5 */, ServerException/* 6 */, BusinessStart/* 7 */, BusinessEnd/* 8 */, ServerEncoded/* 9 */, ServerSent/* 10 */, ClientReceived/* 11 */, ClientDecoded/* 12 */, End/* 13 */
	};

	private static Logger logger = LoggerLoader.getLogger(TimelineManager.class);

	private static volatile ConcurrentHashMap<String, Timeline> sequenceMap = new ConcurrentHashMap<String, Timeline>();

	private static boolean enabled;
	private static boolean enabledLocalLog;
	private static long abnormalThreshold;
	private static long legacyThreshold;
	private static long lastRemoveTime;
	private static String localIp;

	static {
		ConfigManager config = ExtensionLoader.getExtension(ConfigManager.class);
		enabled = config.getBooleanValue("pigeon.timeline.log.enabled", true);
		enabledLocalLog = config.getBooleanValue("pigeon.timeline.locallog.enabled", false);
		abnormalThreshold = config.getLongValue("pigeon.timeline.abnormal.threshold", 50);
		legacyThreshold = config.getLongValue("pigeon.timeline.legacy.threshold", 60000);
		localIp = config.getLocalIp();
	}

	public static class Timeline {
		private long[] timeline;

		public Timeline() {
			timeline = new long[Phase.values().length];
		}

		public void time(Phase phase) {
			timeline[phase.ordinal()] = System.currentTimeMillis();
		}

		public void time(Phase phase, long timestamp) {
			timeline[phase.ordinal()] = timestamp;
		}

		public long[] getTimeline() {
			return timeline;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			long startTime = 0;
			for (int i = 0; i < timeline.length; i++) {
				if (timeline[i] == 0)
					continue;
				if (startTime == 0) {
					startTime = timeline[i];
					sb.append(i).append(':').append(startTime);
				} else {
					int delta = (int) (timeline[i] - startTime);
					sb.append(',').append(i).append(":+").append(delta);
				}
			}
			return sb.toString();
		}

	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean isEnabledLocalLog() {
		return enabledLocalLog;
	}

	public static String getLocalIp() {
		return localIp;
	}

	public static String getRemoteIp() {
		return (String) ContextUtils.getLocalContext("CLIENT_IP");
	}

	public static void time(InvocationSerializable message, String ip, Phase phase) {
		if (shouldTime(message)) {
			Timeline tl = _getTimeline(ip + message.getSequence());
			if (phase.ordinal() == 2 && tl.getTimeline()[0] == 0) {
				sequenceMap.remove(ip + message.getSequence());
			} else {
				tl.time(phase);
			}
		}
	}

	public static void time(InvocationSerializable message, String ip, Phase phase, long timestamp) {
		if (shouldTime(message)) {
			Timeline tl = _getTimeline(ip + message.getSequence());
			tl.time(phase, timestamp);
		}
	}

	private static boolean shouldTime(InvocationSerializable message) {
		return enabled && message.getMessageType() != Constants.MESSAGE_TYPE_HEART
				&& message.getMessageType() != Constants.MESSAGE_TYPE_HEALTHCHECK;
	}

	private static Timeline _getTimeline(String key) {
		Timeline tl = sequenceMap.get(key);
		if (tl == null) {
			tl = new Timeline();
			Timeline _tl = sequenceMap.putIfAbsent(key, tl);
			if (_tl != null) {
				tl = _tl;
			}
		}
		return tl;
	}

	public static Timeline getTimeline(InvocationSerializable message, String ip) {
		Timeline tl = sequenceMap.get(ip + message.getSequence());
		return tl;
	}

	public static Timeline tryRemoveTimeline(InvocationSerializable message, String ip) {
		Timeline tl = sequenceMap.get(ip + message.getSequence());
		if (tl != null && tl.getTimeline()[10] != 0) {
			sequenceMap.remove(ip + message.getSequence());
		}
		return tl;
	}

	public static Timeline removeTimeline(InvocationSerializable message, String ip) {
		Timeline tl = sequenceMap.remove(ip + message.getSequence());
		return tl;
	}

	public static boolean isAbnormalTimeline(InvocationSerializable message, String ip) {
		Timeline tl = sequenceMap.get(ip + message.getSequence());
		if (tl != null) {
			long[] timeline = tl.getTimeline();
			return (timeline[2] - timeline[1] > abnormalThreshold) || (timeline[5] - timeline[4] > abnormalThreshold)
					|| (timeline[10] - timeline[9] > abnormalThreshold)
					|| (timeline[12] - timeline[11] > abnormalThreshold);
		}
		return false;
	}

	public static synchronized void removeLegacyTimelines() {
		long now = System.currentTimeMillis();
		if (now - lastRemoveTime < legacyThreshold) {
			return;
		}
		int count = 0;
		Iterator<Entry<String, Timeline>> it = sequenceMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Timeline> entry = it.next();
			if (isLegacyTimeline(entry.getValue(), lastRemoveTime)) {
				it.remove();
				count++;
			}
		}
		if (count > 0) {
			logger.info(String.format("removed %d legacy timelines", count));
		}
		lastRemoveTime = now;
	}

	private static boolean isLegacyTimeline(Timeline timeline, long threshold) {
		long[] tl = timeline.getTimeline();
		for (int i = 0; i < tl.length; i++) {
			if (tl[i] != 0) {
				return tl[i] < threshold;
			}
		}
		return true;
	}
}
