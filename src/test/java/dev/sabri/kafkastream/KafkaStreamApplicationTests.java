package dev.sabri.kafkastream;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class KafkaStreamApplicationTests extends AbstractIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamApplicationTests.class);

    @Autowired
    KafkaAdmin admin;
    @Autowired
    Environment env;

    @Test
    void testCreationOfTopicAtStartup() throws InterruptedException, ExecutionException {
        AdminClient client = AdminClient.create(admin.getConfigurationProperties());
        Collection<TopicListing> topicList = client.listTopics().listings().get();
        assertNotNull(topicList);
        assertEquals("facts", topicList
                .stream()
                .map(TopicListing::name)
                .findFirst().get());
    }

    @Test
    void testPublishFacts() throws InterruptedException, ExecutionException {
        String topicName = "facts";
        NewTopic topic = TopicBuilder.name(topicName).build();

        AdminClient client = AdminClient.create(admin.getConfigurationProperties());
        client.createTopics(Collections.singletonList(topic));

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                env.getProperty("spring.kafka.properties.bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "myGroup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Collections.singletonList(topicName));
        

        Collection<TopicListing> topicList = client.listTopics().listings().get();
        assertEquals(1, topicList.size());
        String topicNameList = topicList
                .stream()
                .map(TopicListing::name)
                .toList()
                .stream()
                .findFirst().get();
        String expectedTopicNameList = "facts";
        assertTrue(topicNameList.contains(expectedTopicNameList) && expectedTopicNameList.contains(topicNameList));

        await().atMost(Duration.ofSeconds(20)).until(() -> {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(10));
            if (records.isEmpty()) {
                return false;
            }
            records.forEach(r -> logger.info(r.topic() + " *** " + r.key() + " *** " + r.value()));
            Assertions.assertThat(records.count()).isPositive();
            return true;
        });

        consumer.close();

    }

}
