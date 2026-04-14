package net.omalkin.hungernite.util;

import java.util.EnumSet;

public class EnumTranscoder {
    public static <E extends Enum<E>> int encode(EnumSet<E> set) {
        int mask = 0;
        for (E value : set) {
            mask |= (1 << value.ordinal());
        }
        return mask;
    }

    public static <E extends Enum<E>> EnumSet<E> decode(int mask, Class<E> enumClass) {
        EnumSet<E> set = EnumSet.noneOf(enumClass);
        E[] values = enumClass.getEnumConstants();

        for (E value : values) {
            if ((mask & (1 << value.ordinal())) != 0) {
                set.add(value);
            }
        }

        return set;
    }
}
