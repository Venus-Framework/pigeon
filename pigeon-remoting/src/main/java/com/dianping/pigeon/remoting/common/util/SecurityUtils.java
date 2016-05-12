package com.dianping.pigeon.remoting.common.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.dianping.pigeon.remoting.common.exception.SecurityException;

public class SecurityUtils {

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	/**
	 * hmac_sha1加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static String encrypt(String data, String key) throws SecurityException {
		String result;
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			result = Base64.encodeBase64URLSafeString(rawHmac);
		} catch (Exception e) {
			throw new SecurityException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}

}