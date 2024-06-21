package com.godeltech.kafkaproducer.controller

import com.godeltech.config.KafkaTestContainersConfiguration
import com.godeltech.kafkaproducer.KafkaProducerApplication
import com.godeltech.kafkaproducer.enums.TransactionType
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
import java.time.format.DateTimeFormatter

@SpringBootTest(classes = KafkaProducerApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = KafkaTestContainersConfiguration)
@Testcontainers
class TransactionControllerTest extends Specification {

    public static final String TRANSACTION_TOPIC = "transaction"

    public static final DateTimeFormatter DATA_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    TestRestTemplate testRestTemplate

    @LocalServerPort
    int port

    @Shared
    def static kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", TRANSACTION_TOPIC + ":1:1")

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

    def 'Should create transaction successfully'() {
        given:
            def transaction = [
                bank: "revolut", clientId: 1,
                orderType: TransactionType.INCOME,
                quantity: 2, price: 2000.0,
                createdAt: "2024-06-13 10:32:25"
            ]
            def transactionAsJson = JsonOutput.toJson(transaction)
            def entity = createHttpEntity(transactionAsJson)
        when:
            consumer.subscribe(List.of(TRANSACTION_TOPIC))
            def response = testRestTemplate.postForEntity("http://localhost:" + port + "/transactions", entity, String)
        then:
            ConsumerRecord<Long, String> record = consumer.poll(Duration.ofSeconds(10)).iterator().next()
        and:
            response.statusCode == HttpStatus.OK
            record
            record.key == transaction.clientId
            record.value == transactionAsJson
    }

    def 'Should failed return 400 cause body is not valid - clientId is missing'() {
        given:
        def transaction = [
                bank: "revolut", orderType: TransactionType.INCOME,
                quantity: 2, price: 2000.0,
                createdAt: "2024-06-13 10:32:25"
        ]
        def transactionAsJson = JsonOutput.toJson(transaction)
        def entity = createHttpEntity(transactionAsJson)
        when:
            consumer.subscribe(List.of(TRANSACTION_TOPIC))
            def response = testRestTemplate.postForEntity("http://localhost:" + port + "/transactions", entity, String)
        then:
            response.statusCode == HttpStatus.BAD_REQUEST
            response.body.contains("'clientId': rejected value [null];")
            response.body.contains("must not be null")
    }

    def createHttpEntity(String body) {
        def headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        return new HttpEntity<>(body, headers)
    }
}
