#!/bin/bash

echo "Request authentication token"
TOKEN=$(curl -s -XPOST 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t')

echo -e "\nRequest todo list:"
curl "http://localhost:8080/todos?auth=$TOKEN"

echo -e "\nRequest with filter:"
curl -H'Content-Type: application/json' -XGET -d '{"id":null,"done":null,"content":".*kitchen.*"}' "http://localhost:8080/todos?auth=$TOKEN"