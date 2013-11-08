/**
 * Dianping.com Inc.
 * Copyright (c) 2003-${year} All Rights Reserved.
 */
package com.dianping.pigeon.remoting.netty.codec;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

import com.dianping.pigeon.component.invocation.InvocationResponse;
import com.dianping.pigeon.component.invocation.InvocationSerializable;
import com.dianping.pigeon.monitor.Log4jLoader;
import com.dianping.pigeon.remoting.common.util.Constants;
import com.dianping.pigeon.remoting.common.util.ResponseUtils;
import com.dianping.pigeon.serialize.SerializerFactory;

public abstract class AbstractEncoder extends OneToOneEncoder implements Encoder {

	private static final Logger log = Log4jLoader.getLogger(AbstractEncoder.class);

	public Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg instanceof InvocationSerializable) {
			InvocationSerializable message = (InvocationSerializable) msg;
			try {
				ChannelBuffer buffer = (ChannelBuffer) _encode(message.getSerializ(), ctx, channel, message.getObject());
				buffer.setBytes(0, Constants.MESSAGE_HEAD);
				buffer.setByte(2, message.getSerializ());
				buffer.readerIndex(0);
				return buffer;
			} catch (Exception e) {
				doFailResponse(channel,
						ResponseUtils.createThrowableResponse(message.getSequence(), message.getSerializ(), e));

				log.error(e.getMessage(), e);
				throw e;
			}
		} else if (msg instanceof String) {
			byte[] msgBytes = ((String) msg).getBytes(Constants.TELNET_CHARSET);
			ChannelBuffer cb = ChannelBuffers.buffer(msgBytes.length);
			cb.writeBytes(msgBytes);
			return cb;
		} else {
			throw new IllegalArgumentException("invalid message format");
		}
	}

	public abstract void doFailResponse(Channel channel, InvocationResponse response);

	private final int estimatedLength = 512;

	public Object _encode(byte serializerType, ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		ChannelBufferOutputStream bout = new ChannelBufferOutputStream(dynamicBuffer(estimatedLength, ctx.getChannel()
				.getConfig().getBufferFactory()));
		beforeDo(bout);
		SerializerFactory.getSerializer(serializerType).serialize(bout, msg);
		ChannelBuffer encoded = bout.buffer();
		afterDo(encoded, msg);
		return encoded;
	}

	protected static final byte[] LENGTH_PLACEHOLDER = new byte[7];

	// +3是在结尾写入扩展表示EXPEND_FLAG
	// +8是由于后面要写入long类型的扩展字段seq
	public static final int EXPAND_LANGTH = 3 + 8;

	protected void beforeDo(Object buffer) throws IOException {
		if (buffer instanceof ChannelBufferOutputStream) {
			ChannelBufferOutputStream buffer_ = (ChannelBufferOutputStream) buffer;
			buffer_.write(LENGTH_PLACEHOLDER);
		} else if (buffer instanceof ChannelBuffer) {
			ChannelBuffer buffer_ = (ChannelBuffer) buffer;
			buffer_.writeBytes(LENGTH_PLACEHOLDER);
		}

	}

	protected void afterDo(ChannelBuffer cb, Object msg) {

		// -7是由于减去占位符的长度，
		cb.setInt(3, cb.writerIndex() - 7 + EXPAND_LANGTH);
		expand(cb, msg);
	}

	private void expand(ChannelBuffer cb, Object msg) {
		// 将msg的seq写入序列化外的数据流中，便于在序列化出问题是使用
		cb.writeLong(getSeq(msg));
		cb.writeBytes(Constants.EXPAND_FLAG);
	}

	private long getSeq(Object msg) {
		long seq = 0;
		if (msg instanceof InvocationSerializable) {
			InvocationSerializable msg_ = (InvocationSerializable) msg;
			seq = msg_.getSequence();
		}
		return seq;
	}
}
