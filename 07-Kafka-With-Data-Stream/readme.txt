https://stream.wikimedia.org/v2/stream/recentchange


https://esjewett.github.io/wm-eventsource-demo/


Step 1: create a new spring boot project as producer

- Web
- Lombook
- Spring Kafka


application.properties

spring.port=9001

spring.kafka.producer.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer= org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer= org.apache.kafka.common.serialization.StringSerializer



Step 2: Configure Producer and Create a topic

config/KafkaTopicConfig 


@Configuration
public class KafkaTopicConfig {

    public NewTopic topic(){
        return TopicBuilder.name("wikimedia_recent_change")
                .build();
    }
}



Step 3: wikimedia Producer and event handler implementation


add two more dependencies

https://mvnrepository.com/artifact/com.launchdarkly/okhttp-eventsource
https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core


pom.xml

   <dependency>
            <groupId>com.launchdarkly</groupId>
            <artifactId>okhttp-eventsource</artifactId>
            <version>2.5.0</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp -->
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.9.3</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-core</artifactId>
            <version>2.13.2</version>
        </dependency>

        <!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.13.2.2</version>
        </dependency>






Step4: create a new spring boot project for consumer

- web
- kafka

Step5: add configuration

application.properties


server.port=9003

spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=myGroup
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer




Step 6: implement consumer


@Service
public class KafkaDatabaseConsumer {
    private static  final Logger LOGGER = LoggerFactory.getLogger(KafkaDatabaseConsumer.class);

    @KafkaListener(
            topics = "wikimedia_recent_change",
            groupId = "myGroup"
    )
    public void consume(String eventMessage){
        LOGGER.info(String.format("Event message received -> %s", eventMessage));
    }

}


Step 7: configuare MySQL Database

 <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

   <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>



    @KafkaListener(
            topics = "wikimedia_recent_change",
            groupId = "myGroup"
    )
    public void consume(String eventMessage){
        LOGGER.info(String.format("Event message received -> %s", eventMessage));

        WikimediaData wikimediaData = new WikimediaData();
        wikimediaData.setWikiEventData(eventMessage);
        dataRepository.save(wikimediaData);
    }

}





@Service
public class KafkaDatabaseConsumer {
    private static  final Logger LOGGER = LoggerFactory.getLogger(KafkaDatabaseConsumer.class);

    private WikimediaDataRepository dataRepository;

    public KafkaDatabaseConsumer(WikimediaDataRepository dataRepository){
        this.dataRepository = dataRepository;
    }

    @KafkaListener(
            topics = "wikimedia_recent_change",
            groupId = "myGroup"
    )
    public void consume(String eventMessage){
        LOGGER.info(String.format("Event message received -> %s", eventMessage));

        WikimediaData wikimediaData = new WikimediaData();
        wikimediaData.setWikiEventData(eventMessage);
        dataRepository.save(wikimediaData);
    }

}




# Spring Boot Kafka Event Driven Microservice Architectire with Multiple COnsumer Group


# What is Event Driven architecture?
- Event driven architecture is a softweare design pattern in which decoupled application 
application can asynchronously publish and subscribe to event view an event broker/messge broker

- in an event driven architecture application communicate with each other by sending and receving events or messages

- event driven architecture is often referred to as "asynchrnous" communication

- event driven app can be created in any programming language because event driven is a programming apprach not a language

- An event driven architecture is loosely coupled


# Advatanges of EDA
- improves flexibility and maintainability
- high scalability
- improved avaliability



# Practical Implementation

Step 1: Install and Setup Kafka 

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


Step 2: create 3 microservices

order-service
email-service
stock-service


- web
- spring kafka
- lombook

open order service in IntellJIdea


Step 3: Create Order and OrderEvent DTO classes

order-service

dto/Order

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String name;
    private int qty;
    private double price;
}


dto/OrderEvent
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private String message;
    private String status;
    private Order order;
}

Step 4: configure Kafka Producer in OrderService Microservices

application.properties

server.port=9001

spring.kafka.producer.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.topic.name=order_topic


Step 5: configure Kafka topic in OrderService 

config/KafkaTopicConfig 

@Configuration
public class KafkaTopicConfig {
    @Value("${spring.kafka.topic.name}")
    private String topicName;

    // spring bean for kafka topic
    @Bean
    public NewTopic topic(){
        return TopicBuilder.name(topicName)
                .build();
    }
}


Step 6: create kafka producer in order service

service/OrderProducer 


@Service
public class OrderProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProducer.class);
    
    @Autowired
    private NewTopic topic;
    
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public void sendMessage(OrderEvent orderEvent){
        LOGGER.info(String.format("Order event => %s", orderEvent.toString()));
        
        // create message
        Message<OrderEvent> message = MessageBuilder
                .withPayload(orderEvent)
                .setHeader(KafkaHeaders.TOPIC, topic.name())
                .build();
    }
}


Step 7: create a REST API and test Kafka Producer in Order Service

controller/OrderController


@RequestMapping("/api/v1")
@RestController
public class OrderController {
    @Autowired
    private OrderProducer orderProducer;

    @PostMapping("/orders")
    public String placeOrder(@RequestBody Order order){
        order.setOrderId(UUID.randomUUID().toString());
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setStatus("PENDING");
        orderEvent.setMessage("order status is in pending state");
        orderEvent.setOrder(order);

        orderProducer.sendMessage(orderEvent);
        return "Order placed successfully";
    }
}


Step 8: Test Api with postman


Step 9: configure kafka consumer in stockservice

appilaction.properties

server.port=9002

spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=stock
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.topic.name=order_topic


Step 10: create kafka consumer in stock service
