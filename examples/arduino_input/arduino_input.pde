/* //<>//
arduino_input

Demonstrates the reading of digital and analog pins of an Arduino board
running the StandardFirmata firmware.

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
* Run this sketch. The squares show the values of the digital inputs (HIGH
  pins are filled, LOW pins are not). The circles show the values of the
  analog inputs (the bigger the circle, the higher the reading on the
  corresponding analog input pin). The pins are laid out as if the Arduino
  were held with the logo upright (i.e. pin 13 is at the upper left). Note
  that the readings from unconnected pins will fluctuate randomly. 
  
For more information, see: http://playground.arduino.cc/Interfacing/Processing
*/

import processing.serial.*;

// Import Library
import cc.arduinoLab.*;

// Arduino handle
ArduinoLAB arduino;

// analog and digital handles
ArduinoLAB.ANALOG a;
ArduinoLAB.DIGITAL d;

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

  // Assign pins to handles
  a = arduino.analog(2);
  d = arduino.digital(13);

  // Set pullup
  d.pullup(true);
  // Start reporting
  a.on();
  d.on();
  
}

void draw() {
  // Print analog and digital values in console
  println(a.value());
  println(d.value());
  delay(500);
}


void digitalEvent(int[] data) {
  // Event to execute if port value changes. data is {portNumber, portValue}
  print("port: "); println(data[0]);
  print("value: "); println(data[1]);
}
