package main

import (
	"log"
	"os"
	"time"
)

func newCloudantDB() *DBConn {
	url := os.Getenv("CLOUDANT_URL")
	if url == "" {
		url = "localhost"
	}

	key := os.Getenv("CLOUDANT_APIKEY")
	if key == "" {
		key = "PUT_CLOUDANT_APIKEY_HERE"
	}

	pass := os.Getenv("CLOUDANT_PASS")
	if pass == "" {
		pass = "PUT_CLOUDANT_PASS_HERE"
	}

	db := os.Getenv("CLOUDANT_BASE")
	if db == "" {
		db = "PUT_CLOUDANT_BASE_HERE"
	}

	return &DBConn{url, key, pass, db}
}

func newMysqlDB() *mysqlDBConn {
	host := os.Getenv("MYSQL_HOST")
	if host == "" {
		host = "localhost"
	}

	port := os.Getenv("MYSQL_PORT")
	if port == "" {
		port = "3306"
	}

	login := os.Getenv("MYSQL_LOGIN")
	if login == "" {
		login = "PUT_MYSQL_LOGIN_HERE"
	}

	pass := os.Getenv("MYSQL_PASS")
	if pass == "" {
		pass = "PUT_MYSQL_PASS_HERE"
	}

	db := os.Getenv("MYSQL_BASE")
	if db == "" {
		db = "PUT_MYSQL_BASE_HERE"
	}

	var conn *mysqlDBConn
	var err error

	isConnected := false
	for !isConnected {
		conn, err = createMysqlConnection(host, port, login, pass, db)
		if err != nil {
			log.Printf("Error creating connection to MySQL: %s", err)
			time.Sleep(5 * time.Second)
		} else {
			ok := conn.check()
			if ok != nil {
				log.Printf("Error checking connection to MySQL: %s", ok)
				time.Sleep(5 * time.Second)
			} else {
				isConnected = true
			}
		}
	}

	return conn
}
