-- ============================================================
-- IDP Platform: LARGE demo data set (MySQL 8)
--   100 categories, 100 designers (each with a company and 2 IDPs),
--   100 customers, 100 user admins, 100 platform managers,
--   plus favourites and views so the reports have numbers.
--
-- BEFORE RUNNING
--   Start the Spring Boot app ONCE so the tables, the 4 profiles and the
--   seed account admin1 exist. Stop the app, then run this whole script.
--
-- Safe to run more than once: nothing is inserted twice.
-- Every account created here has the password Password123
-- (the script copies the hash of admin1).
-- Usernames: admin001..admin100, designer001..designer100,
--            customer001..customer100, platform001..platform100
-- ============================================================
USE idp_platform;
SET SQL_SAFE_UPDATES = 0;

-- Helper: numbers 1..100
DROP TEMPORARY TABLE IF EXISTS tmp_nums;
CREATE TEMPORARY TABLE tmp_nums (i INT PRIMARY KEY);
INSERT INTO tmp_nums (i)
SELECT a.d * 10 + b.d + 1
FROM (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) a
CROSS JOIN
     (SELECT 0 AS d UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) b;

-- The password hash of Password123 (copied from the seeded admin1 account)
SET @pw = (SELECT password FROM user_accounts WHERE username = 'admin1' LIMIT 1);
SELECT IF(@pw IS NULL, 'STOP: admin1 not found. Start the app once first.', 'OK: password hash found') AS check_password;

-- 1. Categories (category_name is UNIQUE, so INSERT IGNORE skips repeats)
INSERT IGNORE INTO categories (category_name, description, created_at) VALUES
  ('Japandi', 'Interior design projects in the Japandi style.', NOW()),
  ('Scandinavian', 'Interior design projects in the Scandinavian style.', NOW()),
  ('Minimalist', 'Interior design projects in the Minimalist style.', NOW()),
  ('Modern', 'Interior design projects in the Modern style.', NOW()),
  ('Industrial', 'Interior design projects in the Industrial style.', NOW()),
  ('Contemporary', 'Interior design projects in the Contemporary style.', NOW()),
  ('Wabi-Sabi', 'Interior design projects in the Wabi-Sabi style.', NOW()),
  ('Luxury', 'Interior design projects in the Luxury style.', NOW()),
  ('Mid-Century Modern', 'Interior design projects in the Mid-Century Modern style.', NOW()),
  ('Art Deco', 'Interior design projects in the Art Deco style.', NOW()),
  ('Bohemian', 'Interior design projects in the Bohemian style.', NOW()),
  ('Coastal', 'Interior design projects in the Coastal style.', NOW()),
  ('Farmhouse', 'Interior design projects in the Farmhouse style.', NOW()),
  ('Rustic', 'Interior design projects in the Rustic style.', NOW()),
  ('Traditional', 'Interior design projects in the Traditional style.', NOW()),
  ('Transitional', 'Interior design projects in the Transitional style.', NOW()),
  ('Hollywood Regency', 'Interior design projects in the Hollywood Regency style.', NOW()),
  ('French Country', 'Interior design projects in the French Country style.', NOW()),
  ('Mediterranean', 'Interior design projects in the Mediterranean style.', NOW()),
  ('Tropical', 'Interior design projects in the Tropical style.', NOW()),
  ('Shabby Chic', 'Interior design projects in the Shabby Chic style.', NOW()),
  ('Eclectic', 'Interior design projects in the Eclectic style.', NOW()),
  ('Maximalist', 'Interior design projects in the Maximalist style.', NOW()),
  ('Biophilic', 'Interior design projects in the Biophilic style.', NOW()),
  ('Zen', 'Interior design projects in the Zen style.', NOW()),
  ('Urban Modern', 'Interior design projects in the Urban Modern style.', NOW()),
  ('Neoclassical', 'Interior design projects in the Neoclassical style.', NOW()),
  ('Victorian', 'Interior design projects in the Victorian style.', NOW()),
  ('Georgian', 'Interior design projects in the Georgian style.', NOW()),
  ('Colonial', 'Interior design projects in the Colonial style.', NOW()),
  ('Mission', 'Interior design projects in the Mission style.', NOW()),
  ('Craftsman', 'Interior design projects in the Craftsman style.', NOW()),
  ('Southwestern', 'Interior design projects in the Southwestern style.', NOW()),
  ('Moroccan', 'Interior design projects in the Moroccan style.', NOW()),
  ('Art Nouveau', 'Interior design projects in the Art Nouveau style.', NOW()),
  ('Bauhaus', 'Interior design projects in the Bauhaus style.', NOW()),
  ('Brutalist', 'Interior design projects in the Brutalist style.', NOW()),
  ('Futuristic', 'Interior design projects in the Futuristic style.', NOW()),
  ('Retro', 'Interior design projects in the Retro style.', NOW()),
  ('Vintage', 'Interior design projects in the Vintage style.', NOW()),
  ('Cottagecore', 'Interior design projects in the Cottagecore style.', NOW()),
  ('Glam', 'Interior design projects in the Glam style.', NOW()),
  ('Hamptons', 'Interior design projects in the Hamptons style.', NOW()),
  ('Nordic', 'Interior design projects in the Nordic style.', NOW()),
  ('Rattan Resort', 'Interior design projects in the Rattan Resort style.', NOW()),
  ('Peranakan', 'Interior design projects in the Peranakan style.', NOW()),
  ('Balinese', 'Interior design projects in the Balinese style.', NOW()),
  ('Thai Contemporary', 'Interior design projects in the Thai Contemporary style.', NOW()),
  ('Chinese Modern', 'Interior design projects in the Chinese Modern style.', NOW()),
  ('Korean Minimal', 'Interior design projects in the Korean Minimal style.', NOW()),
  ('Parisian', 'Interior design projects in the Parisian style.', NOW()),
  ('Italian Modern', 'Interior design projects in the Italian Modern style.', NOW()),
  ('Spanish Revival', 'Interior design projects in the Spanish Revival style.', NOW()),
  ('Tudor', 'Interior design projects in the Tudor style.', NOW()),
  ('Gothic', 'Interior design projects in the Gothic style.', NOW()),
  ('Steampunk', 'Interior design projects in the Steampunk style.', NOW()),
  ('Loft Living', 'Interior design projects in the Loft Living style.', NOW()),
  ('Scandi-Boho', 'Interior design projects in the Scandi-Boho style.', NOW()),
  ('Japanese Traditional', 'Interior design projects in the Japanese Traditional style.', NOW()),
  ('Warm Minimalist', 'Interior design projects in the Warm Minimalist style.', NOW()),
  ('Earthy Organic', 'Interior design projects in the Earthy Organic style.', NOW()),
  ('Soft Modern', 'Interior design projects in the Soft Modern style.', NOW()),
  ('Dark Academia', 'Interior design projects in the Dark Academia style.', NOW()),
  ('Mid-Century Scandinavian', 'Interior design projects in the Mid-Century Scandinavian style.', NOW()),
  ('Modern Classic', 'Interior design projects in the Modern Classic style.', NOW()),
  ('Modern Farmhouse', 'Interior design projects in the Modern Farmhouse style.', NOW()),
  ('Modern Rustic', 'Interior design projects in the Modern Rustic style.', NOW()),
  ('Modern Tropical', 'Interior design projects in the Modern Tropical style.', NOW()),
  ('Modern Coastal', 'Interior design projects in the Modern Coastal style.', NOW()),
  ('Modern Oriental', 'Interior design projects in the Modern Oriental style.', NOW()),
  ('Luxury Classic', 'Interior design projects in the Luxury Classic style.', NOW()),
  ('Luxury Modern', 'Interior design projects in the Luxury Modern style.', NOW()),
  ('Industrial Loft', 'Interior design projects in the Industrial Loft style.', NOW()),
  ('Industrial Chic', 'Interior design projects in the Industrial Chic style.', NOW()),
  ('Minimalist Zen', 'Interior design projects in the Minimalist Zen style.', NOW()),
  ('Compact Living', 'Interior design projects in the Compact Living style.', NOW()),
  ('Smart Home', 'Interior design projects in the Smart Home style.', NOW()),
  ('Kids Friendly', 'Interior design projects in the Kids Friendly style.', NOW()),
  ('Elderly Friendly', 'Interior design projects in the Elderly Friendly style.', NOW()),
  ('Pet Friendly', 'Interior design projects in the Pet Friendly style.', NOW()),
  ('Sustainable Eco', 'Interior design projects in the Sustainable Eco style.', NOW()),
  ('Budget Friendly', 'Interior design projects in the Budget Friendly style.', NOW()),
  ('Open Concept', 'Interior design projects in the Open Concept style.', NOW()),
  ('Small Space', 'Interior design projects in the Small Space style.', NOW()),
  ('Studio Apartment', 'Interior design projects in the Studio Apartment style.', NOW()),
  ('Penthouse Living', 'Interior design projects in the Penthouse Living style.', NOW()),
  ('Landed Home', 'Interior design projects in the Landed Home style.', NOW()),
  ('HDB Makeover', 'Interior design projects in the HDB Makeover style.', NOW()),
  ('Condo Revamp', 'Interior design projects in the Condo Revamp style.', NOW()),
  ('Office Interior', 'Interior design projects in the Office Interior style.', NOW()),
  ('Cafe and Retail', 'Interior design projects in the Cafe and Retail style.', NOW()),
  ('Hotel and Resort', 'Interior design projects in the Hotel and Resort style.', NOW()),
  ('Clinic Interior', 'Interior design projects in the Clinic Interior style.', NOW()),
  ('Showroom Design', 'Interior design projects in the Showroom Design style.', NOW()),
  ('Restaurant Interior', 'Interior design projects in the Restaurant Interior style.', NOW()),
  ('Co-living Space', 'Interior design projects in the Co-living Space style.', NOW()),
  ('Outdoor Patio', 'Interior design projects in the Outdoor Patio style.', NOW()),
  ('Rooftop Terrace', 'Interior design projects in the Rooftop Terrace style.', NOW()),
  ('Walk-in Wardrobe', 'Interior design projects in the Walk-in Wardrobe style.', NOW()),
  ('Home Theatre', 'Interior design projects in the Home Theatre style.', NOW());

-- 2. Accounts: 100 of each role
INSERT IGNORE INTO user_accounts (username, email, password, profileid, account_status, created_at)
SELECT CONCAT(r.prefix, LPAD(n.i, 3, '0')),
       CONCAT(r.prefix, LPAD(n.i, 3, '0'), '@test.com'),
       @pw, p.profileid, 'ACTIVE', NOW() - INTERVAL (n.i % 30) DAY
FROM tmp_nums n
JOIN (SELECT 'admin' AS prefix, 'User Admin' AS pname
      UNION ALL SELECT 'designer', 'Interior Designer'
      UNION ALL SELECT 'customer', 'Customer'
      UNION ALL SELECT 'platform', 'Platform Manager') r
JOIN user_profiles p ON p.profile_name = r.pname;

-- 3. One company per designer (userid is UNIQUE, so existing companies are kept)
INSERT IGNORE INTO companies (userid, company_name, description, email, phone, address, created_at, updated_at)
SELECT ua.userid,
       CONCAT(ELT(1 + (n.i - 1) % 20, 'Haven','Nordic','Atelier','Urban Nest','Summit','Harbour','Willow','Oakwood','Lumen','Verde',
                                     'Ember','Cobalt','Maple','Sterling','Aurora','Juniper','Granite','Saffron','Zenith','Marble'),
              ' ',
              ELT(1 + (n.i - 1) DIV 20, 'Interior Studio','Design Co','Living Concepts','Interiors','Home Atelier')),
       ELT(1 + n.i % 4, 'Specialising in warm minimalist homes.',
                        'Creating bright, practical family spaces.',
                        'Designing modern and contemporary interiors.',
                        'Delivering bespoke luxury interiors.'),
       ua.email, CONCAT('6100', LPAD(n.i, 4, '0')), CONCAT('Unit ', n.i, ', Design Street, Singapore'), NOW(), NOW()
FROM tmp_nums n
JOIN user_accounts ua ON ua.username = CONCAT('designer', LPAD(n.i, 3, '0'));

-- 4.1 IDP number 1 for every designer's company (skipped if the same title exists)
INSERT INTO idps (companyid, categoryid, title, description, budget, property_type, project_status, view_count, shortlist_count, created_at, updated_at)
SELECT x.companyid, x.categoryid,
       CONCAT(x.category_name, ' ', x.room),
       CONCAT(x.room, ' in the ', x.category_name, ' style, with practical storage and thoughtful lighting.'),
       x.budget, x.property_type, 'Published', 0, 0, x.ts, x.ts
FROM (
  SELECT co.companyid, c.categoryid, c.category_name,
         ELT(1 + (n.i + 1) % 6, 'Kitchen Renovation','Living Room Makeover','Bedroom Retreat',
                                  'Home Office Design','Full Home Renovation','Bathroom Upgrade') AS room,
         15000 + ((n.i * 7919 + 1 * 3571) % 236) * 1000 AS budget,
         ELT(1 + (n.i + 1) % 3, 'HDB','Condo','Landed') AS property_type,
         NOW() - INTERVAL ((n.i + 1) % 14) DAY AS ts
  FROM tmp_nums n
  JOIN user_accounts ua ON ua.username = CONCAT('designer', LPAD(n.i, 3, '0'))
  JOIN companies co ON co.userid = ua.userid
  JOIN (SELECT categoryid, category_name, ROW_NUMBER() OVER (ORDER BY categoryid) AS rn FROM categories) c
    ON c.rn = ((n.i * 2 + 1) % (SELECT COUNT(*) FROM categories)) + 1
) x
WHERE NOT EXISTS (SELECT 1 FROM idps i
                  WHERE i.companyid = x.companyid
                    AND i.title = CONCAT(x.category_name, ' ', x.room));

-- 4.2 IDP number 2 for every designer's company (skipped if the same title exists)
INSERT INTO idps (companyid, categoryid, title, description, budget, property_type, project_status, view_count, shortlist_count, created_at, updated_at)
SELECT x.companyid, x.categoryid,
       CONCAT(x.category_name, ' ', x.room),
       CONCAT(x.room, ' in the ', x.category_name, ' style, with practical storage and thoughtful lighting.'),
       x.budget, x.property_type, 'Published', 0, 0, x.ts, x.ts
FROM (
  SELECT co.companyid, c.categoryid, c.category_name,
         ELT(1 + (n.i + 2) % 6, 'Kitchen Renovation','Living Room Makeover','Bedroom Retreat',
                                  'Home Office Design','Full Home Renovation','Bathroom Upgrade') AS room,
         15000 + ((n.i * 7919 + 2 * 3571) % 236) * 1000 AS budget,
         ELT(1 + (n.i + 2) % 3, 'HDB','Condo','Landed') AS property_type,
         NOW() - INTERVAL ((n.i + 2) % 14) DAY AS ts
  FROM tmp_nums n
  JOIN user_accounts ua ON ua.username = CONCAT('designer', LPAD(n.i, 3, '0'))
  JOIN companies co ON co.userid = ua.userid
  JOIN (SELECT categoryid, category_name, ROW_NUMBER() OVER (ORDER BY categoryid) AS rn FROM categories) c
    ON c.rn = ((n.i * 2 + 2) % (SELECT COUNT(*) FROM categories)) + 1
) x
WHERE NOT EXISTS (SELECT 1 FROM idps i
                  WHERE i.companyid = x.companyid
                    AND i.title = CONCAT(x.category_name, ' ', x.room));

-- 5. Favourites: each customer saves 2 IDPs (customer_id + idp_id is UNIQUE)
INSERT IGNORE INTO favourites (customer_id, idp_id, saved_at)
SELECT ua.userid, r.idpid, NOW() - INTERVAL ((n.i + 1) % 10) DAY
FROM tmp_nums n
JOIN user_accounts ua ON ua.username = CONCAT('customer', LPAD(n.i, 3, '0'))
JOIN (SELECT idpid, ROW_NUMBER() OVER (ORDER BY idpid) AS rn FROM idps) r
  ON r.rn = ((n.i * 3 + 1 * 17) % (SELECT COUNT(*) FROM idps)) + 1;

INSERT IGNORE INTO favourites (customer_id, idp_id, saved_at)
SELECT ua.userid, r.idpid, NOW() - INTERVAL ((n.i + 2) % 10) DAY
FROM tmp_nums n
JOIN user_accounts ua ON ua.username = CONCAT('customer', LPAD(n.i, 3, '0'))
JOIN (SELECT idpid, ROW_NUMBER() OVER (ORDER BY idpid) AS rn FROM idps) r
  ON r.rn = ((n.i * 3 + 2 * 17) % (SELECT COUNT(*) FROM idps)) + 1;

-- 6. Views: each customer views 3 IDPs (one view per customer and IDP)
INSERT INTO idp_views (idpid, customerid, viewed_at)
SELECT r.idpid, ua.userid, NOW() - INTERVAL ((n.i + 1) % 7) DAY
FROM tmp_nums n
JOIN user_accounts ua ON ua.username = CONCAT('customer', LPAD(n.i, 3, '0'))
JOIN (SELECT idpid, ROW_NUMBER() OVER (ORDER BY idpid) AS rn FROM idps) r
  ON r.rn = ((n.i * 5 + 1 * 11) % (SELECT COUNT(*) FROM idps)) + 1
WHERE NOT EXISTS (SELECT 1 FROM idp_views v WHERE v.idpid = r.idpid AND v.customerid = ua.userid);

INSERT INTO idp_views (idpid, customerid, viewed_at)
SELECT r.idpid, ua.userid, NOW() - INTERVAL ((n.i + 2) % 7) DAY
FROM tmp_nums n
JOIN user_accounts ua ON ua.username = CONCAT('customer', LPAD(n.i, 3, '0'))
JOIN (SELECT idpid, ROW_NUMBER() OVER (ORDER BY idpid) AS rn FROM idps) r
  ON r.rn = ((n.i * 5 + 2 * 11) % (SELECT COUNT(*) FROM idps)) + 1
WHERE NOT EXISTS (SELECT 1 FROM idp_views v WHERE v.idpid = r.idpid AND v.customerid = ua.userid);

INSERT INTO idp_views (idpid, customerid, viewed_at)
SELECT r.idpid, ua.userid, NOW() - INTERVAL ((n.i + 3) % 7) DAY
FROM tmp_nums n
JOIN user_accounts ua ON ua.username = CONCAT('customer', LPAD(n.i, 3, '0'))
JOIN (SELECT idpid, ROW_NUMBER() OVER (ORDER BY idpid) AS rn FROM idps) r
  ON r.rn = ((n.i * 5 + 3 * 11) % (SELECT COUNT(*) FROM idps)) + 1
WHERE NOT EXISTS (SELECT 1 FROM idp_views v WHERE v.idpid = r.idpid AND v.customerid = ua.userid);

-- 7. Keep the counters on idps in step with the real rows
UPDATE idps SET view_count      = (SELECT COUNT(*) FROM idp_views v WHERE v.idpid  = idps.idpid);
UPDATE idps SET shortlist_count = (SELECT COUNT(*) FROM favourites f WHERE f.idp_id = idps.idpid);

DROP TEMPORARY TABLE IF EXISTS tmp_nums;
SET SQL_SAFE_UPDATES = 1;

-- 8. Check the result
-- (a) accounts per role: each should be at least 100
SELECT p.profile_name, COUNT(*) AS accounts
FROM user_accounts ua JOIN user_profiles p ON p.profileid = ua.profileid
GROUP BY p.profile_name;

-- (b) totals: categories >= 100, companies >= 100, idps >= 200
SELECT (SELECT COUNT(*) FROM categories) AS categories,
       (SELECT COUNT(*) FROM companies)  AS companies,
       (SELECT COUNT(*) FROM idps)       AS idps,
       (SELECT COUNT(*) FROM favourites) AS favourites,
       (SELECT COUNT(*) FROM idp_views)  AS views;

-- (c) designers named designerNNN without a company, and companies without an IDP (both should be 0)
SELECT (SELECT COUNT(*) FROM user_accounts ua
        LEFT JOIN companies co ON co.userid = ua.userid
        WHERE ua.username LIKE 'designer___' AND co.companyid IS NULL) AS designers_without_company,
       (SELECT COUNT(*) FROM companies co
        LEFT JOIN idps i ON i.companyid = co.companyid
        WHERE i.idpid IS NULL) AS companies_without_idp;
