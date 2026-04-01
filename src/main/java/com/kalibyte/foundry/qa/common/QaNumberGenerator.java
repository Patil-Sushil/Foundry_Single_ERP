package com.kalibyte.foundry.qa.common;

import com.kalibyte.foundry.qa.inspection.repository.QaInspectionRepository;
import com.kalibyte.foundry.qa.rejection.repository.QaRejectionRepository;
import com.kalibyte.foundry.qa.customerreturn.repository.CustomerReturnRepository;
import com.kalibyte.foundry.scrap.repository.ScrapEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class QaNumberGenerator {

    private final QaInspectionRepository inspectionRepo;
    private final QaRejectionRepository rejectionRepo;
    private final CustomerReturnRepository returnRepo;
    private final ScrapEntryRepository scrapRepo;

    public synchronized String generateInspectionNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "QAI-" + year + "-";
        long count = inspectionRepo.countByInspectionNumberStartingWith(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }

    public synchronized String generateRejectionNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "QAR-" + year + "-";
        long count = rejectionRepo.countByRejectionNumberStartingWith(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }

    public synchronized String generateReturnNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "CR-" + year + "-";
        long count = returnRepo.countByReturnNumberStartingWith(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }

    public synchronized String generateScrapNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "SCR-" + year + "-";
        long count = scrapRepo.countByScrapNumberStartingWith(prefix);
        return String.format("%s%04d", prefix, count + 1);
    }
}
