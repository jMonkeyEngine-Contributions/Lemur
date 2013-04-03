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
public class Button
{
    public static final Button MOUSE_BUTTON1 = new Button( "mouse_1", "Mouse Button 1" );
    public static final Button MOUSE_BUTTON2 = new Button( "mouse_2", "Mouse Button 2" );
    public static final Button MOUSE_BUTTON3 = new Button( "mouse_3", "Mouse Button 3" );

    public static final Button JOYSTICK_BUTTON1 = new Button( "joystick_1", "Joystick Button 1" );
    public static final Button JOYSTICK_BUTTON2 = new Button( "joystick_2", "Joystick Button 2" );
    public static final Button JOYSTICK_BUTTON3 = new Button( "joystick_3", "Joystick Button 3" );
    public static final Button JOYSTICK_BUTTON4 = new Button( "joystick_4", "Joystick Button 4" );
    public static final Button JOYSTICK_BUTTON5 = new Button( "joystick_5", "Joystick Button 5" );
    public static final Button JOYSTICK_BUTTON6 = new Button( "joystick_6", "Joystick Button 6" );
    public static final Button JOYSTICK_BUTTON7 = new Button( "joystick_7", "Joystick Button 7" );
    public static final Button JOYSTICK_BUTTON8 = new Button( "joystick_8", "Joystick Button 8" );
    public static final Button JOYSTICK_BUTTON9 = new Button( "joystick_9", "Joystick Button 9" );
    public static final Button JOYSTICK_BUTTON10 = new Button( "joystick_10", "Joystick Button 10" );
    public static final Button JOYSTICK_BUTTON11 = new Button( "joystick_11", "Joystick Button 11" );
    public static final Button JOYSTICK_BUTTON12 = new Button( "joystick_12", "Joystick Button 12" );
     
    public static final Button JOYSTICK_START = new Button( "joystick_start", "Start" );
    public static final Button JOYSTICK_SELECT = new Button( "joystick_select", "Select" );

    public static final Button JOYSTICK_LEFT1 = new Button( "joystick_left1", "Left 1" );
    public static final Button JOYSTICK_LEFT2 = new Button( "joystick_left2", "Left 2" );
    public static final Button JOYSTICK_LEFT3 = new Button( "joystick_left3", "Left 3" );

    public static final Button JOYSTICK_RIGHT1 = new Button( "joystick_right1", "Right 1" );
    public static final Button JOYSTICK_RIGHT2 = new Button( "joystick_right2", "Right 2" );
    public static final Button JOYSTICK_RIGHT3 = new Button( "joystick_right3", "Right 3" );
     
    private String id;
    private String name;
    
    public Button( String id, String name )
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
        Button other = (Button)o;
        if( !Objects.equal( id, other.id ) )
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return "Button[" + id + "]";
    }
}
