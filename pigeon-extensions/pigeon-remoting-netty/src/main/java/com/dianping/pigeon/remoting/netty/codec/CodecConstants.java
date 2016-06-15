package com.dianping.pigeon.remoting.netty.codec;

/**
 * @author qi.yin
 *         2016/05/11  上午12:12.
 */
public class CodecConstants {


    public static final int MEGIC_FIELD_LENGTH = 2;
    public static final int BODY_FIELD_LENGTH = 4;
    public static final int HEAD_LENGTH = 3;
    public static final int SEQ_FIELD_LENGTH = 8;
    public static final int EXPAND_FIELD_LENGTH = 3;
    public static final int TAIL_LENGTH = SEQ_FIELD_LENGTH + EXPAND_FIELD_LENGTH;


    public static final int FRONT_LENGTH = HEAD_LENGTH + BODY_FIELD_LENGTH;
    public static final int FRAME_LENGTH = HEAD_LENGTH + BODY_FIELD_LENGTH + TAIL_LENGTH;

    public static final int _MEGIC_FIELD_LENGTH = 2;
    public static final int _VERSION_FIELD_LENGTH = 1;

    public static final int _HEAD_LENGTH = 4;
    public static final int _HEAD_FIELD_LENGTH = 2;
    public static final int _TOTAL_FIELD_LENGTH = 4;
    public static final int _TAIL_LENGTH = 4;

    public static final int _FRONT_COMMAND_LENGTH = _MEGIC_FIELD_LENGTH + _VERSION_FIELD_LENGTH;
    public static final int _FRONT_LENGTH = _HEAD_LENGTH + _TOTAL_FIELD_LENGTH + _HEAD_FIELD_LENGTH;
    public static final int _FRONT_LENGTH_ = _HEAD_LENGTH + _TOTAL_FIELD_LENGTH;


    public static final byte MAGIC_FIRST = 0x39;
    public static final byte MAGIC_SECEND = 0x3A;
    public static final byte[] MAGIC = new byte[]{MAGIC_FIRST, MAGIC_SECEND};

    public static final byte EXPAND_FIRST = 0x1D;
    public static final byte EXPAND_SECOND = 0x1E;
    public static final byte EXPAND_THIRD = 0x1F;
    public static final byte[] EXPAND = new byte[]{EXPAND_FIRST, EXPAND_SECOND, EXPAND_THIRD};


    public static final byte _MAGIC_FIRST = (byte) 0xAB;
    public static final byte _MAGIC_SECEND = (byte) 0xBA;
    public static final byte[] _MAGIC = new byte[]{_MAGIC_FIRST, _MAGIC_SECEND};


    public static final int ESTIMATED_LENGTH = 512;
}
