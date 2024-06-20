package com.godeltech.kafkaconsumer.client

import com.godeltech.config.KafkaTestContainersConfiguration
import com.godeltech.kafkaconsumer.KafkaConsumerApplication
import com.godeltech.kafkaconsumer.repository.ClientRepository
import groovy.json.JsonOutput
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import static org.apache.kafka.clients.producer.ProducerConfig.*

@SpringBootTest(classes = KafkaConsumerApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = KafkaTestContainersConfiguration)
@Testcontainers
@DirtiesContext
class ClientMessageClientTest extends Specification {

    private static final String CLIENT_TOPIC = "client"
    private static final String DB_NAME = "postgres"
    private static final String DB_USER = "test_user"
    private static final String DB_PASSWORD = "test123"

    @Autowired
    ClientMessageClient clientMessageClient

    @Autowired
    ClientRepository clientRepository

    @Shared
    def static kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", CLIENT_TOPIC + ":1:1")
    @Shared
    @Container
    def static postgresContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName(DB_NAME)
            .withUsername(DB_USER)
            .withPassword(DB_PASSWORD)

    @Shared
    KafkaProducer<Long, String> producer

    private static Properties producerProperties() {
        def props = new Properties()
        props.put(BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers())
        props.put(KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class)
        props.put(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
        props.put(ACKS_CONFIG, "all")
        return props
    }

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers)
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl)
        registry.add("spring.datasource.username", postgresContainer::getUsername)
        registry.add("spring.datasource.password", postgresContainer::getPassword)
        registry.add("spring.datasource.driver-class-name", postgresContainer::getDriverClassName)
    }

    def setupSpec() {
        kafkaContainer.start()
        postgresContainer.start()
        producer = new KafkaProducer<>(producerProperties())
    }

    def cleanupSpec() {
        kafkaContainer.stop()
        postgresContainer.stop()
    }

    def 'Kafka container should be running'() {
        expect:
            kafkaContainer.isRunning()
    }

    def 'Postgres container should be running'() {
        expect:
            postgresContainer.isRunning()
    }

    def 'Should successfully store client message from topic to postgres db'() {
        given:
            def client = [clientId: 1, email: "test@ts.ts"]
            def messageClient = JsonOutput.toJson(client)
        when:
            producer.send(new ProducerRecord<Long, String>(CLIENT_TOPIC, client.clientId.toLong(), messageClient))
            TimeUnit.SECONDS.sleep(2)
            def executor = Executors.newSingleThreadExecutor()
            def future = executor.submit({
                clientMessageClient.processClient()
            })
            TimeUnit.SECONDS.sleep(5)
            future.cancel(true)
        then:
            def actual = clientRepository.findById(client.clientId.toLong())
            actual
            actual.get().id == client.clientId
            actual.get().email == client.email
    }
}
