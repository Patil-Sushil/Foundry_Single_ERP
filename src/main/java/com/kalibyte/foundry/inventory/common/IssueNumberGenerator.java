package com.kalibyte.foundry.inventory.common;

import com.kalibyte.foundry.inventory.issue.repository.MaterialIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class IssueNumberGenerator {

    private final MaterialIssueRepository materialIssueRepository;

    @Transactional(readOnly = true)
    public String generate() {
        int year = LocalDate.now().getYear();
        long count = materialIssueRepository.countByYear(year);
        return String.format("ISS-%d-%04d", year, count + 1);
    }
}
