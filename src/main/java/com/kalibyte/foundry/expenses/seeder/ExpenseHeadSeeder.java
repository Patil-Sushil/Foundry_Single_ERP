package com.kalibyte.foundry.expenses.seeder;

import com.kalibyte.foundry.expenses.entity.ExpenseHead;
import com.kalibyte.foundry.expenses.entity.enums.ExpenseCategory;
import com.kalibyte.foundry.expenses.repository.ExpenseHeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseHeadSeeder implements CommandLineRunner {

    private final ExpenseHeadRepository expenseHeadRepository;

    @Override
    public void run(String... args) {

        if (expenseHeadRepository.count() > 0) {
            return;
        }

        List<ExpenseHead> heads = List.of(

                ExpenseHead.builder()
                        .name("Power & Fuel")
                        .category(ExpenseCategory.FACTORY_OVERHEAD)
                        .description("Electricity, gas, diesel used in production")
                        .build(),

                ExpenseHead.builder()
                        .name("Furnace Repair")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Maintenance and repair of furnace")
                        .build(),

                ExpenseHead.builder()
                        .name("Pattern Repair")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Repair of casting patterns")
                        .build(),

                ExpenseHead.builder()
                        .name("Shop Consumables")
                        .category(ExpenseCategory.FACTORY_OVERHEAD)
                        .description("PPE, welding rods, tools etc")
                        .build(),

                ExpenseHead.builder()
                        .name("Packing Material")
                        .category(ExpenseCategory.FACTORY_OVERHEAD)
                        .description("Packing material for dispatch")
                        .build(),

                ExpenseHead.builder()
                        .name("Freight")
                        .category(ExpenseCategory.FACTORY_OVERHEAD)
                        .description("Transport and freight charges")
                        .build(),

                ExpenseHead.builder()
                        .name("Office Rent")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Office rent expense")
                        .build(),

                ExpenseHead.builder()
                        .name("Telephone & Internet")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Communication expenses")
                        .build(),

                ExpenseHead.builder()
                        .name("Travel")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Employee travel expenses")
                        .build(),

                ExpenseHead.builder()
                        .name("Software Subscription")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Software tools and subscriptions")
                        .build(),

                ExpenseHead.builder()
                        .name("Professional Fees")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("CA, consultants, auditors")
                        .build(),

                ExpenseHead.builder()
                        .name("Bank Charges")
                        .category(ExpenseCategory.MAINTENANCE)
                        .description("Bank charges and transaction fees")
                        .build()
        );

        expenseHeadRepository.saveAll(heads);
    }
}