package com.pes.gadgetrepair.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 Utility Class

 Purpose:
 Provides common date formatting functions used
 across the application.

 SOLID Principles:
 Single Responsibility Principle
*/

public final class DateUtil {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {}

    public static String formatDateTime(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.format(FORMATTER);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}