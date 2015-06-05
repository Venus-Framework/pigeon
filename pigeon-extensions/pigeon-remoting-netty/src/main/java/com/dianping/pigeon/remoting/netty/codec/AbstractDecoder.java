/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.codec;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.dianping.pigeon.log.LoggerLoader;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.DynamicChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.util.DebugUtil;

import com.dianping.pigeon.remoting.common.domain.InvocationResponse;
import com.dianping.pigeon.remoting.common.exception.SerializationException;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.provider.util.ProviderUtils;

public abstract class AbstractDecoder extends OneToOneDecoder implements Decoder {

	private static final Logger logger = LoggerLoader.getLogger(AbstractDecoder.class);

	public abstract Object doInitMsg(Object message, Channel channel, long receiveTime);

	public abstract void doFailResponse(Channel channel, InvocationResponse response);

	public abstract Object deserialize(byte serializerType, InputStream is);

	public abstract String getEventName();

	protected boolean isNettyTimelineEnabled = true;

	public AbstractDecoder() {
		try {
			DebugUtil.getTimestamp();
		} catch (NoSuchMethodError e) {
			isNettyTimelineEnabled = false;
		}
	}

	public Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws IOException,
			ClassNotFoundException {
		long receiveTime = System.currentTimeMillis();
		if (!(msg instanceof ChannelBuffer)) {
			return msg;
		}

		ChannelBuffer cb = (ChannelBuffer) NettyCodecUtils.getAttachment(ctx, Constants.ATTACHMENT_BYTEBUFFER);
		ChannelBuffer cb_ = (ChannelBuffer) msg;
		if (cb == null) {
			cb = cb_;
		} else {
			cb.writeBytes(cb_);
		}

//		if (SizeMonitor.isEnable()) {
//			int size = cb_.readableBytes();
//			String ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
//			SizeMonitor.getInstance().logSize(size, getEventName(), ip);
//		}

		List<Object> messages = null;
		int lastReadIndex = cb.readerIndex();
		while (cb.readable()) {
			if (cb.readableBytes() <= 3) {
				setAttachment(ctx, channel, cb, lastReadIndex);
				break;
			}
			byte head = cb.readByte();
			if (!(head == Constants.MESSAGE_HEAD_FIRST)) {
				throw new IllegalArgumentException("decode error with invalid message head:" + head + ", message:"
						+ msg);
			}
			head = cb.readByte();
			if (!(head == Constants.MESSAGE_HEAD_SECOND)) {
				throw new IllegalArgumentException("decode error with invalid message head:" + head + ", message:"
						+ msg);
			}

			byte serializable = cb.readByte();
			boolean isException = false;
			Object message = null;
			try {
				message = _decode(serializable, ctx, channel, cb);
			} catch (Throwable e) {
				isException = true;
				SerializationException se = new SerializationException(e);
				try {
					// 解析对端encoder扩展的seq字段
					Object seqObj = NettyCodecUtils.getAttachment(ctx, Constants.ATTACHMENT_REQUEST_SEQ);
					if (seqObj != null) {
						long seq = Long.parseLong(String.valueOf(seqObj));
						String errorMsg = "Deserialize Exception>>>>host:"
								+ ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress()
								+ " seq:" + seq + "\n" + e.getMessage();
						logger.error(errorMsg, se);
						doFailResponse(channel, ProviderUtils.createThrowableResponse(seq, serializable, se));
					}
				} catch (Throwable e1) {
					logger.error("", e1);
				}
			}
			if (message != null) {
				if (messages == null) {
					messages = new ArrayList<Object>();
				}
				messages.add(doInitMsg(message, channel, receiveTime));
				lastReadIndex = cb.readerIndex();
			} else if (isException) {
				lastReadIndex = cb.readerIndex();
			} else {
				setAttachment(ctx, channel, cb, lastReadIndex);
				break;
			}
		}

		return messages;
	}

	private void setAttachment(ChannelHandlerContext ctx, Channel channel, ChannelBuffer cb, int lastReadIndex) {
		cb.readerIndex(lastReadIndex);
		if (!(cb instanceof DynamicChannelBuffer) || cb.writerIndex() > 102400) {
			ChannelBuffer db = dynamicBuffer(cb.readableBytes() * 2, channel.getConfig().getBufferFactory());
			db.writeBytes(cb);
			cb = db;
		}

		NettyCodecUtils.setAttachment(ctx, Constants.ATTACHMENT_BYTEBUFFER, cb);
	}

	public Object _decode(byte serializerType, ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer) msg;
		ChannelBuffer frame = beforeDo(ctx, buffer);
		if (frame == null) {
			return null;
		}

		return deserialize(serializerType, new ChannelBufferInputStream(frame));
	}

	private final static int fieldLenth = 4;

	protected ChannelBuffer beforeDo(ChannelHandlerContext ctx, ChannelBuffer buffer) {

		if (buffer.readableBytes() < fieldLenth) {
			return null;
		}

		long frameLength = buffer.getUnsignedInt(buffer.readerIndex());

		// never overflows because it's less than maxFrameLength
		int frameLengthInt = (int) frameLength;
		if (buffer.readableBytes() - fieldLenth < frameLengthInt) {
			return null;
		}

		buffer.skipBytes(fieldLenth);

		// extract frame
		int readerIndex = buffer.readerIndex();
		int msgLen = parseExpand(ctx, buffer, frameLengthInt);
		ChannelBuffer frame = buffer.slice(readerIndex, msgLen);
		buffer.readerIndex(readerIndex + frameLengthInt);

		return frame;
	}

	/**
	 * 
	 * 
	 * @param ctx
	 * @param buffer
	 * @param frameLengthInt
	 * @return
	 */
	private int parseExpand(ChannelHandlerContext ctx, ChannelBuffer buffer, int frameLengthInt) {
		int msgLen = frameLengthInt;
		byte[] expandFlag = new byte[3];
		buffer.getBytes(buffer.readerIndex() + frameLengthInt - 3, expandFlag);
		if (expandFlag[0] == Constants.EXPAND_FLAG_FIRST && expandFlag[1] == Constants.EXPAND_FLAG_SECOND
				&& expandFlag[2] == Constants.EXPAND_FLAG_THIRD) {
			msgLen = frameLengthInt - AbstractEncoder.EXPAND_LANGTH;
			long seq = buffer.getLong(buffer.readerIndex() + msgLen);
			NettyCodecUtils.setAttachment(ctx, Constants.ATTACHMENT_REQUEST_SEQ, seq);
		}

		return msgLen;
	}
}
