# ArduinoLAB
Processing Library to communicate with Arduino oriented to science and robotics laboratory use.

This library uses Firmata protocol, Arduino needs to have been loaded with StandardFirmata sketch.

Also includes i2c communication and is class oriented, so you have a separate variable for each device, ex:

```c++
ArduinoLAB arduino;
ArduinoLAB.ANALOG a;

// To read analog values
a = arduino.analog(2);  // Assign A2 pin
a.on(); // Start reporting
println(a.value());

// To write to pwm pin
a = arduino.analog(5); // Assign digtal pin 5
a.write(128);
```

The available classes are:

#### For analog read and write
- ArduinoLAB.ANALOG
#### For digital read and write
- ArduinoLAB.DIGITAL
#### For servos
- ArduinoLAB.SERVO
#### For I2C devices
- ArduinoLAB.I2C
#### For I2C device registers
- ArduinoLAB.I2C.REG

### Events
As digital reporting is asynchronous, there is an event to handle it:

```c+
digitalEvent(int[] data)
```
where data is {portNumber, portValue).
