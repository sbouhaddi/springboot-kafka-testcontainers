package dev.sabri.kafkastream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.github.javafaker.Faker.instance;
import static java.lang.System.out;
import static org.springframework.messaging.support.MessageBuilder.withPayload;

@SpringBootApplication
public class KafkaStreamApplication {

    public static void main(String[] args) {
        createSpringApplication().run(args);
    }

    public static SpringApplication createSpringApplication() {
        return new SpringApplication(KafkaStreamApplication.class);
    }


    @Bean
    Supplier<Message<String>> produceChuckNorris() {
        return () -> withPayload(instance().chuckNorris().fact()).build();
    }

    @Bean
    Consumer<Message<String>> consumeChuckNorris() {
        return s -> out.println("FACT: \u001B[3m «" + s.getPayload() + "\u001B[0m»");
    }

}
