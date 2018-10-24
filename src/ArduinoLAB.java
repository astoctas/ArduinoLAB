/**
 * ArduinoLAB.java - Arduino/firmata library for Processing
 * Copyright (C) 2006-08 David A. Mellis
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * Processing code to communicate with the Arduino Firmata 2 firmware.
 * http://firmata.org/
 *
 * $Id$
 */

package cc.arduinoLab;

import processing.core.PApplet;
import processing.serial.Serial;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Observer;
import java.util.Observable;

import org.firmata.Firmata;
//import cc.digitalobserver.*;

class DigitalObserver implements Observer {
  ArduinoLAB dg;

  public void setInstance(ArduinoLAB  _dg) {
    dg = _dg;
  }
  public void update(Observable obs, Object obj) {
    dg.digitalEvent(obj);
  }    
}

/**
 * Together with the Firmata 2 firmware (an Arduino sketch uploaded to the
 * Arduino board), this class allows you to control the Arduino board from
 * Processing: reading from and writing to the digital pins and reading the
 * analog inputs.
 */
public class ArduinoLAB {
  /**
   * Constant to set a pin to input mode (in a call to pinMode()).
   */
  public static final int INPUT = 0;
  /**
   * Constant to set a pin to output mode (in a call to pinMode()).
   */
  public static final int OUTPUT = 1;
  /**
   * Constant to set a pin to analog mode (in a call to pinMode()).
   */
  public static final int PINMODE_ANALOG = 2;
  /**
   * Constant to set a pin to PWM mode (in a call to pinMode()).
   */
  public static final int PINMODE_PWM = 3;
  /**
   * Constant to set a pin to servo mode (in a call to pinMode()).
   */
  public static final int PINMODE_SERVO = 4;
  /**
   * Constant to set a pin to shiftIn/shiftOut mode (in a call to pinMode()).
   */
  public static final int SHIFT = 5;
  /**
   * Constant to set a pin to I2C mode (in a call to pinMode()).
   */
  public static final int I2C = 6;
  /**
   * Constant to set a pin to input mode and enable the pull-up resistor (in a call to pinMode()).
   */
  public static final int INPUT_PULLUP = 11;

  /**
   * Constant to write a high value (+5 volts) to a pin (in a call to
   * digitalWrite()).
   */
  public static final int LOW = 0;
  /**
   * Constant to write a low value (0 volts) to a pin (in a call to
   * digitalWrite()).
   */
  public static final int HIGH = 1;

  private static final int  FIRMATA_I2C_REQUEST	 = 0x76;
  private static final int  FIRMATA_I2C_REPLY	 = 0x77;
  private static final int  FIRMATA_I2C_CONFIG	 = 0x78;
  private static final int  FIRMATA_I2C_AUTO_RESTART	 = 0x40;
  private static final int  FIRMATA_I2C_10_BIT		 = 0x20;
  private static final int  FIRMATA_I2C_WRITE		 = 0x00;
  private static final int  FIRMATA_I2C_READ_ONCE		 = 0x08;
  private static final int  FIRMATA_I2C_READ_CONTINUOUS	 = 0x10;
  private static final int FIRMATA_I2C_STOP_READING = 0x18;

  Method digitalEventMethod;
  DigitalObserver digitalObserver = new DigitalObserver();

  
  PApplet parent;
  Serial serial;
  SerialProxy serialProxy;
  Firmata firmata;

  // We need a class descended from PApplet so that we can override the
  // serialEvent() method to capture serial data.  We can't use the Arduino
  // class itself, because PApplet defines a list() method that couldn't be
  // overridden by the static list() method we use to return the available
  // serial ports.  This class needs to be public so that the Serial class
  // can access its serialEvent() method.
  public class SerialProxy extends PApplet {
    public SerialProxy() {
    }

    public void serialEvent(Serial which) {
      try {
        // Notify the Arduino class that there's serial data for it to process.
        while (which.available() > 0)
          firmata.processInput(which.read());
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error inside Arduino.serialEvent()");
      }
    }
  }

  public class FirmataWriter implements Firmata.Writer {
    public void write(int val) {
      serial.write(val);
      //      System.out.print("<" + val + " ");
    }
  }

  public void dispose() {
    this.serial.dispose();
  }

  /**
   * Get a list of the available Arduino boards; currently all serial devices
   * (i.e. the same as Serial.list()).  In theory, this should figure out
   * what's an Arduino board and what's not.
   */
  public static String[] list() {
    return Serial.list();
  }

  /**
   * Create a proxy to an Arduino board running the Firmata 2 firmware at the
   * default baud rate of 57600.
   *
   * @param parent the Processing sketch creating this Arduino board
   * (i.e. "this").
   * @param iname the name of the serial device associated with the Arduino
   * board (e.g. one the elements of the array returned by Arduino.list())
   */
  public ArduinoLAB(PApplet parent, String iname) {
    this(parent, iname, 57600);
  }

  /**
   * Create a proxy to an Arduino board running the Firmata 2 firmware.
   *
   * @param parent the Processing sketch creating this Arduino board
   * (i.e. "this").
   * @param iname the name of the serial device associated with the Arduino
   * board (e.g. one the elements of the array returned by Arduino.list())
   * @param irate the baud rate to use to communicate with the Arduino board
   * (the firmata library defaults to 57600, and the examples use this rate,
   * but other firmwares may override it)
   */
  public ArduinoLAB(PApplet parent, String iname, int irate) {
    this.parent = parent;
    this.firmata = new Firmata(new FirmataWriter());
    this.serialProxy = new SerialProxy();
    this.serial = new Serial(serialProxy, iname, irate);

    parent.registerMethod("dispose", this);

    /*
    try {
      Thread.sleep(3000); // let bootloader timeout
    } catch (InterruptedException e) {
    }
    */
    firmata.init();
    try {
      Thread.sleep(4000); // let firmware communication timeout
    } catch (InterruptedException e) {
    }

    digitalEventMethod = findCallback("digitalEvent");
    digitalObserver.setInstance(this);
    firmata.addObserver(firmata.digitalObservable, digitalObserver);


  }


  /**
   * Returns the last known value read from the digital pin: HIGH or LOW.
   *
   * @param pin the digital pin whose value should be returned (from 2 to 13,
   * since pins 0 and 1 are used for serial communication)
   */
  public int digitalRead(int pin) {
    return firmata.digitalRead(pin);
  }

  /**
   * Returns the last known value read from the analog pin: 0 (0 volts) to
   * 1023 (5 volts).
   *
   * @param pin the analog pin whose value should be returned (from 0 to 5)
   */
  public int analogRead(int pin) {
    return firmata.analogRead(pin);
  }

  /**
   * Set a digital pin to input or output mode.
   *
   * @param pin the pin whose mode to set (from 2 to 13)
   * @param mode either Arduino.INPUT or Arduino.OUTPUT
   */
  public void pinMode(int pin, int mode) {
    try {
      firmata.pinMode(pin, mode);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error inside Arduino.pinMode()");
    }
  }

  /**
   * Write to a digital pin (the pin must have been put into output mode with
   * pinMode()).
   *
   * @param pin the pin to write to (from 2 to 13)
   * @param value the value to write: Arduino.LOW (0 volts) or Arduino.HIGH
   * (5 volts)
   */
  public void digitalWrite(int pin, int value) {
    try {
      firmata.digitalWrite(pin, value);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error inside Arduino.digitalWrite()");
    }
  }


   /*
  * SERVOS
  */
  public class SERVO {
    private int index;
    private int position = 90;

    public SERVO(int _index) {
      index = _index;
      firmata.pinMode(index, PINMODE_SERVO);
    }
    
    /**
     * Sets servo position
     * 
     * @param pos the position of servo
     */    
    public void position(int pos) {
      position = pos;
      try {
        firmata.servoWrite(index, position);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("Error inside Arduino.servoWrite()");
        }
      }
  }

  /**
   * Returns SERVO Instance
   *
   */
  public SERVO servo(int pin) {
    return new SERVO(pin);
  }
  
  
   /*
  * ANALOG
  */
  public class ANALOG {
    private int index;
    
    public ANALOG(int _index) {
      index = _index;
    }
    
    /**
     * Starts reporting
     * 
     */    
    public void on() {
      firmata.pinMode(index, PINMODE_ANALOG);
      firmata.reportAnalog(index, 1);
    }

    /**
     * Stops reporting
     * 
     */    
    public void off() {
      firmata.reportAnalog(index, 0);
    }
    
    /**
     * Gets last received value of analog
     * 
     */        
    public int value() {
      return firmata.analogRead(index);
    }

    /**
     * Write pwm to a analog pin
     *
     * @param value the value to write: 0 to 255
     */
    public void write(int value) {
      firmata.pinMode(index, PINMODE_ANALOG);
      try {
        firmata.analogWrite(index, value);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error inside Arduino.analogWrite()");
      }
    }

  }

  /**
   * Returns ANALOG Instance
   *
   */
  public ANALOG analog(int index) {
    return new ANALOG(index);
  }    


   /*
  * DIGITAL
  */
  public class DIGITAL  {
    public int pin;
    public int port;
    
    public DIGITAL(int _pin) {
      pin = _pin;
      port = pin >> 3;
    }

    /**
     * Starts reporting
     * 
     */    
    public void on() {
      firmata.pinMode(pin, INPUT);
      firmata.reportDigital(port, 1);
    }

    /**
     * Stops reporting
     * 
     */    
    public void off() {
      firmata.reportDigital(port, 0);
    }
    
    /**
     * Gets last received value of digital pin
     * 
     */        
    public int value() {
      return firmata.digitalRead(pin);
    }
    
    /**
     * Gets last received value of digital port
     * 
     */        
    public int portValue() {
      return firmata.digitalReadPort(port);
    }
    
    /**
     * Enables or disables pullup in digital pin
     * 
     * @param enable true to enable, false to disable pullup
     */        
    public void pullup(boolean enable) {
      int mode = (enable) ? INPUT_PULLUP : INPUT;
      firmata.pinMode(pin, mode);
    }

  /**
   * Write to a digital pin (the pin must have been put into output mode with
   * pinMode()).
   *
   * @param value the value to write: Arduino.LOW (0 volts) or Arduino.HIGH
   * (5 volts)
   */
  public void write(int value) {
      firmata.pinMode(pin, OUTPUT);
      try {
        firmata.digitalWrite(pin, value);
      } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error inside Arduino.digitalWrite()");
      }
    }

  }

  /**
   * Returns DIGITAL Instance
   *
   * @param pin the digital pin
   */
  public DIGITAL digital(int pin) {
    return new DIGITAL(pin);
  }

  private Method findCallback(final String name) {
    try {
      return parent.getClass().getMethod(name, int[].class);
    } catch (Exception e) {
    }
    // Permit callback(Object) as alternative to callback(Serial).
    try {
      return parent.getClass().getMethod(name, this.getClass());
    } catch (Exception e) {
    }
    return null;
  }

  public void digitalEvent(Object data) {
    if(digitalEventMethod == null) return;
    try {
      digitalEventMethod.invoke(parent, data);
    } catch (Exception e) {
      throw new RuntimeException("Callback error");
    }      
  }


  /*
  * I2C
  */
  public class I2C {
    protected int address;
    private int delay;
    protected HashMap<Integer, REG> registers = new HashMap();

    public class REG {
      int register;

      public REG(int _register) {
        register = _register;
      }

      /**
       * Starts reporting
       * 
       * @param bytes the amount of bytes to report from register
       */    
      public void on(int bytes) {

        int address_lsb = address & 0x7F;
        int address_msb = (address >> 7) & 0x7F;
    
        if (address_msb > 0) {
          address_msb |= FIRMATA_I2C_10_BIT;
        }
        if (bytes == 0) {
          address_msb |= FIRMATA_I2C_STOP_READING;
        }
        else {
          address_msb |= FIRMATA_I2C_READ_CONTINUOUS;		
        }
    
        int bytes_lsb = (bytes & 0x7F);
        int bytes_msb = (bytes >> 7) & 0x7F;
    
        int register_lsb = (register & 0x7F);
        int register_msb = (register >> 7) & 0x7F;
    
        int[] data = { FIRMATA_I2C_REQUEST, address_lsb, address_msb, register_lsb, register_msb, bytes_lsb, bytes_msb };
        firmata.sendSysex(data);
            
      }
  
      /**
       * Stops reporting
       * 
       */    
      public void off() {
        on(0);
      }
      
      /**
       * Gets last received value of analog
       * 
       */        
      public int[] value() {
        return firmata.getI2CInputs(address, register);
      }

      /**
       * Performs a write of data on the register 
       * 
       * @param data the array of data to write into register
       */        
      public void write(int[] data) {
        int address_lsb = address & 0x7F;
        int address_msb = (address >> 7) & 0x7F;
        if (address_msb > 0) {
          address_msb |= FIRMATA_I2C_10_BIT;
        }
        address_msb |= FIRMATA_I2C_WRITE;
        int register_lsb = (register & 0x7F);
        int register_msb = (register >> 7) & 0x7F;

        int[] dataToWrite = new int[data.length * 2 + 5];
        dataToWrite[0] = FIRMATA_I2C_REQUEST;
        dataToWrite[1] = address_lsb;
        dataToWrite[2] = address_msb;
        dataToWrite[3] = register_lsb;
        dataToWrite[4] = register_msb;
        int j = 5;
        for (int d : data) {
          dataToWrite[j++] = d & 0x7F;
          dataToWrite[j++] = (d >> 7) & 0x7F;
        }
       	firmata.sendSysex(dataToWrite);
      }   

    }

    public I2C(int _address) {
      address = _address;
      delay = 50;
      int[] data = { FIRMATA_I2C_CONFIG, delay, 0 };
      firmata.sendSysex(data);
    }

    public I2C(int _address, int _delay) {
      address = _address;
      delay = _delay;
      int[] data = { FIRMATA_I2C_CONFIG, delay & 0x7F, (delay>>7) & 0x7F };
      firmata.sendSysex(data);
    }

    /**
     * Returns REGISTER Instance.  If the instance exists, returns the same existent
     *
     * @param _register the address of the register on device
     */    
    public REG register(int _register) {
      if(!registers.containsKey(_register)) {
        REG r = new REG(_register);
        registers.put(_register, r);
      }
      return registers.get(_register);

    }


  }

  /**
   * Returns I2C Instance
   *
   * @param address the address of device
   */
  public I2C i2c(int address) {
    return new I2C(address);
  }
  
  /**
   * Returns I2C Instance with delay
   *
   * @param address the address of device
   * @param delay the delay between write and read on device in microseconds
   */
  public I2C i2c(int address, int delay) {
    return new I2C(address, delay);
  }
  
}