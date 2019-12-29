package com.amtkxa.common.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Timestamp;

import org.junit.jupiter.api.Test;

public class DateUtilTest {

    @Test
    public void parse() {
        assertAll("Check parsing from String to Timestamp.",
                  () -> assertEquals(Timestamp.valueOf("2019-12-05 00:00:00.000000000"),
                                     DateUtil.parse("2019-12-05 00:00:00")),
                  () -> assertEquals(Timestamp.valueOf("2019-12-05 12:30:45.123000000"),
                                     DateUtil.parse("2019-12-05 12:30:45.123")),
                  // If no time exists in the given date-time,
                  // Treats the local date-time of midnight at the start of this date-time.
                  () -> assertEquals(Timestamp.valueOf("2019-12-05 00:00:00.000000000"),
                                     DateUtil.parse("2019-12-05")),
                  () -> assertEquals(Timestamp.valueOf("2019-12-05 00:00:00.000000000"),
                                     DateUtil.parse("2019/12/05"))
        );
    }
}
