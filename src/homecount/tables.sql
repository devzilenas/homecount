--This shows database structure.

CREATE TABLE income_expense
(
  id INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
  amount VARCHAR(255) NOT NULL,--DECIMAL(10,2) NOT NULL,
  name VARCHAR(255) NOT NULL,
  ondate VARCHAR(255) NOT NULL-- DATE NOT NULL
);

