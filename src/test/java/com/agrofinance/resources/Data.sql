-- Auto-executed by Spring Boot against the test H2 database, AFTER
-- Hibernate creates the schema (see defer-datasource-initialization
-- in application-test.yml). Seeds the roles every registration test
-- depends on — without this, EVERY register() call fails with
-- "Unknown role" since the role table starts empty.
INSERT INTO role (name) VALUES ('FARMER'), ('BANK_OFFICER'), ('ADMIN');
 


































