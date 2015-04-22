package com.dianping.pigeon.remoting.common.codec.fst;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import de.ruedigermoeller.serialization.FSTBasicObjectSerializer;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTClazzInfo.FSTFieldInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class FSTBigDecimalSerializer extends FSTBasicObjectSerializer {

	@Override
	public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTFieldInfo referencedBy,
			int streamPosition) throws IOException {
		BigDecimal value = (BigDecimal) toWrite;
		out.writeInt(value.scale());
		byte[] unscaledValue = ((BigInteger) value.unscaledValue()).toByteArray();
		out.writeInt(unscaledValue.length);
		out.write(unscaledValue);
	}

	@Override
	public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
			FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		int scale = in.readInt();
		byte[] buf = new byte[in.readInt()];
		in.read(buf);
		BigInteger unscaledValue = new BigInteger(buf);
		BigDecimal value = new BigDecimal(unscaledValue, scale);
		in.registerObject(value, streamPosition, serializationInfo, referencee);
		return value;
	}
}
