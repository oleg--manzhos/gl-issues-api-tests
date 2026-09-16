package com.abnamro.assignment.utils;

import java.time.LocalDate;

public class DateUtil {
    public static String selectDateFromNow(int days) {
        return LocalDate.now().plusDays(days).toString();
    }
}
