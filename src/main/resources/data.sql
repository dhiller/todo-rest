INSERT INTO USER
       (ID, USERNAME, REALNAME  )
VALUES (1,  'jdoe',   'Jane Doe');

INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (1,  1,       FALSE, 'Clean up the kitchen');
INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (2,  1,       FALSE, 'Empty trashcan');
INSERT INTO TODO
        (ID, USER_ID, DONE,  CONTENT)
 VALUES (3,  1,       TRUE,  'Buy milk');