INSERT INTO USER
       (ID, USERNAME, REALNAME,   PASSWORD,                                                           SALT)
VALUES (1,  'jdoe',   'Jane Doe', '162d49fbbd0aaef3081a551ca7c0e02ba515a8dafcd1cc9019bbaa46541c2470', '15b7ead44ac74af1a1110cbb47448926');
INSERT INTO USER
       (ID, USERNAME, REALNAME,   PASSWORD,                                                           SALT)
VALUES (2,  'jdae',   'Jane Dae', '0cead12d8ac97b2376468d8c30d7e18b83b071dac349f8498cb639fcba9f8363', '361eccd5515b4dbf829f803c86805121');

INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (1,  1,       FALSE, 'Clean up the kitchen');
INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (2,  1,       FALSE, 'Empty trashcan');
INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (3,  1,       TRUE,  'Buy milk');