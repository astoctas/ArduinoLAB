# ArduinoLAB
Processing Library to communicate with Arduino oriented to science and robotics laboratory use.

This library uses Firmata protocol, includes i2c communication and is class oriented, so you have a separate variable for each device, ex:

ArduinoLAB arduino;

ArduinoLAB.ANALOG a;

// To read analog values

a = arduino.analog(2);  // Assign A2 pin

a.on(); // Start reporting

println(a.value());


// To write to pwm pin

a = arduino.analog(5); // Assign digtal pin 5

a.write(128);

