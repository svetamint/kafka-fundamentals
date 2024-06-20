package com.godeltech.kafkaconsumer.client

import com.godeltech.config.KafkaTestContainersConfiguration
import com.godeltech.kafkaconsumer.KafkaConsumerApplication
import com.godeltech.kafkaconsumer.entity.Client
import com.godeltech.kafkaconsumer.enums.TransactionType
import com.godeltech.kafkaconsumer.repository.ClientRepository
import com.godeltech.kafkaconsumer.repository.TransactionRepository
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

import static org.apache.kafka.clients.producer.ProducerConfig.ACKS_CONFIG
import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG

@SpringBootTest(classes = KafkaConsumerApplication, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = KafkaTestContainersConfiguration)
@Testcontainers
class TransactionMessageClientTest extends Specification {
    private static final String TRANSACTION_TOPIC = "transaction"
    private static final String DB_NAME = "postgres"
    private static final String DB_USER = "test_user"
    private static final String DB_PASSWORD = "test123"

    @Autowired
    TransactionMessageClient transactionMessageClient

    @Autowired
    TransactionRepository transactionRepository

    @Autowired
    ClientRepository clientRepository

    @Shared
    def static kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withEmbeddedZookeeper()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
            .withEnv("KAFKA_CREATE_TOPICS", TRANSACTION_TOPIC + ":1:1")
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

    @DirtiesContext
    def 'Should successfully store transaction message from topic to postgres db with dummy client cause client does not exist into db'() {
        given:
            def transaction = [
                    bank     : "revolut", clientId: 1,
                    orderType: TransactionType.INCOME,
                    quantity : 2, price: 2000.0,
                    createdAt: "2024-06-20 10:32:25"
            ]
            def messageTransaction = JsonOutput.toJson(transaction)
        when:
            producer.send(new ProducerRecord<Long, String>(TRANSACTION_TOPIC, transaction.clientId.toLong(), messageTransaction))
            TimeUnit.SECONDS.sleep(5)
            def executor = Executors.newSingleThreadExecutor()
            def future = executor.submit({
                transactionMessageClient.processTransaction()
            })
            TimeUnit.SECONDS.sleep(10)
            future.cancel(true)
        then:
            def actualTransaction = transactionRepository.findById(transaction.clientId)
            actualTransaction
            actualTransaction.get().client.id == transaction.clientId
            actualTransaction.get().cost == transaction.price * transaction.quantity
        and:
            def actualClient = clientRepository.findById(transaction.clientId)
            actualClient
            actualClient.get().id == transaction.clientId
            actualClient.get().email == "dummy@email.com"
    }

    def 'Should successfully store transaction message from topic to postgres db with existing client into db'() {
        given:
            def client = Client.builder()
                    .id(2)
                    .email("sometest@email.com")
                    .build()
            def transaction = [
                    bank     : "revolut", clientId: 2,
                    orderType: TransactionType.INCOME,
                    quantity : 2, price: 2000.0,
                    createdAt: "2024-06-20 10:32:25"
            ]
            def messageTransaction = JsonOutput.toJson(transaction)
        when:
            clientRepository.save(client)
            producer.send(new ProducerRecord<Long, String>(TRANSACTION_TOPIC, transaction.clientId.toLong(), messageTransaction))
            TimeUnit.SECONDS.sleep(5)
            def executor = Executors.newSingleThreadExecutor()
            def future = executor.submit({
                transactionMessageClient.processTransaction()
            })
            TimeUnit.SECONDS.sleep(10)
            future.cancel(true)
        then:
            def actualTransaction = transactionRepository.findById(transaction.clientId)
            actualTransaction
            actualTransaction.get().client.id == transaction.clientId
            actualTransaction.get().cost == transaction.price * transaction.quantity
        and:
            def actualClient = clientRepository.findById(transaction.clientId)
            actualClient
            actualClient.get().id == client.id
            actualClient.get().email == client.email
    }
}
