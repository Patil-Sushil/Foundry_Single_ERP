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

    @Transactional
    public String generate() {
        int year = LocalDate.now().getYear();
        long count = materialIssueRepository.countByYear(year);
        String issueNumber;
        do {
            count++;
            issueNumber = String.format("ISS-%d-%04d", year, count);
        } while (materialIssueRepository.existsByIssueNumber(issueNumber));
        return issueNumber;
    }
}
