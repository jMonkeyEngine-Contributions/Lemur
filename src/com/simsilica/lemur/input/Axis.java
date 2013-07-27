/*
 * $Id$
 *
 * Copyright (c) 2012-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.input;

import com.google.common.base.Objects;


/**
 *  Represents a logical anglog input axis that can be used
 *  for mapping inputs to logical functions.
 *
 *  @author    Paul Speed
 */
public class Axis {

    public static final Axis MOUSE_X = new Axis("mouse_x", "Mouse Left/Right");
    public static final Axis MOUSE_Y = new Axis("mouse_y", "Mouse Up/Down");
    public static final Axis MOUSE_WHEEL = new Axis("mouse_wheel", "Mouse Wheel");

    // For a generic single-stick joystick
    public static final Axis JOYSTICK_X = new Axis("joystick_x", "Joystick Left/Right");
    public static final Axis JOYSTICK_Y = new Axis("joystick_y", "Joystick Up/Down");

    public static final Axis JOYSTICK_LEFT_X
                                = new Axis("joystick_left_x", "Joystick (left) Left/Right");
    public static final Axis JOYSTICK_LEFT_Y
                                = new Axis("joystick_left_y", "Joystick (left) Up/Down");
    public static final Axis JOYSTICK_RIGHT_X
                                = new Axis("joystick_right_x", "Joystick (right) Left/Right");
    public static final Axis JOYSTICK_RIGHT_Y
                                = new Axis("joystick_right_y", "Joystick (right) Up/Down");
    public static final Axis JOYSTICK_HAT_X
                                = new Axis("hat_x", "Joystick HAT Left/Right");
    public static final Axis JOYSTICK_HAT_Y
                                = new Axis("hat_y", "Joystick HAT Up/Down");


    private String id;
    private String name;

    public Axis( String id, String name ) {
        this.id = id;
        this.name = name;
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
        Axis other = (Axis)o;
        if( !Objects.equal(id, other.id) )
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Axis[" + id + "]";
    }
}
