/*
 * $Id$
 * 
 * Copyright (c) 2019, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.input;

import java.util.Objects;

/**
 *  Represents a logical input device like a specific game controller.
 *
 *  @author    Paul Speed
 */
public class InputDevice {
    
    public static final InputDevice JOYSTICK1 = InputDevice.joystick(1);
    public static final InputDevice JOYSTICK2 = InputDevice.joystick(2);
    
    private String id;
    private String name;

    public InputDevice( String id, String name ) {
        this.id = id;
        this.name = name;
    }

    /**
     *  Creates a new logical joystick with the specified ID.  Note:
     *  to map reported events IDs should be one-based, ie: first
     *  joystick will be reported as InputDevice(1), second as InputDevice(2), etc. 
     */
    public static InputDevice joystick( int id ) {
        // Note: no underscore... this makes it visually distinct
        // from the IDs used for buttons.
        return new InputDevice("joystick" + id, "Joystick " + id);
    }

    /**
     *  Return the InputDevice-specific version of the specified
     *  Button.
     */
    public DeviceButton button( Button b ) {
        return new DeviceButton(this, b);
    }  

    /**
     *  Return the InputDevice-specific version of the specified
     *  Axis.
     */
    public DeviceAxis axis( Axis a ) {
        return new DeviceAxis(this, a);
    }  

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this )
            return true;
        if( o == null || o.getClass() != getClass() )
            return false;
        InputDevice other = (InputDevice)o;
        if( !Objects.equals(id, other.id) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InputDevice[" + id + "]";
    }
    
    public static class DeviceButton extends Button {
        private InputDevice device;
        private Button button;
        
        public DeviceButton( InputDevice device, Button button ) {
            super(button.getId(), button.getName());
            this.device = device;
            this.button = button;
        }
 
        /**
         *  Returns the specific device to which this button applies. 
         */       
        public InputDevice getDevice() {
            return device;
        }
 
        /**
         *  Returns the generic button identifier that is not specific
         *  to this device.
         */       
        public Button getButton() {
            return button;
        }
        
        public int hashCode() {
            return Objects.hash(device, button);
        }
        
        public boolean equals( Object o ) {
            if( o == this ) {
                return true;
            }
            if( o == null || o.getClass() != getClass() ) {
                return false;
            }
            DeviceButton other = (DeviceButton)o; 
            if( !Objects.equals(other.button, button) ) {
                return false;
            }
            if( !Objects.equals(other.device, device) ) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            return device + "->" + button;
        }
    }
    
    public static class DeviceAxis extends Axis {
        private InputDevice device;
        private Axis axis;
        
        public DeviceAxis( InputDevice device, Axis axis ) {
            super(axis.getId(), axis.getName());
            this.device = device;
            this.axis = axis;
        }
 
        /**
         *  Returns the specific device to which this axis applies. 
         */       
        public InputDevice getDevice() {
            return device;
        }
 
        /**
         *  Returns the generic axis identifier that is not specific
         *  to this device.
         */       
        public Axis getAxis() {
            return axis;
        }
        
        public int hashCode() {
            return Objects.hash(device, axis);
        }
        
        public boolean equals( Object o ) {
            if( o == this ) {
                return true;
            }
            if( o == null || o.getClass() != getClass() ) {
                return false;
            }
            DeviceAxis other = (DeviceAxis)o; 
            if( !Objects.equals(other.axis, axis) ) {
                return false;
            }
            if( !Objects.equals(other.device, device) ) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            return device + "->" + axis;
        }
    }
}
