package com.dianping.pigeon.remoting.invoker.route.balance;

import java.math.BigInteger;
import java.security.MessageDigest;

import com.dianping.pigeon.remoting.invoker.Client;

public class ClientHashFunction implements HashFunction {

	public Integer hash(Object client) {

		return ((Client) client).getAddress().hashCode();
	}

	@Override
	public Integer hash(String string) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(string.getBytes());
			byte[] digest = m.digest();
			BigInteger bigInt = new BigInteger(1, digest);
			int hash = bigInt.intValue();
			return hash;
		} catch (Exception e) {
			// System.err.println("NO md5");
			return string.hashCode();
		}
	}

}
