package com.godeltech.kafkaproducer.controller

import com.godeltech.config.KafkaTestContainersConfiguration
import com.godeltech.kafkaproducer.KafkaProducerApplication
import groovy.json.JsonOutput
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration

@SpringBootTest(classes = KafkaProducerApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = KafkaTestContainersConfiguration)
@Testcontainers
class ClientControllerTest extends Specification {

    public static final String CLIENT_TOPIC = "client"

    @Autowired
    TestRestTemplate testRestTemplate

    @LocalServerPort
    int port

    @Shared
    def static kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", CLIENT_TOPIC + ":1:1")

    @Shared
    KafkaConsumer<Long, String> consumer

    private static Properties consumerProperties() {
        def properties = new Properties()
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers())
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group")
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.name)
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.name)
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        return properties
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    def setupSpec() {
        kafkaContainer.start()
        consumer = new KafkaConsumer<>(consumerProperties())
    }

    def cleanupSpec() {
        kafkaContainer.stop()
    }

    def 'Kafka container should be running'() {
        expect:
        kafkaContainer.isRunning()
    }

    def 'Should create client successfully'() {
        given:
            def client = [clientId: 1, email: "test@test.com"]
            def clientAsJson = JsonOutput.toJson(client)
            def entity = createHttpEntity(clientAsJson)
        when:
            consumer.subscribe(List.of(CLIENT_TOPIC))
            def response = testRestTemplate.postForEntity("http://localhost:" + port + "/clients", entity, String)
        then:
            ConsumerRecord<Long, String> record = consumer.poll(Duration.ofSeconds(10)).iterator().next()
        and:
            response.statusCode == HttpStatus.OK
            record
            record.key == client.clientId
            record.value == clientAsJson

    }

    def 'Should failed return 400 cause body is not valid'() {
        given:
            def client = [clientId: 1, email: "testtestcom"]
            def clientAsJson = JsonOutput.toJson(client)
            def entity = createHttpEntity(clientAsJson)
        when:
            consumer.subscribe(List.of(CLIENT_TOPIC))
            def response = testRestTemplate.postForEntity("http://localhost:" + port + "/clients", entity, String)
        then:
            response.statusCode == HttpStatus.BAD_REQUEST
            response.body.contains("must be a well-formed email address")
    }

    def createHttpEntity(String body) {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return new HttpEntity<>(body, headers)
    }

}
