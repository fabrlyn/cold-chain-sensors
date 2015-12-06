package main

import "database/sql"
import _ "github.com/go-sql-driver/mysql"

type mysqlDBConn struct {
	db *sql.DB
}

func createMysqlConnection(host, port, login, pass, db string) (*mysqlDBConn, error) {
	dsn := login + ":" + pass + "@tcp(" + host + ":" + port + ")/" + db
	conn, err := sql.Open("mysql", dsn)
	if err != nil {
		return &mysqlDBConn{}, err
	}
	return &mysqlDBConn{conn}, nil
}

func (c *mysqlDBConn) check() error {
	return c.db.Ping()
}
