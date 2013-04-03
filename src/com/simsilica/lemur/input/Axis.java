/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.input;

import com.google.common.base.Objects;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class Axis
{
    public static final Axis MOUSE_X = new Axis( "mouse_x", "Mouse Left/Right" );
    public static final Axis MOUSE_Y = new Axis( "mouse_y", "Mouse Up/Down" );
    public static final Axis MOUSE_WHEEL = new Axis( "mouse_wheel", "Mouse Wheel" );

    // For a generic single-stick joystick
    public static final Axis JOYSTICK_X = new Axis( "joystick_x", "Joystick Left/Right" );
    public static final Axis JOYSTICK_Y = new Axis( "joystick_y", "Joystick Up/Down" );
    
    public static final Axis JOYSTICK_LEFT_X = new Axis( "joystick_left_x", "Joystick (left) Left/Right" );
    public static final Axis JOYSTICK_LEFT_Y = new Axis( "joystick_left_y", "Joystick (left) Up/Down" );
    public static final Axis JOYSTICK_RIGHT_X = new Axis( "joystick_right_x", "Joystick (right) Left/Right" );
    public static final Axis JOYSTICK_RIGHT_Y = new Axis( "joystick_right_y", "Joystick (right) Up/Down" );
    public static final Axis JOYSTICK_HAT_X = new Axis( "hat_x", "Joystick HAT Left/Right" );
    public static final Axis JOYSTICK_HAT_Y = new Axis( "hat_y", "Joystick HAT Up/Down" );
 
    
    private String id;
    private String name;
    
    public Axis( String id, String name )
    {
        this.id = id;
        this.name = name;
    }
    
    public String getId()
    {
        return id;
    }
    
    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override
    public boolean equals( Object o )
    {
        if( o == this )
            return true;
        if( o == null || o.getClass() != getClass() )
            return false;
        Axis other = (Axis)o;
        if( !Objects.equal( id, other.id ) )
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return "Axis[" + id + "]";
    }
}
