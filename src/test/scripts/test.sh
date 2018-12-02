#!/bin/bash

#(mockserver -serverPort 8081)&

echo "Request authentication token"
TOKEN=$(curl -s -XPOST 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t')

echo -e "\nRequest todo list:"
curl "http://localhost:8080/todos?auth=$TOKEN"

echo -e "\nRequest with filter:"
curl -XGET -d '{"id":null,"done":null,"content":".*kitchen.*"}' \
     -H'Content-Type: application/json' \
    "http://localhost:8080/todos?auth=$TOKEN"

echo -e "\nAdd remote listener:"
curl -XPUT -d '{"endpoint":"http://localhost:8081/todoItemUpdate"}' \
    -H 'Content-Type: application/json' \
    "http://localhost:8080/updates?auth=$TOKEN"

echo -e "\nAdd (non existing) remote listener:"
curl -XPUT -d '{"endpoint":"http://localhost:9999/nonExistingEndpoint"}' \
    -H 'Content-Type: application/json' \
    "http://localhost:8080/updates?auth=$TOKEN"

echo -e "\nGet list of remote listeners for todo items of user:"
curl -XGET "http://localhost:8080/updates?auth=$TOKEN"

echo -e "\nUpdate todo item:"
curl -d '{"id":null,"done":true,"content":"Clean up the kitchen (finished)"}' \
     -H 'Content-Type: application/json' \
     "http://localhost:8080/todos/1?auth=$TOKEN"

echo -e "\nFetch todo item:"
curl -XGET \
     -H 'Content-Type: application/json' \
     "http://localhost:8080/todos/1?auth=$TOKEN"
