# Database setup

1. Install MySQL 8 and create an empty database:
   CREATE DATABASE idp_platform;
2. Set environment variables DB_USERNAME and DB_PASSWORD.
3. Run the application once (./mvnw spring-boot:run) so it creates the tables,
   role profiles and the admin1 account. Then stop it.
4. Run idp_platform_bulk_data.sql
   (100 categories, 100 accounts per role, 100 companies, 200 IDPs, 200 favourites, 300 views).
5. Run idp_platform_fix_yes_and_photos.sql
   (removes the category "yes" and adds 4 photos per IDP; needs internet access for the photos).
Test account password for the generated users: Password123