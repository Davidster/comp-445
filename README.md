## Build instructions

- Install Java 11
- Install Maven

```
# compile
mvn clean install

# linux/macos usage
./httpc/target/httpc get http://httpbin.org/get
./httpfs/target/httpfs

# windows usage (untested)
java -jar httpc/target/httpc get http://httpbin.org/get
java -jar httpfs/target/httpfs
```
