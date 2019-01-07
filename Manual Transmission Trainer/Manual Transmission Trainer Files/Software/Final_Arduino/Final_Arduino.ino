
#include "quaternionFilters.h"
#include "MPU9250.h"

#include <SoftwareSerial.h>

#define BAUD 9600
SoftwareSerial mySerial(2,3); // RX, TX

//Variables to hold the speed and RPM data.
int vehicleSpeed=0;
int vehicleRPM1=0;
int vehicleRPM=0;
int vehicleRPM2=0;
int vehicleThrottle=0;
int vehicleLoad1=0;
int vehicleLoad2=0;

//This is a character buffer that will store the data from the serial port
char rxData[20];
char rxIndex=0;

#define AHRS false         // Set to false for basic data read
#define SerialDebug false// Set to true to get mySerial output for debugging
#define SerialCSV true
#define UARTDebug false

MPU9250 myIMU;

float const alpha = .5;

double fAx = 0;
double fAy = 0;
double fAz = 0;
double ptch;



void setup()
{
  Wire.begin();
  // TWBR = 12;  // 400 kbit/sec I2C speed
  mySerial.begin(9600);
  mySerial.println("Starting");
  Serial.begin(9600); 
  // Get sensor resolutions, only need to do this once
  myIMU.getAres();
  myIMU.getGres();
  myIMU.getMres();

  // The next call delays for 4 seconds, and then records about 15 seconds of
  // data to calculate bias and scale.
  myIMU.magCalMPU9250(myIMU.magBias, myIMU.magScale);
  
  delay(1500);
  //Reset the OBD-II-UART
  Serial.println("ATZ");
  //Wait for a bit before starting to send commands after the reset.
  getResponse();
  getResponse();
  getResponse();
  getResponse();
  getResponse();

  getResponse();
  getResponse();
  getResponse();
  delay(2000);

  //Delete any data that may be in the serial port before we begin.
  Serial.flush();
  mySerial.println("end setup");
}

void loop()
{
  if (myIMU.readByte(MPU9250_ADDRESS, INT_STATUS) & 0x01)
  {
    myIMU.readAccelData(myIMU.accelCount);  // Read the x/y/z adc values

    // Now we'll calculate the accleration value into actual g's
    // This depends on scale being set
    myIMU.ax = (float)myIMU.accelCount[0] * myIMU.aRes; // - myIMU.accelBias[0];
    myIMU.ay = (float)myIMU.accelCount[1] * myIMU.aRes; // - myIMU.accelBias[1];
    myIMU.az = (float)myIMU.accelCount[2] * myIMU.aRes; // - myIMU.accelBias[2];

//    mySerial.println(myIMU.ax);
//    mySerial.println(myIMU.ay);
//    mySerial.println(myIMU.az);
    myIMU.readGyroData(myIMU.gyroCount);  // Read the x/y/z adc values

    // Calculate the gyro value into actual degrees per second
    // This depends on scale being set
    myIMU.gx = (float)myIMU.gyroCount[0] * myIMU.gRes;
    myIMU.gy = (float)myIMU.gyroCount[1] * myIMU.gRes;
    myIMU.gz = (float)myIMU.gyroCount[2] * myIMU.gRes;

//    myIMU.readMagData(myIMU.magCount);  // Read the x/y/z adc values
//
//    // Calculate the magnetometer values in milliGauss
//    // Include factory calibration per data sheet and user environmental
//    // corrections
//    // Get actual magnetometer value, this depends on scale being set
//    myIMU.mx = (float)myIMU.magCount[0] * myIMU.mRes
//               * myIMU.factoryMagCalibration[0] - myIMU.magBias[0];
//    myIMU.my = (float)myIMU.magCount[1] * myIMU.mRes
//               * myIMU.factoryMagCalibration[1] - myIMU.magBias[1];
//    myIMU.mz = (float)myIMU.magCount[2] * myIMU.mRes
//               * myIMU.factoryMagCalibration[2] - myIMU.magBias[2];
  } // if (readByte(MPU9250_ADDRESS, INT_STATUS) & 0x01)

  myIMU.updateTime();


  fAx = myIMU.ax * alpha + (1-alpha) * fAx;
  fAy = myIMU.ay * alpha + (1-alpha) * fAy;
  fAz = myIMU.az * alpha + (1-alpha) * fAz;
  
  ptch = (atan2(fAy, sqrt(fAx*fAx + fAz*fAz))*((180.0)/M_PI));
  
  Serial.flush();
  //Query the OBD-II-UART for the Vehicle Speed
  Serial.println("010D");  
  getResponse();
  getResponse();
  vehicleSpeed = strtol(&rxData[6],0,16);
  getResponse();
  
  delay(80);
  //Delete any data that may be left over in the serial port.
  Serial.flush();
  //Query the OBD-II-UART for the Vehicle RPM
  Serial.println("010C");
  //Get the response from the OBD-II-UART board
  getResponse();
  getResponse();
 
  vehicleRPM1 = strtol(&rxData[6],0,16);
  vehicleRPM2 = strtol(&rxData[9],0,16);
  getResponse();
  delay(80);

  

  Serial.flush();
  //Query the OBD-II-UART for the Vehicle Throttle
  Serial.println("0111");  
  getResponse();
  getResponse();
  vehicleThrottle = strtol(&rxData[6],0,16);
  getResponse();
  delay(80);

  Serial.flush();
  //Query the OBD-II-UART for the Vehicle Load
  Serial.println("0143");  
  getResponse();
  getResponse();
  vehicleLoad1 = strtol(&rxData[6],0,16);
  vehicleLoad2 = strtol(&rxData[9],0,16);
  getResponse();


   
   mySerial.print(ptch);
   mySerial.print(",");
   mySerial.print(vehicleSpeed);
   mySerial.print(",");
   mySerial.print(vehicleRPM1);
   mySerial.print(",");
   mySerial.print(vehicleRPM2);
   mySerial.print(",");
   mySerial.print(vehicleThrottle);
   mySerial.print(",");
   mySerial.print(vehicleLoad1);
   mySerial.print(",");
   mySerial.print(vehicleLoad2);
   mySerial.print(">\n");
   
   delay(80);

  fAx=0;
  fAy=0;
  fAz=0;
  ptch=0;
}


void getResponse(void){
  char inChar=0;
  //Keep reading characters until we get a carriage return
  while(inChar != '\r'){
    //If a character comes in on the serial port, we need to act on it.
    if(Serial.available() > 0){
      //Start by checking if we've received the end of message character ('\r').
      if(Serial.peek() == '\r'){
        //Clear the Serial buffer
        inChar=Serial.read();
        //Put the end of string character on our data string
        rxData[rxIndex]='\0';
        //Reset the buffer index so that the next character goes back at the beginning of the string.
        rxIndex=0;
      }
      //If we didn't get the end of message character, just add the new character to the string.
      else{
        //Get the new character from the Serial port.
        inChar = Serial.read();
        if (UARTDebug){
          mySerial.print(inChar);
        }
        //Add the new character to the string, and increment the index variable.
        rxData[rxIndex++]=inChar;
      }
    }
  }
  if (UARTDebug){
    mySerial.println("");
  }
}
