package com.dianping.pigeon.remoting.common.codec.fst;

import java.io.IOException;
import java.math.BigInteger;

import de.ruedigermoeller.serialization.FSTBasicObjectSerializer;
import de.ruedigermoeller.serialization.FSTClazzInfo;
import de.ruedigermoeller.serialization.FSTClazzInfo.FSTFieldInfo;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;

public class FSTBigIntegerSerializer extends FSTBasicObjectSerializer {

	@Override
	public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo, FSTFieldInfo referencedBy,
			int streamPosition) throws IOException {
		byte[] value = ((BigInteger) toWrite).toByteArray();
		out.writeInt(value.length);
		out.write(value);
	}

	@Override
	public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
			FSTFieldInfo referencee, int streamPosition) throws IOException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		byte[] buf = new byte[in.readInt()];
		in.read(buf);
		BigInteger result = new BigInteger(buf);
		in.registerObject(result, streamPosition, serializationInfo, referencee);
		return result;
	}
}
