package com.kalibyte.foundry.production.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ProductionNumberGenerator {

    private final AtomicInteger counter = new AtomicInteger(1);

    public synchronized String generate() {
        return "PROD-" + LocalDate.now() + "-" + counter.getAndIncrement();
    }
}
