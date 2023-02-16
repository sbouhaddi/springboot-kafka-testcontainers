package dev.sabri.kafkastream;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AbstractIntegrationTest.Initializer.class)
public abstract class AbstractIntegrationTest {

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        static Network network = Network.newNetwork();
        static KafkaContainer kafka = new KafkaContainer(DockerImageName
                .parse("confluentinc/cp-kafka:latest"))
                .withEmbeddedZookeeper()
                .withNetwork(network);


        public static Map<String, String> getProperties() {
            Startables.deepStart(kafka).join();

            return Map.of(
                    "spring.kafka.properties.bootstrap.servers", kafka.getBootstrapServers()
            );
        }

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            var env = context.getEnvironment();
            env.getPropertySources().addFirst(new MapPropertySource(
                    "testcontainers",
                    (Map) getProperties()
            ));
        }
    }
}