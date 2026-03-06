package com.kalibyte.foundry.inventory.common.seeder;

import com.kalibyte.foundry.auth.entity.User;
import com.kalibyte.foundry.auth.repository.UserRepository;
import com.kalibyte.foundry.inventory.department.entity.Department;
import com.kalibyte.foundry.inventory.department.repository.DepartmentRepository;
import com.kalibyte.foundry.inventory.inward.entity.MaterialInward;
import com.kalibyte.foundry.inventory.inward.entity.ReceivedItem;
import com.kalibyte.foundry.inventory.inward.entity.enums.InwardStatus;
import com.kalibyte.foundry.inventory.inward.repository.MaterialInwardRepository;
import com.kalibyte.foundry.inventory.inward.repository.ReceivedItemRepository;
import com.kalibyte.foundry.inventory.issue.entity.IssuedItem;
import com.kalibyte.foundry.inventory.issue.entity.MaterialIssue;
import com.kalibyte.foundry.inventory.issue.repository.MaterialIssueRepository;
import com.kalibyte.foundry.inventory.item.entity.Item;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemSubCategory;
import com.kalibyte.foundry.inventory.item.entity.enums.ItemUnit;
import com.kalibyte.foundry.inventory.item.repository.ItemRepository;
import com.kalibyte.foundry.inventory.ledger.entity.VendorLedger;
import com.kalibyte.foundry.inventory.ledger.entity.enums.LedgerEntryType;
import com.kalibyte.foundry.inventory.ledger.repository.VendorLedgerRepository;
import com.kalibyte.foundry.inventory.purchaseorder.entity.ItemVendorRate;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrderItem;
import com.kalibyte.foundry.inventory.purchaseorder.entity.PurchaseOrder;
import com.kalibyte.foundry.inventory.purchaseorder.entity.enums.POStatus;
import com.kalibyte.foundry.inventory.purchaseorder.repository.ItemVendorRateRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderItemRepository;
import com.kalibyte.foundry.inventory.purchaseorder.repository.PurchaseOrderRepository;
import com.kalibyte.foundry.inventory.vendor.entity.Vendor;
import com.kalibyte.foundry.inventory.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("seed") // Use a specific profile for seeding inventory
@Order(2) // Run after AdminBootstrap and DataSeeder
public class InventoryDataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final VendorRepository vendorRepository;
    private final ItemRepository itemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository orderItemRepository;
    private final MaterialInwardRepository materialInwardRepository;
    private final ReceivedItemRepository receivedItemRepository;
    private final MaterialIssueRepository materialIssueRepository;
    private final VendorLedgerRepository vendorLedgerRepository;
    private final ItemVendorRateRepository itemVendorRateRepository;
    private final UserRepository userRepository;

    private User adminUser;

    @Override
    @Transactional
    public void run(String... args) {
        if (itemRepository.count() > 0) {
            log.info("Inventory data already exists. Skipping seeding.");
            return;
        }

        log.info("Seeding inventory data...");

        adminUser = userRepository.findAll().stream().findFirst().orElse(null);
        
        List<Department> departments = seedDepartments();
        List<Vendor> vendors = seedVendors();
        List<Item> items = seedItems(departments);
        
        seedTransactions(vendors, items, departments);

        log.info("Inventory data seeded successfully.");
    }

    private List<Department> seedDepartments() {
        log.info("Seeding Departments...");
        String[][] deptData = {
            {"Quality Control", "QC"},
            {"Packaging", "PKG"},
            {"R&D", "RD"},
            {"Administration", "ADMIN"},
            {"Melting", "MELT"},
            {"Maintenance", "MAINT"},
            {"Store", "STORE"}
        };

        List<Department> departments = new ArrayList<>();
        for (String[] data : deptData) {
            departmentRepository.findByName(data[0]).ifPresentOrElse(
                departments::add,
                () -> {
                    Department dept = new Department();
                    dept.setName(data[0]);
                    dept.setCode(data[1]);
                    departments.add(departmentRepository.save(dept));
                }
            );
        }
        // Add existing ones
        departmentRepository.findAll().forEach(d -> {
            if (!departments.contains(d)) departments.add(d);
        });
        return departments;
    }

    private List<Vendor> seedVendors() {
        log.info("Seeding Vendors...");
        List<Vendor> vendors = new ArrayList<>();
        vendors.add(createVendor("Industrial Solutions Ltd", "9876543210", "27AAAAA0000A1Z5", "Industrial Estate, Pune"));
        vendors.add(createVendor("Elite Packaging Systems", "9876543211", "27BBBBB1111B1Z5", "Chakan MIDC, Pune"));
        vendors.add(createVendor("Global Raw Materials", "9876543212", "27CCCCC2222C1Z5", "Narhe, Pune"));
        vendors.add(createVendor("Precision Tools & Spares", "9876543213", "27DDDDD3333D1Z5", "Bhosari, Pune"));
        vendors.add(createVendor("Quality Chemicals", "9876543214", "27EEEEE4444E1Z5", "Hadapsar, Pune"));
        return vendorRepository.saveAll(vendors);
    }

    private Vendor createVendor(String name, String phone, String gst, String address) {
        return Vendor.builder()
                .name(name)
                .phone(phone)
                .gstNumber(gst)
                .address(address)
                .isActive(true)
                .build();
    }

    private List<Item> seedItems(List<Department> departments) {
        log.info("Seeding Items...");
        List<Item> items = new ArrayList<>();
        
        Department production = Optional.ofNullable(departmentRepository.findByCode("MELT")).orElse(departments.get(0));
        Department maint = Optional.ofNullable(departmentRepository.findByCode("MAINT")).orElse(departments.get(0));
        Department store = Optional.ofNullable(departmentRepository.findByCode("STORE")).orElse(departments.get(0));

        // Raw Materials
        items.add(createItem("Pig Iron Grade A", "RM-001", ItemCategory.RAW_MATERIAL, ItemSubCategory.FERROUS, production, ItemUnit.KG, 5000, 2000));
        items.add(createItem("Scrap Steel", "RM-002", ItemCategory.RAW_MATERIAL, ItemSubCategory.FERROUS, production, ItemUnit.KG, 10000, 5000));
        items.add(createItem("Silica Sand", "RM-003", ItemCategory.RAW_MATERIAL, ItemSubCategory.SAND, production, ItemUnit.KG, 2000, 1000));

        // Consumables
        items.add(createItem("Grinding Wheels 4\"", "CON-001", ItemCategory.CONSUMABLE, ItemSubCategory.ABRASIVE, production, ItemUnit.PCS, 100, 50));
        items.add(createItem("Industrial Oxygen", "CON-002", ItemCategory.CONSUMABLE, ItemSubCategory.GENERAL, production, ItemUnit.LITRE, 200, 100));
        items.add(createItem("Hydraulic Oil VG 68", "CON-003", ItemCategory.CONSUMABLE, ItemSubCategory.GENERAL, maint, ItemUnit.LITRE, 50, 20));
        items.add(createItem("CORES", "CORE-001", ItemCategory.RAW_MATERIAL, ItemSubCategory.CORE, production, ItemUnit.PCS, 30, 10));

        // Spare Parts
        items.add(createItem("Ball Bearing 6205", "SP-001", ItemCategory.SPARE_PART, ItemSubCategory.MECHANICAL, maint, ItemUnit.PCS, 20, 10));
        items.add(createItem("Conveyor Belt 20m", "SP-002", ItemCategory.SPARE_PART, ItemSubCategory.MECHANICAL, production, ItemUnit.METER, 100, 40));
        items.add(createItem("Electric Motor 5HP", "SP-003", ItemCategory.SPARE_PART, ItemSubCategory.ELECTRICAL, maint, ItemUnit.PCS, 5, 2));

        // Packaging Materials
        items.add(createItem("Wooden Pallets", "PKG-001", ItemCategory.PACKING_MATERIAL, ItemSubCategory.GENERAL, store, ItemUnit.PCS, 50, 20));
        items.add(createItem("Stretch Film Roll", "PKG-002", ItemCategory.PACKING_MATERIAL, ItemSubCategory.GENERAL, store, ItemUnit.BAG, 30, 10));

        return itemRepository.saveAll(items);
    }

    private Item createItem(String name, String code, ItemCategory cat, ItemSubCategory sub, Department dept, ItemUnit unit, double reorder, double min) {
        return Item.builder()
                .name(name)
                .code(code)
                .category(cat)
                .subCategory(sub)
                .department(dept)
                .unit(unit)
                .reorderLevel(BigDecimal.valueOf(reorder))
                .minStockLevel(BigDecimal.valueOf(min))
                .currentStock(BigDecimal.ZERO)
                .avgRate(BigDecimal.ZERO)
                .isActive(true)
                .build();
    }

    private void seedTransactions(List<Vendor> vendors, List<Item> items, List<Department> departments) {
        log.info("Seeding Transactions (POs, Inwards, Issues)...");
        LocalDate now = LocalDate.now();

        // 1. PO for Raw Materials - Fully Received
        PurchaseOrder po1 = createPO(vendors.get(2), now.minusDays(60), POStatus.RECEIVED);
        addOrderItem(po1, items.get(0), 5000, 45.00); // Pig Iron
        addOrderItem(po1, items.get(1), 10000, 32.00); // Scrap Steel
        
        MaterialInward mi1 = createInward(po1, vendors.get(2), now.minusDays(55));
        receiveItem(mi1, items.get(0), 5000, 45.00, po1.getOrderItems().get(0));
        receiveItem(mi1, items.get(1), 10000, 32.00, po1.getOrderItems().get(1));
        confirmInward(mi1);

        // 2. PO for Consumables - Partially Received
        PurchaseOrder po2 = createPO(vendors.get(0), now.minusDays(45), POStatus.PARTIALLY_RECEIVED);
        addOrderItem(po2, items.get(3), 100, 150.00); // Grinding Wheels
        addOrderItem(po2, items.get(4), 200, 80.00); // Oxygen
        
        MaterialInward mi2 = createInward(po2, vendors.get(0), now.minusDays(40));
        receiveItem(mi2, items.get(3), 50, 150.00, po2.getOrderItems().get(0));
        receiveItem(mi2, items.get(4), 100, 80.00, po2.getOrderItems().get(1));
        confirmInward(mi2);

        // 3. Issue some items
        MaterialIssue iss1 = createIssue(departments.stream().filter(d -> d.getCode().equals("MELT")).findFirst().get(), now.minusDays(35));
        issueItem(iss1, items.get(0), 2000); // Issue 2000 Pig Iron
        issueItem(iss1, items.get(3), 20); // Issue 20 Grinding Wheels

        // 4. Another Inward for po2
        MaterialInward mi3 = createInward(po2, vendors.get(0), now.minusDays(30));
        receiveItem(mi3, items.get(3), 50, 155.00, po2.getOrderItems().get(0)); // Rate changed
        receiveItem(mi3, items.get(4), 100, 80.00, po2.getOrderItems().get(1));
        confirmInward(mi3);
        po2.setStatus(POStatus.RECEIVED);
        purchaseOrderRepository.save(po2);

        // 5. PO for Spare Parts - Open
        PurchaseOrder po3 = createPO(vendors.get(3), now.minusDays(20), POStatus.OPEN);
        addOrderItem(po3, items.get(6), 20, 450.00); // Ball Bearing
        addOrderItem(po3, items.get(8), 5, 12000.00); // Motor

        // 6. Direct Inward (no PO) for Packaging
        MaterialInward mi4 = createInward(null, vendors.get(1), now.minusDays(15));
        receiveItem(mi4, items.get(9), 100, 1200.00, null); // Wooden Pallets
        confirmInward(mi4);

        // 7. More Issues
        MaterialIssue iss2 = createIssue(departments.stream().filter(d -> d.getCode().equals("MAINT")).findFirst().get(), now.minusDays(10));
        
        // Add stock for Hydraulic Oil
        MaterialInward mi5 = createInward(null, vendors.get(4), now.minusDays(12));
        receiveItem(mi5, items.get(5), 50, 250.00, null);
        confirmInward(mi5);
        
        issueItem(iss2, items.get(5), 10);
        
        // 8. Issue that brings stock low
        MaterialIssue iss3 = createIssue(departments.stream().filter(d -> d.getCode().equals("MELT")).findFirst().get(), now.minusDays(5));
        issueItem(iss3, items.get(0), 2500); // Pig Iron: 5000 - 2000 - 2500 = 500 (Reorder is 5000, so LOW)
        issueItem(iss3, items.get(4), 180); // Oxygen: 200 - 180 = 20 (Reorder 200, so LOW)

        // 9. Issue that brings stock to critical
        MaterialIssue iss4 = createIssue(departments.stream().filter(d -> d.getCode().equals("MELT")).findFirst().get(), now.minusDays(1));
        issueItem(iss4, items.get(0), 400); // Pig Iron: 500 - 400 = 100 (Min stock 2000, so CRITICAL)
    }

    private PurchaseOrder createPO(Vendor vendor, LocalDate date, POStatus status) {
        PurchaseOrder po = PurchaseOrder.builder()
                .poNumber(String.format("PO-%d-%04d", date.getYear(), purchaseOrderRepository.count() + 1))
                .vendor(vendor)
                .poDate(date)
                .status(status)
                .createdByUserId(adminUser != null ? adminUser.getId() : null)
                .orderItems(new ArrayList<>())
                .build();
        return purchaseOrderRepository.save(po);
    }

    private void addOrderItem(PurchaseOrder po, Item item, double qty, double rate) {
        PurchaseOrderItem oi = PurchaseOrderItem.builder()
                .purchaseOrder(po)
                .item(item)
                .orderedQuantity(BigDecimal.valueOf(qty))
                .receivedQuantity(BigDecimal.ZERO)
                .unitRate(BigDecimal.valueOf(rate))
                .build();
        po.getOrderItems().add(oi);
        orderItemRepository.save(oi);
    }

    private MaterialInward createInward(PurchaseOrder po, Vendor vendor, LocalDate date) {
        MaterialInward mi = MaterialInward.builder()
                .inwardNumber(String.format("MI-%d-%04d", date.getYear(), materialInwardRepository.count() + 1))
                .purchaseOrder(po)
                .vendor(vendor)
                .inwardDate(date)
                .status(InwardStatus.DRAFT)
                .createdByUserId(adminUser != null ? adminUser.getId() : null)
                .receivedItems(new ArrayList<>())
                .build();
        return materialInwardRepository.save(mi);
    }

    private void receiveItem(MaterialInward mi, Item item, double qty, double rate, PurchaseOrderItem oi) {
        ReceivedItem ri = ReceivedItem.builder()
                .materialInward(mi)
                .item(item)
                .orderItem(oi)
                .poQuantity(oi != null ? oi.getOrderedQuantity() : null)
                .receivedQuantity(BigDecimal.valueOf(qty))
                .unitRate(BigDecimal.valueOf(rate))
                .build();
        mi.getReceivedItems().add(ri);
        receivedItemRepository.save(ri);
        
        if (oi != null) {
            oi.setReceivedQuantity(oi.getReceivedQuantity().add(BigDecimal.valueOf(qty)));
            orderItemRepository.save(oi);
        }
    }

    private void confirmInward(MaterialInward mi) {
        mi.setStatus(InwardStatus.CONFIRMED);
        mi.setConfirmedAt(java.time.LocalDateTime.now());
        mi.setConfirmedByUserId(adminUser != null ? adminUser.getId() : null);
        materialInwardRepository.save(mi);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ReceivedItem ri : mi.getReceivedItems()) {
            Item item = ri.getItem();
            item.receiveStock(ri.getReceivedQuantity(), ri.getUnitRate());
            itemRepository.save(item);
            
            totalAmount = totalAmount.add(ri.getReceivedQuantity().multiply(ri.getUnitRate()));
            
            // Update Rate History
            ItemVendorRate rateHistory = itemVendorRateRepository.findByItemIdAndVendorId(item.getId(), mi.getVendor().getId())
                    .orElse(new ItemVendorRate());
            rateHistory.setItem(item);
            rateHistory.setVendor(mi.getVendor());
            rateHistory.setLastRate(ri.getUnitRate());
            rateHistory.setLastPurchasedOn(mi.getInwardDate());
            itemVendorRateRepository.save(rateHistory);
        }

        // Vendor Ledger
        VendorLedger ledger = new VendorLedger();
        ledger.setVendor(mi.getVendor());
        ledger.setMaterialInward(mi);
        ledger.setEntryType(LedgerEntryType.CREDIT);
        ledger.setAmount(totalAmount);
        ledger.setEntryDate(mi.getInwardDate());
        ledger.setDescription("Material Inward: " + mi.getInwardNumber());
        vendorLedgerRepository.save(ledger);
    }

    private MaterialIssue createIssue(Department dept, LocalDate date) {
        MaterialIssue issue = MaterialIssue.builder()
                .issueNumber(String.format("ISS-%d-%04d", date.getYear(), materialIssueRepository.count() + 1))
                .department(dept)
                .issueDate(date)
                .issuedByUserId(adminUser != null ? adminUser.getId() : null)
                .issuedItems(new ArrayList<>())
                .build();
        return materialIssueRepository.save(issue);
    }

    private void issueItem(MaterialIssue issue, Item item, double qty) {
        IssuedItem ii = IssuedItem.builder()
                .materialIssue(issue)
                .item(item)
                .issuedQuantity(BigDecimal.valueOf(qty))
                .unitRate(item.getAvgRate()) // Capture current avg rate
                .build();
        issue.getIssuedItems().add(ii);
        
        item.issueStock(BigDecimal.valueOf(qty));
        itemRepository.save(item);
        
        materialIssueRepository.save(issue);
    }
}
