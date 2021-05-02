package com.kafka.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

@SpringBootTest
@ActiveProfiles("word-count")
public class WordCountStreamITests {

    @Test
    public void testWordCount() {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        KafkaProducer<String, String> textKafkaProducer = new KafkaProducer<>(props);
        textKafkaProducer.send(new ProducerRecord<>("streams-plaintext-input", "hello kafka streams"));
        textKafkaProducer.send(new ProducerRecord<>("streams-plaintext-input", "all streams lead to join kafka summit"));

        // consume rated movie data and print
        Properties consumerProperties = new Properties();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "client-group-id");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);

        KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(consumerProperties);
        consumer.subscribe(Collections.singletonList("streams-wordcount-output"));
        ConsumerRecords<String, Long> consumerRecords = consumer.poll(Duration.ofSeconds(15));
        consumerRecords.records("streams-wordcount-output").forEach(record -> {
            System.out.println(record.key() + " : " + record.value());
        });
    }
}
