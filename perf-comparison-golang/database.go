package main

import (
	"context"
	"encoding/json"
	"log"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/go-redis/redis/v8"
)

type Data struct {
	ID    int
	Name  string
	Email string
}

// Fetch data from MySQL
func getDBData(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.String(400, "Invalid ID")
		return
	}

	dataCh := make(chan Data, 1)

	go func() {
		var data Data
		err := db.QueryRowContext(context.Background(), "SELECT id, name, email FROM data WHERE id = ?", id).Scan(&data.ID, &data.Name, &data.Email)
		if err != nil {
			log.Println("Failed to fetch data from MySQL:", err)
			dataCh <- Data{}
			return
		}

		dataCh <- data
	}()

	data := <-dataCh

	c.JSON(200, data)
}

// Fetch data from Redis and update if missing from MySQL
func getCachedData(c *gin.Context) {
	id, err := strconv.Atoi(c.Param("id"))
	if err != nil {
		c.String(400, "Invalid ID")
		return
	}

	dataStr, err := rdb.Get(context.Background(), strconv.Itoa(id)).Result()
	if err == redis.Nil {
		var data Data
		err := db.QueryRowContext(context.Background(), "SELECT id, name, email FROM data WHERE id = ?", id).Scan(&data.ID, &data.Name, &data.Email)
		if err != nil {
			log.Println("Failed to fetch data from MySQL:", err)
			c.String(500, "Failed to fetch data from MySQL")
			return
		}

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

		c.JSON(200, data)
		return
	} else if err != nil {
		log.Println("Failed to fetch data from Redis:", err)
		c.String(500, "Failed to fetch data from Redis")
		return
	}

	var data Data
	err = json.Unmarshal([]byte(dataStr), &data)
	if err != nil {
		log.Println("Failed to unmarshal data from JSON:", err)
		c.String(500, "Failed to unmarshal data from JSON")
		return
	}

	c.JSON(200, data)
}
