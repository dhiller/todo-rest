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

To build and run the service directly execute the following command:

```bash
$ gradle bootRun
```
    
To build the jar file including all dependencies execute the following command: 

```bash
$ gradle clean build
executing gradlew instead of gradle
...
> Task :test
...
BUILD SUCCESSFUL in 1m 2s
6 actionable tasks: 6 executed
```
    
To run the jar file execute the following command:

```bash
$ java -jar build/libs/todo-rest-0.1.0.jar

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::        (v2.0.5.RELEASE)
2018-12-02 22:35:13.901  INFO 35436 --- [           main] de.dhiller.todo.Application              : Starting Application ...
...
2018-12-02 22:35:25.779  INFO 35436 --- [           main] de.dhiller.todo.Application              : Started Application in 18.039 seconds (JVM running for 18.938)

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
token that must be provided to any endpoint available.

```bash
$ TOKEN=$(curl -d '' 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t')
53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4
```

### Add a remote watch

To be notified of changes for own todo items, a client can register a callback url that will be called if
a todo item is changed.

```bash
curl -s -XPUT -d '{"endpoint":"http://localhost:8081/todoItemUpdate"}' \
    -H 'Content-Type: application/json' \
    "http://localhost:8080/updates?auth=$TOKEN"
```

The service will then PUT updates against the registered update endpoint transmitting the new state, being
equivalent to this curl request:

```bash
curl -s -XPUT -d '{"updated":{"id":1,"done":true,"content":"Clean up the kitchen (finished)"},"method":"POST","timestamp":[2018,12,2,22,24,26,807945000]}' \
    -H 'Content-Type: application/json' \
    "http://localhost:8081/todoItemUpdate"
``` 

In case of error the PUT will **not be retried**!

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
