package com.amtkxa.common.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.amtkxa.common.exception.BadRequestException;

public class DateUtil {
    private static List<String> patterns = Arrays.asList(
            "yyyy-MM-dd HH:mm:ss[.SSS]",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyyMMdd"
    );

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
            patterns.stream().collect(Collectors.joining("][", "[", "]"))
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
            return parseStartOfDateTime(datetime);
        }
    }

    public static Timestamp parseStartOfDateTime(String datetime) {
        try {
            return Timestamp.valueOf(LocalDate.parse(datetime, formatter).atStartOfDay());
        } catch (DateTimeParseException e) {
            throw new BadRequestException(
                    "Invalid datetime in the request.　Text " + datetime + " could not be parsed. " +
                    "The supported format is " + patterns.toString());
        }
    }

    /**
     * Return the current time in {@code Timestamp}.
     *
     * @return current timestamp value.
     */
    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }
}
