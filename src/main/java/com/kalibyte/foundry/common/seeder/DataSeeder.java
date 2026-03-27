package com.kalibyte.foundry.common.seeder;

import com.kalibyte.foundry.auth.entity.ENUMS.RoleName;
import com.kalibyte.foundry.auth.entity.Role;
import com.kalibyte.foundry.auth.repository.RoleRepository;
import com.kalibyte.foundry.customer.dto.CustomerRequest;
import com.kalibyte.foundry.customer.entity.Customer;
import com.kalibyte.foundry.customer.repository.CustomerRepository;
import com.kalibyte.foundry.customer.service.CustomerService;
import com.kalibyte.foundry.furnace.furnace_heats.entity.FurnaceHeats;
import com.kalibyte.foundry.furnace.furnace_heats.repository.FurnaceHeatsRepository;
import com.kalibyte.foundry.furnace.furnace_report.entity.Enum.Shift;
import com.kalibyte.foundry.furnace.furnace_report.entity.Furnace;
import com.kalibyte.foundry.furnace.furnace_report.repository.FurnaceRepository;
import com.kalibyte.foundry.order.entity.enums.OrderStatus;
import com.kalibyte.foundry.order.entity.enums.OrderType;
import com.kalibyte.foundry.order.entity.Order;
import com.kalibyte.foundry.order.repository.OrderRepository;
import com.kalibyte.foundry.quotation.entity.Quotation;
import com.kalibyte.foundry.quotation.entity.enums.QuotationStatus;
import com.kalibyte.foundry.quotation.repository.QuotationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("seed") // Only runs in dev profile
@org.springframework.core.annotation.Order(1)
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final QuotationRepository quotationRepository;
    private final FurnaceRepository furnaceRepository;
    private final FurnaceHeatsRepository furnaceHeatsRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("===== STARTING DATA SEEDING =====");

        seedRoles();
        seedCustomers();
        seedOrders();
        seedFurnaceReports();

        log.info("===== DATA SEEDING COMPLETE =====");
    }

    /* ---------------- ROLES ---------------- */

    private void seedRoles() {
        log.info("Seeding roles...");
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setName(roleName);
                        role.setDescription(roleName.name() + " Role");
                        return roleRepository.save(role);
                    });
        }
        log.info("Roles seeded.");
    }

    /* ---------------- CUSTOMERS ---------------- */

    private void seedCustomers() {
        if (customerRepository.count() > 0) {
            log.info("Customers already exist. Skipping.");
            return;
        }

        log.info("Seeding customers...");
        createCustomer("Acme Castings", "acme@company.com", "1111111111");
        createCustomer("Steel Industries", "steel@company.com", "2222222222");
        createCustomer("Metal Traders", "metal@company.com", "3333333333");
        log.info("Customers seeded.");
    }

    private void createCustomer(String name, String email, String phone) {
        CustomerRequest req = new CustomerRequest();
        req.setName(name);
        req.setEmail(email);
        req.setPhone(phone);
        req.setCreditLimit(new BigDecimal("100000"));
        customerService.createCustomer(req);
    }

    /* ---------------- ORDERS ---------------- */

    private void seedOrders() {
        if (orderRepository.count() > 0) {
            log.info("Orders already exist. Skipping.");
            return;
        }

        log.info("Seeding orders and quotations...");
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            log.warn("No customers found for seeding orders.");
            return;
        }

        Customer acme = customers.get(0);

        // Quotation for Order 1
        Quotation q1 = new Quotation();
        q1.setQuotationNumber("QT-2024-001");
        q1.setCustomer(acme);
        q1.setQuotationDate(LocalDate.now().minusDays(10));
        q1.setStatus(QuotationStatus.APPROVED);
        q1.setTotalAmount(new BigDecimal("5000.00"));
        q1 = quotationRepository.save(q1);

        // Order 1 (Quotation-based)
        Order o1 = Order.builder()
                .orderNumber("ORD-2024-001")
                .customer(acme)
                .orderType(OrderType.QUOTATION)
                .quotation(q1)
                .orderDate(LocalDate.now().minusDays(5))
                .deliveryDate(LocalDate.now().plusDays(20))
                .status(OrderStatus.IN_PRODUCTION)
                .totalAmount(new BigDecimal("5000.00"))
                .build();
        orderRepository.save(o1);

        // Order 2 (Direct)
        Order o2 = Order.builder()
                .orderNumber("ORD-2024-002")
                .customer(acme)
                .orderType(OrderType.DIRECT)
                .quotation(null) // Possible now because we fixed Order.java
                .orderDate(LocalDate.now())
                .deliveryDate(LocalDate.now().plusDays(30))
                .status(OrderStatus.CREATED)
                .totalAmount(new BigDecimal("2500.00"))
                .build();
        orderRepository.save(o2);

        log.info("Orders and quotations seeded.");
    }

    /* ---------------- FURNACE REPORTS ---------------- */

    private void seedFurnaceReports() {
        if (furnaceRepository.count() > 0) {
            log.info("Furnace reports already exist. Skipping.");
            return;
        }

        log.info("Seeding furnace reports and heats...");
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            log.warn("No orders found for seeding furnace reports.");
            return;
        }

        Order order = orders.get(0);

        // Create a Furnace Report
        Furnace report = Furnace.builder()
                .furnaceRefNo("FUR-2024-001")
                .operatorName("John Doe")
                .shift(Shift.DAY)
                .inchargeName("Jane Smith")
                .date(LocalDate.now())
                .heats(new java.util.ArrayList<>())
                .build();
        
        // Add a Heat linked to an Order
        FurnaceHeats heat1 = FurnaceHeats.builder()
                .sipercentage(0.5)
                .cpcpercentage(0.1)
                .mgpercentage(0.05)
                .startReading(100.0)
                .stopReading(150.0)
                .differenceReading(50.0)
                .totalWeight(500.0)
                .pouringTemp(1450.0)
                .powerToWeight(0.1)
                .pouringStartTime(LocalTime.of(9, 0))
                .pouringEndTime(LocalTime.of(10, 30))
                .order(order)
                .build();
        
        // Add a Heat NOT linked to any order
        FurnaceHeats heat2 = FurnaceHeats.builder()
                .sipercentage(0.4)
                .cpcpercentage(0.12)
                .mgpercentage(0.04)
                .startReading(150.0)
                .stopReading(210.0)
                .differenceReading(60.0)
                .totalWeight(550.0)
                .pouringTemp(1460.0)
                .powerToWeight(0.109)
                .pouringStartTime(LocalTime.of(11, 0))
                .pouringEndTime(LocalTime.of(12, 15))
                .order(null)
                .build();

        report.addHeat(heat1);
        report.addHeat(heat2);
        
        furnaceRepository.save(report);
        log.info("Furnace reports and heats seeded.");
    }
}
