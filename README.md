# TODO REST service example

## Requirements

* for building and running the service binary
    * [gradle](https://gradle.org/) and 
    * [java development kit](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html) 
* [curl](https://curl.haxx.se/) for using the test script (see below)
* [mockserver](http://www.mock-server.com/) for running the example with external notification
  can be installed with brew:
  ```bash
  $ brew install mockserver
  ```

## How to run the service

To build and run the service directly from the command line

```bash
$ gradle bootRun
```
    
To build the jar file including all dependencies 

```bash
$ gradle build
```
    
To run the jar file

```bash
$ java -jar build/libs/todo-rest-0.1.0.jar
```

## Example usage

A test script demonstrating how to use the service with [curl](https://curl.haxx.se/) is provided [here](src/test/scripts/test.sh).

Prior to execution, one should run the `mockserver` to see the notification request:

```bash
mockserver -serverPort 8081
``` 

### Create authentication token

In general a client first has to authenticate against the service, after which she receives an authentication
token that must be provided to any endpoint provided.

```bash
$ curl -d '' 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t'
53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4
```

### Request todo list

Providing the token the client can now request the list of todo items:

```bash
$ curl 'http://localhost:8080/todos?auth=53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4'
[{"id":1,"done":false,"content":"Clean up the kitchen"},{"id":2,"done":false,"content":"Empty trashcan"},{"id":3,"done":true,"content":"Buy milk"}]
```
    
Supplying a filter object the client can request only items matching content:

```bash
$ curl -v -H'Content-Type: application/json' -XGET \
    -d '{"id":null,"done":null,"content":".*kitchen.*"}' \
    'http://localhost:8080/todos?auth=53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4'
```

## Caveats

### Transport encryption missing

The service does **NOT** provide encryption over the wire, this must be done on the reverse
proxy level.

### Authentication tokens never expire

Although authentication tokens are not persisted, they do **never expire** as long as the service is running.
