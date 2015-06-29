--This shows database structure.

CREATE TABLE income_expense IF NOT EXISTS
(
  id     INTEGER       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  amount DECIMAL(10,2) NOT NULL,
  name   VARCHAR(255)  NOT NULL,
  ondate DATE          NOT NULL
);

