package main

import (
	"database/sql"
	"log"

	"github.com/gin-gonic/gin"
	"github.com/go-redis/redis/v8"
	_ "github.com/go-sql-driver/mysql"
)

var (
	db  *sql.DB
	rdb *redis.Client
)

func main() {
	var err error

	// MySQL connection
	db, err = sql.Open("mysql", "root:mysecretpassword@tcp(172.17.0.2:3306)/perf_db")
	if err != nil {
		log.Fatal("Failed to connect to MySQL:", err)
	}
	defer db.Close()

	// Redis connection
	rdb = redis.NewClient(&redis.Options{
		Addr:     "172.17.0.3:6379",
		Password: "", // If your Redis container has a password, set it here
		DB:       0,  // Use the default Redis database
	})

	// Create a Gin router
	router := gin.Default()

	// Database routes
	router.GET("/db/:id", getDBData)
	router.GET("/cache/:id", getCachedData)

	// Kafka routes
	router.POST("/produce/:message", produceToKafka)
	router.GET("/consume", consumeFromKafka)

	// Start the HTTP server
	router.Run(":8080")
}
