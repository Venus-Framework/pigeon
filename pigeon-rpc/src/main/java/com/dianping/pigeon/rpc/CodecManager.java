/**
 * Dianping.com Inc.
 * Copyright (c) 2003-2013 All Rights Reserved.
 */
package com.dianping.pigeon.rpc;

import com.dianping.dpsf.protocol.DefaultRequest;
import com.dianping.dpsf.protocol.DefaultResponse;

/**
 * @author xiangwu
 * @Sep 5, 2013
 * 
 */
public interface CodecManager {

	DefaultRequest decodeRequest(Object obj) throws CodecException;

	Object encodeRequest(DefaultRequest request) throws CodecException;

	DefaultResponse decodeResponse(Object obj) throws CodecException;

	Object encodeResponse(DefaultResponse response) throws CodecException;

}
