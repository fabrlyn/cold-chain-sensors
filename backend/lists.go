package main

import (
	"fmt"
	"log"

	"github.com/gin-gonic/gin"
	"github.com/go-sql-driver/mysql"
)

type responceJSONTranport struct {
	ID            string `json:"id"`
	Name          string `json:"name"`
	StartDate     string `json:"start_date"`
	EndDate       string `json:"end_date"`
	StartLocation string `json:"location_start"`
	EndLocation   string `json:"location_end"`
}

type responceJSONTranportAll struct {
	Message string                  `json:"message"`
	Result  []*responceJSONTranport `json:"result"`
}

type responceJSONPackage struct {
	ID       string `json:"id"`
	DeviceID string `json:"device_id"`
}

type responceJSONPackageAll struct {
	Message string                 `json:"message"`
	Result  []*responceJSONPackage `json:"result"`
}

type responceJSONMedicine struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Amount      string `json:"amount"`
	Requirement string `json:"requirement"`
	StartDate   string `json:"start_date"`
	Alarm       int    `json:"alarm"`
}

type responceJSONMedicineAll struct {
	Message string                  `json:"message"`
	Result  []*responceJSONMedicine `json:"result"`
}

func getAllTransport(c *gin.Context) {
	c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
	resp := &responceJSONTranportAll{}

	rows, err := mysqlConn.db.Query("SELECT * FROM Transport")
	if err != nil {
		log.Printf("Error quering the DB: %s", err)
		c.JSON(500, gin.H{"message": "error quering the DB"})
		return
	}

	for rows.Next() {
		var uid string
		var start mysql.NullTime
		var end mysql.NullTime
		var sLoc string
		var eLoc string
		var name string

		err := rows.Scan(&uid, &start, &end, &sLoc, &eLoc, &name)
		if err != nil {
			log.Printf("Error fetching row: %s", err)
			c.JSON(500, gin.H{"message": "error fetching row"})
			return
		}

		var startT, endT string
		if start.Valid {
			//startT = string(start.Time.Unix())
			startT = start.Time.String()
		} else {
			startT = ""
		}
		if end.Valid {
			//endT = string(end.Time.Unix())
			endT = end.Time.String()
		} else {
			endT = ""
		}
		resp.Result = append(resp.Result, &responceJSONTranport{uid, name, startT, endT, sLoc, eLoc})
	}

	resp.Message = "ok"
	c.JSON(200, resp)
}

func getAllPackages(c *gin.Context) {
	c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
	id := c.Param("id")
	resp := &responceJSONPackageAll{}

	rows, err := mysqlConn.db.Query(fmt.Sprintf("SELECT * FROM Package WHERE transportId = '%s'", id))
	if err != nil {
		log.Printf("Error quering the DB: %s", err)
		c.JSON(500, gin.H{"message": "error quering the DB"})
		return
	}
	for rows.Next() {
		var uid string
		var pID string
		var devID string

		err := rows.Scan(&uid, &pID, &devID)
		if err != nil {
			log.Printf("Error fetching row: %s", err)
			c.JSON(500, gin.H{"message": "error fetching row"})
			return
		}

		resp.Result = append(resp.Result, &responceJSONPackage{uid, devID})
	}

	resp.Message = "ok"

	c.JSON(200, resp)
}

func getAllMedicine(c *gin.Context) {
	c.Writer.Header().Set("Access-Control-Allow-Origin", "*")
	id := c.Param("id")

	resp := &responceJSONMedicineAll{}

	rows, err := mysqlConn.db.Query(fmt.Sprintf("SELECT * FROM Medicine WHERE packageId = '%s'", id))
	if err != nil {
		log.Printf("Error quering the DB: %s", err)
		c.JSON(500, gin.H{"message": "error quering the DB"})
		return
	}

	for rows.Next() {
		var uid string
		var name string
		var amount string
		var req string
		var startDate mysql.NullTime
		var high bool
		var pkgID string
		var alarm int

		err := rows.Scan(&uid, &name, &amount, &req, &startDate, &high, &pkgID, &alarm)
		if err != nil {
			log.Printf("Error fetching row: %s", err)
			c.JSON(500, gin.H{"message": "error fetching row"})
			return
		}

		var start string
		if startDate.Valid {
			//startT = string(start.Time.Unix())
			start = startDate.Time.String()
		} else {
			start = ""
		}

		resp.Result = append(resp.Result, &responceJSONMedicine{uid, name, amount, req, start, alarm})
	}

	resp.Message = "ok"

	c.JSON(200, resp)

}
