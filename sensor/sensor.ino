#include <SFE_BMP180.h>
#include <Wire.h>

unsigned long prevTime;
int tickInterval = 1000 * 2;
unsigned long currentTicks = 0;
String reqId = "D";
int errorState = 0;
bool high = false;
bool conf = false;

int error2Temp = 21;
int error3Temp = 22;

int redPin = A7;
int yellowPin = A9;
int greenPin = A10;

int tempStatus = 1;
int strike = 0;

// BMP-180 variables
SFE_BMP180 bmp;
double pressureBaseline = 0;
double temp = 0;
double pressure = 0;
double altitude = 0;

String appName = "PlantIT";

bool nextTick(){
  unsigned long currTime = millis();
  if ((currTime - prevTime) > tickInterval){
    prevTime = currTime;
    currentTicks++;
    return true;
  }

  return false;
}

void changeStatus(int s){
  if (s == 2){
    analogWrite(greenPin, 0);
    analogWrite(yellowPin, 255);
  } else if (s == 3){
    analogWrite(greenPin, 0);
    analogWrite(yellowPin, 0);
    analogWrite(redPin,255);
  }
}

void resetSerial() {
  Serial1.end();
  Serial1.begin(9600);
}

void buildResponse(String &res) {
  res = res + "#";
  res = res + "@" + temp + "$";
}

double readBMP180Values()
{
  char status;
  status = bmp.startTemperature();

  if (status != 0)
  {
    delay(status);
    status = bmp.getTemperature(temp);


    if (status != 0)
    {
      status = bmp.startPressure(3);

      if (status != 0)
      {
        delay(status);
        status = bmp.getPressure(pressure, temp);

        if (status != 0)
        {
          altitude = bmp.altitude(pressure, pressureBaseline + 5.6);
          return pressure;
        }
        else Serial1.println("error retrieving pressure measurement\n");
      }
      else Serial1.println("error starting pressure measurement\n");
    }
    else Serial1.println("error retrieving temperature measurement\n");
  }
  else Serial1.println("error starting temperature measurement\n");
}

void readValues() {
  readBMP180Values();
}

void setupBMP180() {
  if (!bmp.begin()) {
    Serial1.println("Error");
    while (1);
  }

  pressureBaseline = readBMP180Values();
}

void setup() {
  Serial1.begin(9600);
  Serial.begin(19200);
  setupBMP180();
  pinMode(redPin, OUTPUT);
  pinMode(yellowPin, OUTPUT);
  pinMode(greenPin, OUTPUT);

  analogWrite(greenPin, 255);
  
  delay(2000);
  prevTime = millis();
  
}

void sendResponse(String &res) {
  Serial1.print(res);
}

void checkForOperation() {
  char operation = '0';

  if (Serial1.available()) {
    operation = char(Serial1.read());

    if (operation == 'd'){
      sendData();
    } else if (operation == 'n'){
      sendName();
    } else if (operation == 'c'){
      conf = true;
    } else if (conf){
      reqId = String(operation);
    }
  }
}

void sendName(){
  Serial1.print("#" + appName + "$");
}

void sendData() {
  readValues();
  String res = String("");
  buildResponse(res);
  Serial1.print(res);
}

void loop() {
  if (nextTick()){ 
      Serial.print("Current ticks: ");
      Serial.println(currentTicks);
      Serial.print("Current temp: ");
      Serial.println(temp);
      Serial.print("High: ");
      Serial.println(high);

      readValues();
      if (errorState != 3){
        
        errorState = checkRequirements();

        if (errorState == 1){
          currentTicks = 0;
        }
        
        changeStatus(errorState);
      }

      Serial.println();
  }

  checkForOperation();
}

//########################  Check values ##############################

int above(double value, double threshold){
  if (value > threshold){
    return 3;
  } else if (value > threshold*0.9){
    return 2;
  } else {
    return 1;
  }
}

int isFreeze(double value){
   if (value < 0){
      return 3;
   } else if (value < 2) {
    return 2;
   } else {
    return 1;
   }
}

int combine(int state1, int state2){
  if (state1 == 3 || state2 == 3){
    return 3;
  } else if (state1 == 2 || state2 == 2){
    return 2;
  } else {
    return 1;
  }
}

int complexCheck(double d1, double d2){
  if (!high){
    int res = above(temp, 22);
    if (res == 3){
      Serial.println("Set to high");
      high = true;
      return checkRequirements();
    }
    return combine(res, noMore(d1));
  }

  return combine(above(temp, 23), noMore(d2));
}

int noMore(int numberOfTicks){
  if (currentTicks > numberOfTicks){
    return 3;
  } else if (currentTicks > numberOfTicks * 0.9){
    return 2;
  } else {
    return 1;
  }
}

int checkRequirements(){
  Serial.println(reqId);
  
  if (reqId == "A"){
    return complexCheck(5, 3);
  } else if (reqId == "B"){
    return complexCheck(5, 3);
  } else if (reqId == "C"){
    return complexCheck(5, 3);
  } else if (reqId == "D"){
    return above(temp, 24);
  } else if (reqId == "E"){
    return isFreeze(temp);
  } else if (reqId == "F"){
    return above(temp, 25);
  } else if (reqId == "0"){
    return above(temp, 8);
  } else if (reqId == "25"){
     return above(temp, 25);
  } else if (reqId == "0F"){
    return combine(above(temp,8), isFreeze(temp));
  } else if (reqId == "AF"){  
    return combine(complexCheck(3,2), isFreeze(temp));
  } else if (reqId == "BF"){
    return combine(complexCheck(5, 3), isFreeze(temp));
  } else if (reqId == "CF"){
    return combine(complexCheck(5, 3), isFreeze(temp));
  } else {
    return 0;
  }
}
