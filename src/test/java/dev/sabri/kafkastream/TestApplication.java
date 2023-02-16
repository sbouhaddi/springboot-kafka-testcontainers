package dev.sabri.kafkastream;

public class TestApplication {
    public static void main(String[] args) {
        var application = KafkaStreamApplication.createSpringApplication();

        // Here we add the same initializer as we were using in our tests...
        application.addInitializers(new AbstractIntegrationTest.Initializer());

        // ... and start it normally
        application.run(args);
    }
}