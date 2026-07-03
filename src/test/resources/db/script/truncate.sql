set foreign_key_checks = 0;

truncate table organization;
truncate table employee;
truncate table message;
truncate table user_group;
truncate table employee_user_group;
truncate table message_recipient;
truncate table users;

set foreign_key_checks = 1;