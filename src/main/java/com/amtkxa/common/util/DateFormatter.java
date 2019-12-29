package com.amtkxa.common.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatter {
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            "[yyyy-MM-dd HH:mm:ss[.SSS]]" +
            "[yyyy-MM-dd]" +
            "[yyyy/MM/dd]" +
            "[yyyyMMdd]"
    );

    /**
     * Obtains an instance of {@code Timestamp} from a String date-time.
     * <p>
     * If no time exists in the given date-time,
     * Treats the local date-time of midnight at the start of this date-time.
     *
     * @param datetime String value of date-time.
     * @return the parsed timestamp value.
     */
    public static Timestamp parse(String datetime) {
        try {
            return Timestamp.valueOf(LocalDateTime.parse(datetime, formatter));
        } catch (DateTimeParseException e) {
            return Timestamp.valueOf(LocalDate.parse(datetime, formatter).atStartOfDay());
        }
    }
}
