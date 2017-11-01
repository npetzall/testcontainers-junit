CREATE DATABASE by_root;
CREATE TABLE by_root.in_by_root (
  id TINYINT PRIMARY KEY,
  text VARCHAR(50)
);

INSERT INTO by_root.in_by_root VALUES (1,'added by script');

GRANT SELECT,INSERT ON by_root.* TO 'test'@'%';
FLUSH PRIVILEGES;