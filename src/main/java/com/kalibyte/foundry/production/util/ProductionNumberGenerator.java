package com.kalibyte.foundry.production.util;

import com.kalibyte.foundry.production.repository.ProductionEntryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class ProductionNumberGenerator {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ProductionEntryRepository entryRepo;

    public synchronized String generate() {

        String dateStr = LocalDate.now().format(FMT);
        String prefix = "PROD-" + dateStr + "-";

        // count today's entries (including deleted — avoids number reuse)
        long count = entryRepo.countByEntryNumberStartingWith(prefix);

        return String.format("%s%04d", prefix, count + 1);
    }
}