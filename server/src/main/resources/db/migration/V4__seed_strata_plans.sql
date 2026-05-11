-- Seed data — NSW strata plans (mirrors NSW Strata Hub public records)
INSERT INTO strata_plans (plan_number, address, suburb, state, postcode, total_lots,
    registration_date, managing_agent, managing_agent_licence, last_agm,
    building_class, sinking_fund_status, latitude, longitude, data_source)
VALUES
    ('SP83985', '1 Bourke Road, Mascot', 'Mascot', 'NSW', '2020', 132,
     '12 Sep 1998', 'City Strata Management Pty Ltd', '1234567', '15 Jun 2024',
     'Class 2', 'critical', -33.9283, 151.1994, 'seed'),
    ('SP52948', '200 Harris Street, Pyrmont', 'Pyrmont', 'NSW', '2009', 48,
     '5 Mar 2003', 'Harris Strata Services', '2345678', '22 Sep 2024',
     'Class 2', 'adequate', -33.8763, 151.1938, 'seed'),
    ('SP75403', '55 King Street, Newtown', 'Newtown', 'NSW', '2042', 18,
     '20 Jul 2008', 'Inner West Strata', '3456789', '10 Mar 2025',
     'Class 2', 'adequate', -33.8987, 151.1792, 'seed'),
    ('SP91234', '15 Albert Avenue, Chatswood', 'Chatswood', 'NSW', '2067', 60,
     '14 Feb 2011', 'North Shore Strata Pty Ltd', '4567890', '18 Nov 2024',
     'Class 2', 'low', -33.7969, 151.1828, 'seed'),
    ('SP44567', '88 Crown Street, Surry Hills', 'Surry Hills', 'NSW', '2010', 24,
     '9 Nov 2001', 'Crown Strata Management', '5678901', '5 Aug 2024',
     'Class 2', 'adequate', -33.8848, 151.2115, 'seed'),
    ('SP33210', '3 Church Street, Parramatta', 'Parramatta', 'NSW', '2150', 96,
     '28 Apr 2006', 'Western Sydney Strata Group', '6789012', '2 Dec 2024',
     'Class 2', 'adequate', -33.8153, 151.0034, 'seed'),
    ('SP88765', '10 Whistler Street, Manly', 'Manly', 'NSW', '2095', 12,
     '3 Aug 1995', 'Northern Beaches Strata', '7890123', '20 Jul 2024',
     'Class 2', 'adequate', -33.7969, 151.2869, 'seed'),
    ('SP66543', '25 Moore Street, Liverpool', 'Liverpool', 'NSW', '2170', 72,
     '17 Jun 2013', 'South West Strata Services', '8901234', '14 Jan 2025',
     'Class 2', 'critical', -33.9208, 150.9236, 'seed'),
    ('SP22341', '180 Campbell Parade, Bondi Beach', 'Bondi Beach', 'NSW', '2026', 36,
     '30 Jan 2000', 'Eastern Suburbs Strata', '9012345', '8 Oct 2024',
     'Class 2', 'adequate', -33.8915, 151.2747, 'seed'),
    ('SP55678', '100 Miller Street, North Sydney', 'North Sydney', 'NSW', '2060', 54,
     '22 Mar 2009', 'North Shore Premier Strata', '0123456', '3 Feb 2025',
     'Class 2', 'low', -33.8394, 151.2073, 'seed');

-- VIC plans
INSERT INTO strata_plans (plan_number, address, suburb, state, postcode, total_lots,
    registration_date, managing_agent, managing_agent_licence, last_agm,
    building_class, sinking_fund_status, latitude, longitude, data_source)
VALUES
    ('OC1234567', '1 Queens Road, Melbourne', 'Melbourne', 'VIC', '3004', 84,
     '10 Oct 2007', 'Melbourne Strata Management', 'VIC12345', '25 Aug 2024',
     'Class 2', 'adequate', -37.8399, 144.9738, 'seed'),
    ('OC7654321', '50 Southbank Boulevard, Southbank', 'Southbank', 'VIC', '3006', 120,
     '5 May 2015', 'Southbank OC Management', 'VIC67890', '12 Dec 2024',
     'Class 2', 'low', -37.8234, 144.9656, 'seed');

-- Building defects for SP83985 (Mascot — two active orders)
INSERT INTO building_defects (strata_plan_id, category, description, severity,
    order_type, reported_date, source_document)
SELECT id, 'structural',
    'Structural defects — façade water ingress and balcony cracking requiring rectification',
    'critical', 'rectification', '2024-01-12',
    'NSW Building Commission order BC-2024-0112'
FROM strata_plans WHERE plan_number = 'SP83985';

INSERT INTO building_defects (strata_plan_id, category, description, severity,
    order_type, reported_date, source_document)
SELECT id, 'structural',
    'Unauthorised works to load-bearing walls in basement car park',
    'critical', 'stop_work', '2024-03-03',
    'NSW Building Commission order BC-2024-0303'
FROM strata_plans WHERE plan_number = 'SP83985';

-- Defect for SP52948 (Pyrmont — fire door non-compliance)
INSERT INTO building_defects (strata_plan_id, category, description, severity,
    order_type, reported_date, source_document)
SELECT id, 'fire_safety',
    'Fire door non-compliance — 14 doors require replacement or rectification',
    'major', 'rectification', '2024-08-08',
    'NSW Building Commission order BC-2024-0808'
FROM strata_plans WHERE plan_number = 'SP52948';

-- Defects for SP66543 (Liverpool — two active orders)
INSERT INTO building_defects (strata_plan_id, category, description, severity,
    order_type, reported_date, source_document)
SELECT id, 'structural',
    'Structural cracking to columns B4–B7 on levels 3 and 4 requiring engineer assessment and rectification',
    'critical', 'rectification', '2024-02-21',
    'NSW Building Commission order BC-2024-0221'
FROM strata_plans WHERE plan_number = 'SP66543';

INSERT INTO building_defects (strata_plan_id, category, description, severity,
    order_type, reported_date, source_document)
SELECT id, 'safety',
    'Pool area closed pending safety audit — non-compliant pool barrier',
    'major', 'prohibition', '2024-04-30',
    'NSW Building Commission order BC-2024-0430'
FROM strata_plans WHERE plan_number = 'SP66543';

-- Defect for OC7654321 (Southbank — cladding)
INSERT INTO building_defects (strata_plan_id, category, description, severity,
    order_type, reported_date, source_document)
SELECT id, 'fire_safety',
    'External cladding non-compliant with current fire safety standards — rectification required',
    'critical', 'rectification', '2024-07-09',
    'VIC Building Commission order VIC-2024-0709'
FROM strata_plans WHERE plan_number = 'OC7654321';
