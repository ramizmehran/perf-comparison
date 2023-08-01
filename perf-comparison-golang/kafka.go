package main

import (
	"log"
	"sync"

	"github.com/IBM/sarama"
	"github.com/gin-gonic/gin"
)

var (
	consumerRunning bool
	consumerLock    sync.Mutex
)

// Produce a message to Kafka
func produceToKafka(c *gin.Context) {
	message := c.Param("message")

	producer, err := sarama.NewSyncProducer([]string{"localhost:9092"}, nil)
	if err != nil {
		log.Println("Failed to create Kafka producer:", err)
		c.String(500, "Failed to create Kafka producer")
		return
	}
	defer producer.Close()

	_, _, err = producer.SendMessage(&sarama.ProducerMessage{
		Topic: "test-topic",
		Value: sarama.StringEncoder(message),
	})
	if err != nil {
		log.Println("Failed to send message to Kafka:", err)
		c.String(500, "Failed to send message to Kafka")
		return
	}

	c.String(200, "Message sent to Kafka successfully")
}

// Start or stop a Kafka consumer
func consumeFromKafka(c *gin.Context) {
	consumerLock.Lock()
	defer consumerLock.Unlock()

	if consumerRunning {
		consumerRunning = false
		c.String(200, "Kafka consumer stopped")
	} else {
		go startKafkaConsumer()
		consumerRunning = true
		c.String(200, "Kafka consumer started")
	}
}

// Start a Kafka consumer
func startKafkaConsumer() {
	consumer, err := sarama.NewConsumer([]string{"localhost:9092"}, nil)
	if err != nil {
		log.Println("Failed to create Kafka consumer:", err)
		return
	}
	defer consumer.Close()

	partitionConsumer, err := consumer.ConsumePartition("test-topic", 0, sarama.OffsetNewest)
	if err != nil {
		log.Println("Failed to create Kafka partition consumer:", err)
		return
	}
	defer partitionConsumer.Close()

	for consumerRunning {
		select {
		case msg := <-partitionConsumer.Messages():
			log.Println("Received message from Kafka:", string(msg.Value))
		case err := <-partitionConsumer.Errors():
			log.Println("Error from Kafka consumer:", err)
		}
	}
}
