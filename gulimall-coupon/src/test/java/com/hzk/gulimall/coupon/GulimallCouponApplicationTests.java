package com.hzk.gulimall.coupon;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime startDate = LocalDateTime.of(now, min);
        String start = startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDate now1 = LocalDate.now();
        LocalDate localDate = now1.plusDays(2);
        LocalTime MAX = LocalTime.MAX;
        LocalDateTime endDate = LocalDateTime.of(localDate, MAX);
        String end = endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println(start);
        System.out.println(end);
    }

}
