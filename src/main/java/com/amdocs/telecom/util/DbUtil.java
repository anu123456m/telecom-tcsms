package com.amdocs.telecom.util;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class DbUtil {

    private DbUtil() {
    }

    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }

    public static Timestamp toTimestamp(LocalDateTime dt) {
        return dt == null ? null : Timestamp.valueOf(dt);
    }
}