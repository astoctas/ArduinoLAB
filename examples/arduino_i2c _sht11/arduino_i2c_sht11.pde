/*
arduino_i2c_sht11  USING SHT11 Library

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

// Import Libraries
import cc.arduinoLab.*;
import lab.sht11.*;

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
  sht11 = arduino.i2c(SHT11.address);

// Assign SHT11 temperature register to reg
  temp = sht11.register(SHT11.temperature.register);
// Assign SHT11 humidity register to reg
  hum = sht11.register(SHT11.humidity.register);
  
// Start reporting (3 bytes per register)
  temp.on(SHT11.temperature.bytes);
  hum.on(SHT11.humidity.bytes);
}

void draw() {
  delay(500); // Rest to wait first reporting
  // Get values
  print("Temperature: "); println(SHT11.temperature.value(temp.value()));
  print("Humidity: "); println(SHT11.humidity.value(hum.value()));
  
  
}
