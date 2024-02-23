

### Local Kafka Commands
```
# start server
bin/kafka-server-start.sh config/kraft/server.properties
## changed server props to disable auto topic creation

# create topic
bin/kafka-topics.sh --create   --topic test1 --bootstrap-server localhost:9092
bin/kafka-topics.sh --describe --topic test1 --bootstrap-server localhost:9092


# list / delete consumer
bin/kafka-consumer-groups.sh --list --bootstrap-server localhost:9092
bin/kafka-consumer-groups.sh --delete --group test1-consumer --bootstrap-server localhost:9092


# create arbitrary topic to consume from topic
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test1 --from-beginning --group my-created-consumer-group
```