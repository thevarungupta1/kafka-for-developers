package com.thevarungupta.spring.boot.kafka.demo.app.kafka;

import com.thevarungupta.spring.boot.kafka.demo.app.payload.User;
import com.thevarungupta.spring.boot.kafka.demo.app.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class JsonKafkaConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonKafkaConsumer.class);

    @KafkaListener(topics = Constants.TOPIC_NAME_JSON, groupId = "myGroup")
    public void consume(User user){
        LOGGER.info(String.format("Json message received -> %s", user.toString()));
    }
}
