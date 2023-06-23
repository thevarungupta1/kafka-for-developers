# Kakfa Integration With Spring Boot


Step 0: create docker compose file to start kafka and zookeeper


docker-compose.yml

version: '3'

services:
  zookeeper:
    image: wurstmeister/zookeeper
    container_name: zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka
    container_name: kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: localhost
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181




> docker-compose up -d


Step 1: create and setup spring boot project with kafka and wen as dependenices

https://start.spring.io/
- Spring Web
- Spring for Apache Kafka

Step 2: configure the kafka producer and consumer

application.properties

server.port=9001

# Kafka Consumer
spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=myGroup
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer

# Kafka Producer
spring.kafka.producer.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer



Step 3: create kafka topic

config/KafkaTopicConfig


@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic testTopic(){
        return TopicBuilder.name("test-topic")
                .build();
    }
}

Step 4: create kafka producer

kafka/KafkaProducer

@Service
public class KafkaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class); 
    private KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message){
        LOGGER.info(String.format("Message send %s", message));
        kafkaTemplate.send(Constants.TOPIC_NAME, message);
    }
}


utils/Constans

public class Constants {
    public static final String TOPIC_NAME = "test-topic2";
}



Step 5: create a RestFull API to send a message;

controller/MessageController


@RequestMapping("/api/v1/kafka")
@RestController
public class MessageController {
    private KafkaProducer kafkaProducer;

    public MessageController(KafkaProducer kafkaProducer){
        this.kafkaProducer = kafkaProducer;
    }

    // http://localhost:9001/api/v1/kafka/publish?message=hello-world
    @GetMapping("/publish")
    public ResponseEntity<String> publish(@RequestParam("message") String message){
        kafkaProducer.sendMessage(message);
        return ResponseEntity.ok("Message send to the topic");
    }
}



Step 6: create the kafka consumer

kafka/KafkaConsumer

@Service
public class KafkaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

    @KafkaListener(topics = Constants.TOPIC_NAME, groupId = "myGroup")
    public void consume(String message){
        LOGGER.info(String.format("Message received -> %s", message));
    }
}

Step 7: configure kafka producer and consumer for JSON message


How to send and recieve a java object as a JSON byte[] to and from Apache Kafka
Apache Kafka stores and transports byte[]. there are a number of build-in serializers and decerializers but it doesn't include any for JSON. Spring kafka create a JsonSerializer and JSOnDesializer which we can use to convert java Objects to and from JSON



application.properties - (update)
server.port=9001

# Kafka Consumer
spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=myGroup
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
#spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*

# Kafka Producer
spring.kafka.producer.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
#spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer



payload/User


public class User {
    public int id;
    private String firstName;
    private String lastName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}


Step 8: create kafka producrcer to produce JSON message

kafka/JsonKafkaProducer 

@Service
public class JsonKafkaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonKafkaProducer.class);
    private KafkaTemplate<String, User> kafkaTemplate;

    public JsonKafkaProducer(KafkaTemplate<String, User> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(User data){
        LOGGER.info(String.format("Message sent -> %s", data.toString()));
        Message<User> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, Constants.TOPIC_NAME)
                .build();
        kafkaTemplate.send(message);
    }
}

Step 9: create REST API to send JSON object

utils/Constants - (update)

public class Constants {
    public static final String TOPIC_NAME = "test-topic";
    public static final String TOPIC_NAME_JSON = "test-topic-json";
}



config/KafkaTopicConfig - (update)


@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic testTopic(){
        return TopicBuilder.name(Constants.TOPIC_NAME)
                .build();
    }

    @Bean
    public NewTopic testTopicJson(){
        return TopicBuilder.name(Constants.TOPIC_NAME_JSON)
                .build();
    }
}


kafka/JsonKafkaProducer - (update)


@Service
public class JsonKafkaProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonKafkaProducer.class);
    private KafkaTemplate<String, User> kafkaTemplate;

    public JsonKafkaProducer(KafkaTemplate<String, User> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(User data){
        LOGGER.info(String.format("Message sent -> %s", data.toString()));
        Message<User> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, Constants.TOPIC_NAME_JSON)
                .build();
        kafkaTemplate.send(message);
    }
}


controller/JsonMessageController 


@RestController
@RequestMapping("/api/v1/kafka")
public class JsonMessageController {

    private JsonKafkaProducer kafkaProducer;

    public JsonMessageController(JsonKafkaProducer kafkaProducer){
        this.kafkaProducer = kafkaProducer;
    }

    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestBody User user){
        kafkaProducer.sendMessage(user);
        return ResponseEntity.ok("Json message sent to kafka topic");
    }
}



Step 10: create kafka consumer to consume JSON message


kafka/JsonKafkaConsumer 

@Service
public class JsonKafkaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonKafkaConsumer.class);

    @KafkaListener(topics = Constants.TOPIC_NAME_JSON, groupId = "myGroup")
    public void consume(User user){
        LOGGER.info(String.format("Json message received -> %s", user.toString()));
    }
}







# reference link: https://docs.spring.io/spring-kafka/reference/html/#preface









