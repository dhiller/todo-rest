# TODO list example

## Requirements

* java virtual machine
* gradle

## Example usage

Run application:

    gradle bootRun

Create authentication:

    > curl -d '' 'http://localhost:8080/authenticate?username=jdoe&password=mys3cr3t'
    53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4

Request todo list:

    > curl 'http://localhost:8080/todos?auth=53483c8a-6c38-4e2f-b96f-4a8eb0e1cfe4'
    [{"id":1,"done":false,"content":"Clean up the kitchen"},{"id":2,"done":false,"content":"Empty trashcan"},{"id":3,"done":true,"content":"Buy milk"}]