#!/bin/bash

#(mockserver -serverPort 8081)&

echo "Request authentication token"
TOKEN=$(curl -s -XPOST 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t')

echo -e "\nAdd remote listener:"
curl -s -XPUT -d '{"endpoint":"http://localhost:8081/todoItemUpdate"}' \
    -H 'Content-Type: application/json' \
    "http://localhost:8080/updates?auth=$TOKEN"

echo -e "\nGet list of remote listeners for todo items of user:"
curl -XGET "http://localhost:8080/updates?auth=$TOKEN"

echo -e "\nRequest todo list:"
curl "http://localhost:8080/todos?auth=$TOKEN"

TODO_FILTER='{"id":null,"done":null,"content":".*kitchen.*"}'
echo -e "\nRequest with filter: $TODO_FILTER"
curl -XGET -d "$TODO_FILTER" \
     -H'Content-Type: application/json' \
    "http://localhost:8080/todos?auth=$TOKEN"

echo -e "\nUpdate todo item:"
curl -d '{"id":null,"done":true,"content":"Clean up the kitchen (finished)"}' \
     -H 'Content-Type: application/json' \
     "http://localhost:8080/todos/1?auth=$TOKEN"

echo -e "\nFetch todo item:"
curl -H 'Content-Type: application/json' \
     "http://localhost:8080/todos/1?auth=$TOKEN"

NEW_ITEM='{"id":null,"done":false,"content":"Mow the lawn"}'
echo -e "\n\nInsert new todo item: $NEW_ITEM"
NEW_ITEM=$(curl -s -XPUT -d "$NEW_ITEM" \
     -H 'Content-Type: application/json' \
     "http://localhost:8080/todos?auth=$TOKEN")

NEW_ITEM_ID=$(echo "$NEW_ITEM" | grep -oE '"id":[0-9]+' | sed 's/"id"\://g')

echo -e "\nFetch new todo item:"
curl -H 'Content-Type: application/json' \
     "http://localhost:8080/todos/$NEW_ITEM_ID?auth=$TOKEN"

echo -e "\n\nDelete todo item:"
curl -s -XDELETE \
     -H 'Content-Type: application/json' \
     "http://localhost:8080/todos/$NEW_ITEM_ID?auth=$TOKEN"

echo -e "\nRequest todo list:"
curl "http://localhost:8080/todos?auth=$TOKEN"

