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

After running the service a live ui containing an overview of provided methods and for trying out the service
is visible [here](http://localhost:8080/swagger-ui.html). This is provided using [Swagger](https://swagger.io/). 

A bash script demonstrating how to use the service with [curl](https://curl.haxx.se/) is provided 
[here](src/test/scripts/test.sh). Prior to execution, one should run the `mockserver` to see the 
watch callbacks (see below):

```bash
mockserver -serverPort 8081
``` 

### Create authentication token

In general a client first has to authenticate against the service, after which she receives an authentication
token that must be provided to any endpoint provided.

```bash
$ TOKEN=$(curl -d '' 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t')
53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4
```

### Add a remote watch

To be notified of changes for it's own todo items, a client can register a callback url that will be called, should
a todo item be changed.

```bash
curl -s -XPUT -d '{"endpoint":"http://localhost:8081/todoItemUpdate"}' \
    -H 'Content-Type: application/json' \
    "http://localhost:8080/updates?auth=$TOKEN"
```

### Request todo list

Providing the token the client can now request the list of todo items:

```bash
$ curl "http://localhost:8080/todos?auth=$TOKEN"
[{"id":1,"done":false,"content":"Clean up the kitchen"},{"id":2,"done":false,"content":"Empty trashcan"},{"id":3,"done":true,"content":"Buy milk"}]
```
    
Supplying a filter object the client can request only items matching content:

```bash
$ curl -H'Content-Type: application/json' -XGET \
    -d '{"id":null,"done":null,"content":".*kitchen.*"}' \
    "http://localhost:8080/todos?auth=$TOKEN"
```

### Insert, update and delete

Respective examples are provided in [test.sh](src/test/scripts/test.sh).

## Caveats

### Transport not encrypted

The service does **NOT** provide encryption over the wire, this must be done on the reverse
proxy level.

### Authentication tokens never expire

Although authentication tokens are not persisted, they do **never expire** as long as the service is running.
