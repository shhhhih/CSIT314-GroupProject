-- ============================================================
-- IDP Platform: (A) remove the test category "yes"  (B) add photos to every IDP
-- Database: idp_platform (MySQL 8)
-- Safe to run more than once.
-- ============================================================
USE idp_platform;
SET SQL_SAFE_UPDATES = 0;

-- ------------------------------------------------------------
-- A. Remove the category "yes"
--    The app refuses to delete a category that still has IDPs, so first
--    move those IDPs to the other categories (spread out evenly), rename
--    them so they no longer say "yes", then delete the category.
-- ------------------------------------------------------------
SELECT COUNT(*) AS idps_still_in_yes
FROM idps
WHERE categoryid = (SELECT categoryid FROM categories WHERE category_name = 'yes');

UPDATE idps i
JOIN (SELECT categoryid, category_name,
             ROW_NUMBER() OVER (ORDER BY categoryid) AS rn
      FROM categories
      WHERE category_name <> 'yes') c
  ON c.rn = (i.idpid % (SELECT COUNT(*) FROM categories WHERE category_name <> 'yes')) + 1
SET i.title       = IF(i.title LIKE 'yes %', CONCAT(c.category_name, SUBSTRING(i.title, 4)), i.title),
    i.description = REPLACE(i.description, ' in the yes style,', CONCAT(' in the ', c.category_name, ' style,')),
    i.categoryid  = c.categoryid
WHERE i.categoryid = (SELECT categoryid FROM categories WHERE category_name = 'yes');

DELETE FROM categories WHERE category_name = 'yes';

SELECT COUNT(*) AS yes_categories_left FROM categories WHERE category_name = 'yes';   -- expected 0

-- ------------------------------------------------------------
-- B. Photos: 4 photos for every IDP (skipped if that exact photo already exists)
--    Photo 1 becomes the cover, unless the IDP already has a cover
--    (for example one you uploaded yourself in the designer form).
--    The photos are placeholder pictures from picsum.photos, so the
--    computer needs internet to show them.
-- ------------------------------------------------------------
INSERT INTO idp_images (idpid, image_url, is_cover)
SELECT i.idpid,
       CONCAT('https://picsum.photos/seed/idp', i.idpid, '-', k.n, '/800/600'),
       IF(k.n = 1 AND NOT EXISTS (SELECT 1 FROM idp_images x WHERE x.idpid = i.idpid AND x.is_cover = 1), 1, 0)
FROM idps i
JOIN (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) k
WHERE NOT EXISTS (SELECT 1 FROM idp_images y
                  WHERE y.idpid = i.idpid
                    AND y.image_url = CONCAT('https://picsum.photos/seed/idp', i.idpid, '-', k.n, '/800/600'));

SET SQL_SAFE_UPDATES = 1;

-- ------------------------------------------------------------
-- Check the result
-- ------------------------------------------------------------
SELECT (SELECT COUNT(*) FROM categories)                         AS categories,
       (SELECT COUNT(*) FROM idps)                               AS idps,
       (SELECT COUNT(*) FROM idp_images)                         AS photos,
       (SELECT COUNT(*) FROM idps i
         WHERE NOT EXISTS (SELECT 1 FROM idp_images x WHERE x.idpid = i.idpid)) AS idps_without_photo,
       (SELECT COUNT(*) FROM idps i
         WHERE (SELECT COUNT(*) FROM idp_images x WHERE x.idpid = i.idpid AND x.is_cover = 1) <> 1) AS idps_without_exactly_one_cover;
