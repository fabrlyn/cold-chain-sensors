package main

import (
	"bytes"
	"encoding/json"
	"log"
	"os"

	"github.com/gin-gonic/gin"
)

var dbconn *DBConn
var mysqlConn *mysqlDBConn

// ValueJSON describe format of incoming values
type ValueJSON struct {
	ID        string  `json:"device_id" binding:"required"`
	Timestamp int     `json:"date" binding:"required"`
	ValueAvg  float64 `json:"value_avg" binding:"required"`
	ValueMin  float64 `json:"value_min" binding:"required"`
	ValueMax  float64 `json:"value_max" binding:"required"`
}

func postValues(c *gin.Context) {
	var jsonV ValueJSON
	if c.BindJSON(&jsonV) == nil {

		jsonData, err := json.Marshal(jsonV)
		if err != nil {
			log.Println("Error during marshaling")
			c.JSON(500, gin.H{"message": "error during marshaling"})
		}

		err = dbconn.Put(bytes.NewReader(jsonData))
		if err != nil {
			log.Println("Error pushing to cloudant")
			c.JSON(500, gin.H{"message": "error pushing to cloudant"})
		}

		c.JSON(200, gin.H{"message": "ok"})
	} else {
		c.JSON(500, gin.H{"message": "error decoding JSON"})
	}
}

func main() {

	dbconn = newCloudantDB()
	mysqlConn = newMysqlDB()

	router := gin.Default()

	listRouter := router.Group("/list")
	{
		listRouter.GET("/transport", getAllTransport)
		//listRouter.GET("/package", getAllPackages)
		//listRouter.GET("/medicine", getAllMedicine)
	}

	router.GET("/transport/:id", getAllPackages)
	router.GET("/package/:id", getAllMedicine)

	router.POST("/sensor", postValues)

	router.GET("/check", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"message": "ok",
		})
	})

	port := os.Getenv("VCAP_APP_PORT")
	if port == "" {
		port = "8080"
	}
	router.Run(":" + port)
}
