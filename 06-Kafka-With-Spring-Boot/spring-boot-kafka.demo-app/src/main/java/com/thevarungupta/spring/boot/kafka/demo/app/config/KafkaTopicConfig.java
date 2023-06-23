package com.thevarungupta.spring.boot.kafka.demo.app.config;

import com.thevarungupta.spring.boot.kafka.demo.app.utils.Constants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

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
