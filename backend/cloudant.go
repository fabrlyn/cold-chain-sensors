package main

import (
	//"bytes"
	"crypto/sha1"
	"encoding/hex"
	//"encoding/json"
	"io"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"time"
)

// SensorData describe schema of data in for uploading to DB
type SensorData struct {
	Timestamp string
	ID        string
	ValueAvg  float64
	ValueMin  float64
	ValueMax  float64
}

//DBConn describe parameters for connecting to Cloudant DB
type DBConn struct {
	URL      string
	User     string
	Password string
	DBName   string
}

func hash() string {
	t := strconv.FormatInt(time.Now().UnixNano(), 10)
	data := []byte(t)
	hash := sha1.New()
	hash.Write(data)
	str := hex.EncodeToString(hash.Sum(nil))
	return str
}

// Put used for uploading data to DB
func (dbconn *DBConn) Put(jsonData io.Reader) error {

	id := hash()
	//log.Printf("%s\n", id)
	url := dbconn.URL + "/" + dbconn.DBName + "/" + id

	client := &http.Client{}

	req, err := http.NewRequest("PUT", url, jsonData)
	if err != nil {
		log.Printf("Error creating new request: %s", err)
		return err
	}
	req.SetBasicAuth(dbconn.User, dbconn.Password)
	resp, err := client.Do(req)
	if err != nil {
		log.Printf("Error getting response from DB: %s", err)
		return err
	}

	//contents, err := ioutil.ReadAll(resp.Body)
	_, err = ioutil.ReadAll(resp.Body)
	if err != nil {
		log.Printf("Error reading response from DB: %s", err)
		return err
	}
	//log.Printf("%s\n", string(contents))

	return nil

}
