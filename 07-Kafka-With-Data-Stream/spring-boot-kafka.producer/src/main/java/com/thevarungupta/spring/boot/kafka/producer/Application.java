package com.thevarungupta.spring.boot.kafka.producer;

import com.thevarungupta.spring.boot.kafka.producer.kafka.WikimediaChangesProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(Application.class);
	}

	@Autowired
	private WikimediaChangesProducer wikimediaChangesProducer;

	@Override
	public void run(String... args) throws Exception{
		wikimediaChangesProducer.sendMessage();
	}

}
