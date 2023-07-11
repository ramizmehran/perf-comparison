package main

import (
	"context"
	"database/sql"
	"log"
	"strconv"
	"encoding/json"
	// "sync"

	"github.com/gin-gonic/gin"
	"github.com/go-redis/redis/v8"
	_ "github.com/go-sql-driver/mysql"
)

type Data struct {
	ID    int
	Name  string
	Email string
}

func main() {
	// MySQL connection
	db, err := sql.Open("mysql", "root:mysecretpassword@tcp(172.17.0.2:3306)/perf_db")
	if err != nil {
		log.Fatal("Failed to connect to MySQL:", err)
	}
	defer db.Close()

	// Redis connection
	rdb := redis.NewClient(&redis.Options{
		Addr:     "172.17.0.3:6379",
		Password: "", // If your Redis container has a password, set it here
		DB:       0,  // Use the default Redis database
	})

	// Create a Gin router
	router := gin.Default()

	// Route for fetching data from MySQL
	router.GET("/db/:id", func(c *gin.Context) {
		// Get the ID from the path parameter
		idStr := c.Param("id")
		id, err := strconv.Atoi(idStr)
		if err != nil {
			c.String(400, "Invalid ID")
			return
		}

		// Create a channel to receive the data
		dataCh := make(chan Data, 1)

		// Fetch data from MySQL in a separate goroutine
		go func() {
			var data Data
			err := db.QueryRowContext(context.Background(), "SELECT id, name, email FROM data WHERE id = ?", id).Scan(&data.ID, &data.Name, &data.Email)
			if err != nil {
				log.Println("Failed to fetch data from MySQL:", err)
				dataCh <- Data{} // Send empty data if there's an error
				return
			}

			dataCh <- data // Send the fetched data
		}()

		// Wait for the data to be received
		data := <-dataCh

		// Return the data as JSON
		c.JSON(200, data)
	})

	// Route for fetching data from Redis and updating if missing from MySQL
	router.GET("/cache/:id", func(c *gin.Context) {
		// Get the ID from the path parameter
		idStr := c.Param("id")
		id, err := strconv.Atoi(idStr)
		if err != nil {
			c.String(400, "Invalid ID")
			return
		}

		// Fetch data from Redis
		dataStr, err := rdb.Get(context.Background(), strconv.Itoa(id)).Result()
		if err == redis.Nil {
			// Data is missing from Redis, fetch from MySQL
			var data Data
			err := db.QueryRowContext(context.Background(), "SELECT id, name, email FROM data WHERE id = ?", id).Scan(&data.ID, &data.Name, &data.Email)
			if err != nil {
				log.Println("Failed to fetch data from MySQL:", err)
				c.String(500, "Failed to fetch data from MySQL")
				return
			}

			// Update Redis with the fetched data
			jsonData, err := json.Marshal(data)
			if err != nil {
				log.Println("Failed to marshal data to JSON:", err)
				c.String(500, "Failed to marshal data to JSON")
				return
			}
			err = rdb.Set(context.Background(), strconv.Itoa(data.ID), jsonData, 0).Err()
			if err != nil {
				log.Println("Failed to update data in Redis:", err)
			}

			// Return the data as JSON
			c.JSON(200, data)
			return
		} else if err != nil {
			log.Println("Failed to fetch data from Redis:", err)
			c.String(500, "Failed to fetch data from Redis")
			return
		}

		// Unmarshal the data from Redis
		var data Data
		err = json.Unmarshal([]byte(dataStr), &data)
		if err != nil {
			log.Println("Failed to unmarshal data from JSON:", err)
			c.String(500, "Failed to unmarshal data from JSON")
			return
		}

		// Return the data from Redis as JSON
		c.JSON(200, data)
	})


	// Start the HTTP server
	router.Run(":8080")
}
