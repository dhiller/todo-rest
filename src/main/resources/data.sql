INSERT INTO USER
       (ID, USERNAME, REALNAME,   PASSWORD,                                                           SALT)
VALUES (1,  'jdoe',   'Jane Doe', '162d49fbbd0aaef3081a551ca7c0e02ba515a8dafcd1cc9019bbaa46541c2470', '15b7ead44ac74af1a1110cbb47448926');

INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (1,  1,       FALSE, 'Clean up the kitchen');
INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (2,  1,       FALSE, 'Empty trashcan');
INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (3,  1,       TRUE,  'Buy milk');