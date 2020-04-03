package com.study.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.util.stream.IntStream;

class SimpleTest {

    private static Logger LOGGER = LoggerFactory.getLogger(SimpleTest.class);

    private final static Integer START_NUMBER = 0;
    private final static Integer END_NUMBER = 200000;

    private StopWatch stopWatch;

    @BeforeEach
    void setUp() {
        stopWatch = new StopWatch("system / logback - stopwatch");
    }

    @Test
    void systemOutPrintTest() {
        stopWatch.start();
        // 1.949
        IntStream.range(START_NUMBER, END_NUMBER).forEach((i) -> System.out.println("[2020-04-03 14:26:02] [DEBUG] c.s.s.SimpleTest.test[14] " + i));
        stopWatch.stop();
    }

    @Test
    void logbackTest() {
        stopWatch.start();
        // consoleAppender 27.522
        // fileAppender 24.737
        // async + consoleAppender 0.458
        // async + fileAppender 0.263
        IntStream.range(START_NUMBER, END_NUMBER).forEach((i) -> LOGGER.debug(i + ""));
        stopWatch.stop();
    }

    @AfterEach
    void after() {
        System.out.println(stopWatch.getTotalTimeSeconds());
    }
}
