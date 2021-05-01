### kafka-stream
- This example contains the protobuf format of [confluent join example](https://kafka-tutorials.confluent.io/join-a-stream-to-a-table/kstreams.html) running on apache kafka

### pre-requisite
- ensure docker and docker-compose is installed

### kafka start
 ```shell script
$ docker-compose up -d
```
### create topics
 ```shell script
$ bin/kafka-topics.sh --create --topic movies --bootstrap-server localhost:9093
$ bin/kafka-topics.sh --create --topic rekeyed-movies --bootstrap-server localhost:9093
$ bin/kafka-topics.sh --create --topic ratings --bootstrap-server localhost:9093
$ bin/kafka-topics.sh --create --topic rated-movies --bootstrap-server localhost:9093
```

### Build and Run
```shell script
$ ./mvnw clean install
$ ./mvnw spring-boot:run
```


### kafka stop
 ```shell script
$ docker-compose stop
$ docker-compose rm --force
```