/*
arduino_i2c

Demonstrates the control of servo motors connected to an Arduino board
running the StandardFirmata firmware.  Moving the mouse horizontally across
the sketch changes the angle of servo motors on digital pins 4 and 7.  For
more information on servo motors, see the reference for the Arduino Servo
library: http://arduino.cc/en/Reference/Servo

To use:
* Using the Arduino software, upload the StandardFirmata example (located
  in Examples > Firmata > StandardFirmata) to your Arduino board.
* Run this sketch and look at the list of serial ports printed in the
  message area below. Note the index of the port corresponding to your
  Arduino board (the numbering starts at 0).  (Unless your Arduino board
  happens to be at index 0 in the list, the sketch probably won't work.
  Stop it and proceed with the instructions.)
* Modify the "arduino = new Arduino(...)" line below, changing the number
  in Arduino.list()[0] to the number corresponding to the serial port of
  your Arduino board.  Alternatively, you can replace Arduino.list()[0]
  with the name of the serial port, in double quotes, e.g. "COM5" on Windows
  or "/dev/tty.usbmodem621" on Mac.
* Connect Servos to digital pins 4 and 7.  (The servo also needs to be
  connected to power and ground.)
* Run this sketch and move your mouse horizontally across the screen.
  
For more information, see: http://playground.arduino.cc/Interfacing/Processing
*/

import processing.serial.*;

// Import Library
import cc.arduinoLab.*;

// Arduino handle
ArduinoLAB arduino;

// I2C handles
ArduinoLAB.I2C sht11;
ArduinoLAB.I2C.REG temp;
ArduinoLAB.I2C.REG hum;

void setup() {
  surface.setVisible(false);
  
  // Prints out the available serial ports.
  println(ArduinoLAB.list());
  
  // Modify this line, by changing the "0" to the index of the serial
  // port corresponding to your Arduino board (as it appears in the list
  // printed by the line above).
  //  arduino = new Interfaz(this, Interfaz.list()[0], 57600);
  // Alternatively, use the name of the serial port corresponding to your
  // Arduino (in double-quotes), as in the following line.
  arduino = new ArduinoLAB(this, "COM6");

// Assign SHT11 address to dev
  sht11 = arduino.i2c(0x40);

// Assign SHT11 temperature register to reg
  temp = sht11.register(0xE3);
// Assign SHT11 humidity register to reg
  hum = sht11.register(0xE5); 
  
// Start reporting (3 bytes per register)
  temp.on(3);
  hum.on(3);
}

void draw() {
  delay(500); // Rest to wait first reporting
  // Get values
  int[] temp_reg = temp.value();
  int[] hum_reg = hum.value();
  
  // Process values
  float temp =  temp_reg[0] << 8 | temp_reg[1];
  temp *= 175.72;
  temp /= 65536;
  temp -= 46.85;
  print("Temperature: "); println(temp);

  float hum =  hum_reg[0] << 8 | hum_reg[1];
  hum *= 125;
  hum /= 65536;
  hum -=6;
  print("Humidity: "); println(hum);
  
  
}
