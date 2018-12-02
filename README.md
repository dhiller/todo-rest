# TODO REST service example

## Requirements

* for building and running the service binary
    * [gradle](https://gradle.org/) and 
    * [java development kit](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html) 
* [curl](https://curl.haxx.se/) for using the test script (see below)

## Caveats

### Transport encryption missing

The service does **NOT** provide encryption over the wire, this must be done on the reverse
proxy level.

## How to run the service

From the command line execute

    gradle bootRun

## Example usage

A test script demonstrating how to use the service with [curl](https://curl.haxx.se/) is provided [here](src/test/scripts/test.sh).

### Create authentication token

In general a client first has to authenticate against the service, after which she receives an authentication
token that must be provided to any endpoint provided.

    > curl -d '' 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t'
    53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4

### Request todo list

Providing the token the client can now request the list of todo items:

    > curl 'http://localhost:8080/todos?auth=53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4'
    [{"id":1,"done":false,"content":"Clean up the kitchen"},{"id":2,"done":false,"content":"Empty trashcan"},{"id":3,"done":true,"content":"Buy milk"}]
    
Supplying a filter object the client can request only items matching content:

    > curl -v -H'Content-Type: application/json' -XGET -d '{"id":null,"done":null,"content":".*kitchen.*"}' 'http://localhost:8080/todos?auth=53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4'
