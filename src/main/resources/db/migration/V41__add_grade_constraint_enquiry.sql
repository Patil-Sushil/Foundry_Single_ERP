-- Add grade constraint
ALTER TABLE enquiry_item ADD CONSTRAINT check_valid_grade
    CHECK (material_grade IN (
        -- Cast Iron
                     'FG150', 'FG200', 'FG260', 'FG300', 'FG350', 'FG400',
        -- SG Iron
                     'SG 400/15', 'SG 400/18', 'SG 500/7', 'SG 600/3', 'SG 700/2', 'SG 800/2',
        -- Carbon Steel
                     'WCB', 'WCC', 'WCA', 'LCB', 'LCC',
        -- Stainless Steel
                     'CF8 (SS 304)', 'CF8M (SS 316)', 'CF3 (SS 304L)', 'CF3M (SS 316L)', 'CA15 (SS 410)', 'CA40 (SS 420)',
        -- Alloy Steel
                     'Mn Steel (11-14%)', 'Cr-Mo Steel', 'Ni-Hard', 'High Chrome Iron',
        -- Aluminum Alloy
                     'LM6', 'LM25', 'ADC12', 'A356', 'LM2',
        -- Copper Alloy
                     'Gunmetal (GM)', 'Phosphor Bronze (PB)', 'Leaded Bronze', 'Aluminum Bronze', 'Brass',
        -- Special
                     'MIXED'
        ));