package com.kalibyte.foundry.labors.labor.service;

import com.kalibyte.foundry.labors.labor.dto.LaborerRequestDTO;
import com.kalibyte.foundry.labors.labor.entity.Enum.WageType;
import com.kalibyte.foundry.labors.labor.exception.LaborException;
import com.kalibyte.foundry.labors.labor.mapper.LaborerMapper;
import com.kalibyte.foundry.labors.labor.repository.LaborerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class LaborerServiceTest {

    @Mock
    private LaborerRepository laborerRepository;

    @Mock
    private LaborerMapper laborerMapper;

    @InjectMocks
    private LaborerService laborerService;

    private LaborerRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new LaborerRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setWageType(WageType.DAILY);
        // phNumber is null by default
    }

    @Test
    void createLaborer_WhenPhNumberIsNull_ShouldThrowLaborException() {
        assertThrows(LaborException.class, () -> {
            laborerService.createLaborer(requestDTO);
        });
    }
}
