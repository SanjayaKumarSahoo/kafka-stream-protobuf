package com.kafka.stream.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

@Configuration
@Profile("word-count")
public class WordCountStreamConfig {

    @Bean(destroyMethod = "close")
    @Qualifier("wordCountStream")
    public KafkaStreams wordCountStream(Environment envProps) {
        Topology topology = buildTopology(envProps);
        final KafkaStreams streams = new KafkaStreams(topology, properties(envProps));
        streams.start();
        return streams;
    }

    private Properties properties(Environment envProps) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, envProps.getProperty("kafka.bootstrap-servers"));
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, envProps.getProperty("kafka.application-id"));
        properties.put(StreamsConfig.STATE_DIR_CONFIG, envProps.getProperty("kafka.word.count.state.dir"));
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, envProps.getProperty("kafka.client.id"));
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, envProps.getProperty("kafka.group.id"));
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return properties;
    }

    private Topology buildTopology(Environment envProps) {
        final StreamsBuilder builder = new StreamsBuilder();
        final String inputTopic = envProps.getProperty("kafka.text.count.input.topic");
        final String outputTopic = envProps.getProperty("kafka.word.count.output.topic");

        final KStream<String, String> textLines = builder.stream(inputTopic);
        final Pattern pattern = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS);

        final KTable<String, Long> wordCounts = textLines
                .flatMapValues(value -> Arrays.asList(pattern.split(value.toLowerCase())))
                .groupBy((keyIgnored, word) -> word)
                .count();

        wordCounts.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));
        return builder.build();
    }
}