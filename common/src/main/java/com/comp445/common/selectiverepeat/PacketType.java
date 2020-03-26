package com.comp445.common.selectiverepeat;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum PacketType {
    DATA((byte)0), ACK((byte)1), NACK((byte)2), SYN((byte)3), SYNACK((byte)4);
    private final byte value;

    private static final Map<Byte, PacketType> BY_BYTE_VALUE = new HashMap<>();

    static {
        for (PacketType e: values()) {
            BY_BYTE_VALUE.put(e.value, e);
        }
    }

    public static PacketType fromByteValue(byte value) {
        return BY_BYTE_VALUE.get(value);
    }
}
