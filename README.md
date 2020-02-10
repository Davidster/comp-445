## Build instructions

- Install Java 11
- Install Maven

```
# compile
mvn clean install

# linux/macos usage
./target/httpc get http://httpbin.org/get

# windows usage (untested)
java -jar target/httpc get http://httpbin.org/get
```
