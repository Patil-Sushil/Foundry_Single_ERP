-------------------------------------------------------
-- EXTENSION (UUID generation)
-------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-------------------------------------------------------
-- DELIVERY CHALLANS
-------------------------------------------------------

CREATE TABLE delivery_challans (

                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                   dc_number VARCHAR(50) UNIQUE NOT NULL,

                                   order_id UUID NOT NULL,
                                   customer_id UUID NOT NULL,

                                   dispatch_date DATE,

                                   vehicle_number VARCHAR(50),
                                   transport_name VARCHAR(100),
                                   lr_number VARCHAR(100),

                                   total_quantity INT,
                                   total_weight DECIMAL(10,2),
                                   total_amount DECIMAL(12,2),

                                   status VARCHAR(20) DEFAULT 'CREATED',

                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP,
                                   created_by VARCHAR(255),
                                   updated_by VARCHAR(255),

                                   CONSTRAINT fk_dc_order
                                       FOREIGN KEY (order_id)
                                           REFERENCES orders(id),

                                   CONSTRAINT fk_dc_customer
                                       FOREIGN KEY (customer_id)
                                           REFERENCES customer(id)

);

-------------------------------------------------------
-- INDEXES
-------------------------------------------------------

CREATE INDEX idx_dc_order_id
    ON delivery_challans(order_id);

CREATE INDEX idx_dc_customer_id
    ON delivery_challans(customer_id);

-------------------------------------------------------
-- DELIVERY CHALLAN ITEMS
-------------------------------------------------------

CREATE TABLE delivery_challan_items (

                                        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                        dc_id UUID NOT NULL,

                                        order_item_id UUID NOT NULL,

                                        quantity INT,
                                        weight DECIMAL(10,2),
                                        rate DECIMAL(10,2),

                                        amount DECIMAL(12,2),

                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        updated_at TIMESTAMP,
                                        created_by VARCHAR(255),
                                        updated_by VARCHAR(255),

                                        CONSTRAINT fk_dc_items
                                            FOREIGN KEY (dc_id)
                                                REFERENCES delivery_challans(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_dc_order_item
                                            FOREIGN KEY (order_item_id)
                                                REFERENCES order_item(id)

);

-------------------------------------------------------
-- INDEXES
-------------------------------------------------------

CREATE INDEX idx_dc_items_dc_id
    ON delivery_challan_items(dc_id);

CREATE INDEX idx_dc_items_order_item_id
    ON delivery_challan_items(order_item_id);

-------------------------------------------------------
-- INVOICES (1 ORDER → 1 INVOICE)
-------------------------------------------------------

CREATE TABLE invoices (

                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          invoice_number VARCHAR(50) UNIQUE NOT NULL,

                          order_id UUID UNIQUE NOT NULL,

                          vehicle_number VARCHAR(50),

                          subtotal DECIMAL(12,2),

                          cgst DECIMAL(10,2),
                          sgst DECIMAL(10,2),
                          igst DECIMAL(10,2),

                          gst_percentage DECIMAL(5,2),

                          total_amount DECIMAL(12,2),

                          invoice_date DATE,
                          due_date DATE,

                          bill_status VARCHAR(20) DEFAULT 'UNPAID',

                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP,
                          created_by VARCHAR(255),
                          updated_by VARCHAR(255),

                          CONSTRAINT fk_invoice_order
                              FOREIGN KEY (order_id)
                                  REFERENCES orders(id)

);

-------------------------------------------------------
-- INDEXES
-------------------------------------------------------

CREATE INDEX idx_invoice_order_id
    ON invoices(order_id);

-------------------------------------------------------
-- INVOICE ITEMS
-------------------------------------------------------

CREATE TABLE invoice_items (

                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                               invoice_id UUID NOT NULL,

                               order_item_id UUID NOT NULL,

                               quantity INT,
                               weight DECIMAL(10,2),
                               rate DECIMAL(10,2),

                               amount DECIMAL(12,2),

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP,
                               created_by VARCHAR(255),
                               updated_by VARCHAR(255),

                               CONSTRAINT fk_invoice_items_invoice
                                   FOREIGN KEY (invoice_id)
                                       REFERENCES invoices(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_invoice_items_order_item
                                   FOREIGN KEY (order_item_id)
                                       REFERENCES order_item(id)

);

-------------------------------------------------------
-- INDEXES
-------------------------------------------------------

CREATE INDEX idx_invoice_items_invoice_id
    ON invoice_items(invoice_id);

CREATE INDEX idx_invoice_items_order_item_id
    ON invoice_items(order_item_id);