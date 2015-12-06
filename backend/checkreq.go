package main

import (
	"log"
	"time"
)

func above(value, threshold float64) int {
	if value > threshold {
		return 3
	} else if value > threshold*0.9 {
		return 2
	} else {
		return 1
	}
}

func isFreeze(value float64) int {
	if value < 0 {
		return 3
	} else if value < 2 {
		return 2
	} else {
		return 1
	}
}

func combine(state1, state2 int) int {
	if state1 == 3 || state2 == 3 {
		return 3
	} else if state1 == 2 || state2 == 2 {
		return 2
	} else {
		return 1
	}
}

func noMore(timestamp time.Time, numberDays float64) int {
	timeDiff := time.Since(timestamp)
	minutesLimit := numberDays * 24 * 60
	if timeDiff.Minutes() > minutesLimit {
		return 3
	} else if timeDiff.Minutes() > minutesLimit*0.9 {
		return 2
	} else {
		return 1
	}
}

func setHigh(id string) {
	//TODO: write function for updating value in MySQL
	return
}

func checkRequrement(id string, value float64, high bool, timestamp time.Time) int {
	complexCheck := func(d1, d2 float64) int {
		if !high {
			res := above(value, 12)
			if res == 3 {
				setHigh(id)
				return checkRequrement(id, value, true, timestamp)
			}
			return combine(res, noMore(timestamp, d1))

		}
		return combine(above(value, 21), noMore(timestamp, d2))
	}

	switch id {
	case "A":
		log.Println("A")
		return complexCheck(3, 2)
	case "B":
		log.Println("B")
		return complexCheck(8, 6)
	case "C":
		log.Println("C")
		return complexCheck(14, 11)
	case "D":
		log.Println("D")
		return above(value, 34)
	case "F":
		log.Println("F")
		return isFreeze(value)
	case "0":
		log.Println("0")
		return above(value, 8)
	case "25":
		log.Println("25")
		return above(value, 25)
	case "0F":
		log.Println("0F")
		return combine(above(value, 8), isFreeze(value))
	case "AF":
		log.Println("AF")
		return combine(complexCheck(3, 2), isFreeze(value))
	case "BF":
		log.Println("BF")
		return combine(complexCheck(8, 6), isFreeze(value))
	case "CF":
		log.Println("CF")
		return combine(complexCheck(14, 11), isFreeze(value))
	default:
		log.Println("Wrong requrement ID")
		return 0
	}
}
