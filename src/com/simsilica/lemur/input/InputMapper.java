/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.input;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Objects;

import com.jme3.input.*;
import com.jme3.input.event.*;
import com.jme3.util.SafeArrayList;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class InputMapper
{
    static Logger log = LoggerFactory.getLogger(InputMapper.class);
    
    private InputManager inputManager;
    private InputObserver listener;
 
    private Set<String> activeGroups = new HashSet<String>();

    
    private Map<Object,StateGroupIndex> stateIndex = new HashMap<Object,StateGroupIndex>();
    private Set<StateGroup> activeStates = new HashSet<StateGroup>();

    private Map<JoystickAxis,Axis> joystickAxisMap = new HashMap<JoystickAxis,Axis>(); 
    private Map<JoystickButton,Button> joystickButtonMap = new HashMap<JoystickButton,Button>(); 
 
    private Map<FunctionId, FunctionListeners> listenerMap = new HashMap<FunctionId, FunctionListeners>();

    private double tpf = 0;
    private long lastFrameNanos;
    
    public InputMapper( InputManager inputManager )
    {
        this.inputManager = inputManager;
        this.listener = new InputObserver();
        
        inputManager.addRawInputListener(listener);
        
        Joystick[] sticks = inputManager.getJoysticks();
        if( sticks != null )
            {
            for( Joystick j : sticks )
                {
                mapJoystick(j);
                }
            }
            
        activeGroups.add( FunctionId.DEFAULT_GROUP );
        
        lastFrameNanos = System.nanoTime();
    }
 
    public void activateGroup( String group )
    {
        if( log.isTraceEnabled() )
            log.trace( "activate:" + group );    
        activeGroups.add(group);
    }
    
    public void deactivateGroup( String group )
    {
        if( log.isTraceEnabled() )
            log.trace( "deactivate:" + group );    
        activeGroups.remove(group);
    }
    
    public void release()
    {
        inputManager.removeRawInputListener(listener);
    }

    protected void mapJoystick( Joystick j )
    {
        // We attempt to determine what kind of stick it is so
        // that we can provide more intelligent button and axis mappings
        if( j.getAxis( JoystickAxis.Z_ROTATION ) != null && j.getAxis( JoystickAxis.Z_AXIS ) != null )
            {
            mapGamepad(j);
            return;
            }
        
        // Else it's a generic one
        joystickAxisMap.put( j.getXAxis(), Axis.JOYSTICK_X );
        joystickAxisMap.put( j.getYAxis(), Axis.JOYSTICK_Y );
        joystickAxisMap.put( j.getPovXAxis(), Axis.JOYSTICK_HAT_X );
        joystickAxisMap.put( j.getPovYAxis(), Axis.JOYSTICK_HAT_Y );
        
        for( JoystickButton b : j.getButtons() )
            {
            String id = b.getLogicalId();
            if( !Character.isDigit( id.charAt(0) ) )
                continue;
            
            int idVal = Integer.parseInt(id) + 1;
            joystickButtonMap.put( b, new Button( "joystick_" + idVal, "Button " + idVal ) );
            }
    }

    protected void mapGamepad( Joystick j )
    {
        joystickAxisMap.put( j.getXAxis(), Axis.JOYSTICK_LEFT_X );
        joystickAxisMap.put( j.getYAxis(), Axis.JOYSTICK_LEFT_Y );
        joystickAxisMap.put( j.getAxis(JoystickAxis.Z_AXIS), Axis.JOYSTICK_RIGHT_X );
        joystickAxisMap.put( j.getAxis(JoystickAxis.Z_ROTATION), Axis.JOYSTICK_RIGHT_Y );
        joystickAxisMap.put( j.getPovXAxis(), Axis.JOYSTICK_HAT_X );
        joystickAxisMap.put( j.getPovYAxis(), Axis.JOYSTICK_HAT_Y );
        
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_0 ), Button.JOYSTICK_BUTTON1 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_1 ), Button.JOYSTICK_BUTTON2 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_2 ), Button.JOYSTICK_BUTTON3 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_3 ), Button.JOYSTICK_BUTTON4 );
 
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_4 ), Button.JOYSTICK_LEFT1 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_5 ), Button.JOYSTICK_RIGHT1 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_6 ), Button.JOYSTICK_LEFT2 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_7 ), Button.JOYSTICK_RIGHT2 );
 
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_8 ), Button.JOYSTICK_SELECT );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_9 ), Button.JOYSTICK_START );
 
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_10 ), Button.JOYSTICK_LEFT3 );
        joystickButtonMap.put( j.getButton( JoystickButton.BUTTON_11 ), Button.JOYSTICK_RIGHT3 );               
    }

    protected StateGroupIndex getIndex( Object state, boolean create )
    {
        StateGroupIndex result = stateIndex.get(state);
        if( result == null && create )
            {
            result = new StateGroupIndex(state);
            stateIndex.put(state, result);
            }
        return result;
    }

    protected FunctionListeners getFunctionListeners( FunctionId f, boolean create )
    {
        FunctionListeners result = listenerMap.get(f);
        if( result == null && create )  
            {
            result = new FunctionListeners();
            listenerMap.put(f, result);
            }
        return result;
    }   

    public void map( FunctionId function, Axis axis, Object... pressed )
    {
        addMapping( function, InputState.POSITIVE, axis, pressed );
    } 

    public void map( FunctionId function, InputState bias, Axis axis, Object... pressed )
    {
        addMapping( function, bias, axis, pressed );
    } 

    public void map( FunctionId function, Button button, Object... pressed )
    {
        addMapping( function, InputState.POSITIVE, button, pressed );
    } 

    public void map( FunctionId function, InputState bias, Button button, Object... pressed )
    {
        addMapping( function, bias, button, pressed );
    }
     
    public void map( FunctionId function, int mainKeyCode, Object... pressed )
    {
        map( function, InputState.POSITIVE, mainKeyCode, pressed );
    } 

    public void map( FunctionId function, InputState bias, int mainKeyCode, Object... pressed )
    {
        addMapping( function, bias, mainKeyCode, pressed );
    } 

    protected void addMapping( FunctionId function, InputState bias, Object primary, Object... modifiers )
    {
        StateGroup g = new StateGroup( function, bias, primary, modifiers );
        getIndex(primary, true).addGroup(g);
        for( Object o : modifiers )
            getIndex(o, true).addGroup(g);
    }

    public void addStateListener( StateFunctionListener l, FunctionId... functions )
    {
        if( functions == null || functions.length == 0 )
            throw new RuntimeException( "No function IDs specified." );
            
        for( FunctionId function : functions )
            {
            FunctionListeners listeners = getFunctionListeners(function, true);
            listeners.stateListeners.add(l);
            }
    } 

    public void removeStateListener( StateFunctionListener l, FunctionId... functions )
    {
        if( functions == null || functions.length == 0 )
            throw new RuntimeException( "No function IDs specified." );
            
        for( FunctionId function : functions )
            {
            FunctionListeners listeners = getFunctionListeners(function, false);
            if( listeners == null )
                continue;
            listeners.stateListeners.remove(l);
            }
    } 

    public void addAnalogListener( AnalogFunctionListener l, FunctionId... functions )
    {
        if( functions == null || functions.length == 0 )
            throw new RuntimeException( "No function IDs specified." );
            
        for( FunctionId function : functions )
            {
            FunctionListeners listeners = getFunctionListeners(function, true);
            listeners.analogListeners.add(l);
            }
    } 

    public void removeAnalogListener( AnalogFunctionListener l, FunctionId... functions )
    {
        if( functions == null || functions.length == 0 )
            throw new RuntimeException( "No function IDs specified." );
            
        for( FunctionId function : functions )
            {
            FunctionListeners listeners = getFunctionListeners(function, false);
            if( listeners == null )
                continue;
            listeners.analogListeners.remove(l);
            }
    } 

    public void addDelegate( FunctionId func, Object target, String methodName )
    {
        addDelegate(func, target, methodName, false);
    }                                      

    public void addDelegate( FunctionId func, Object target, String methodName, boolean passArgument )
    {
        addStateListener( new StateMethodDelegate(target, methodName, passArgument), func );
    }                                      

    public void removeDelegate( FunctionId func, Object target, String methodName )
    {
        FunctionListeners listeners = getFunctionListeners(func, false);
        if( listeners == null )
            return; // nothing to remove

        for( StateFunctionListener l : listeners.stateListeners )
            {
            if( !(l instanceof StateMethodDelegate) )
                continue;
            StateMethodDelegate smd = (StateMethodDelegate)l;
            
            if( smd.getTarget() != target )
                continue;
            if( !smd.getMethodName().equals(methodName) )
                continue;
            
            // remove it
            listeners.stateListeners.remove(l);
            }
    }    

    protected void activate( StateGroup g )
    {
        if( !activeStates.add(g) )
            return;
            
        if( log.isTraceEnabled() )
            log.trace( "activate(" + g.function + ":" + g.bias + ")" );
        
        // So the activation state changed and now we
        // should notify those listeners... actually... that should
        // be done in the update loop since even when a group is
        // activated it's state value can change (from positive to negative
        // for example).
        
    }
    
    protected void deactivate( StateGroup g )
    {
        if( !activeStates.remove(g) )
            return;            
            
        if( log.isTraceEnabled() )
            log.trace( "deactivate(" + g.function + ":" + g.bias + ")" );
        
        // Need to make sure that the group is set back to
        // ground-state so it will show up right when activated again
        notifyValueActive( g.getFunction(), 0 );
        g.resetValue();
    }
    
    public void update()
    {
        for( StateGroup g : activeStates )
            {
            double value = getIndex(g.getPrimary(), false).getValue();            
            g.updateValue(value);
            notifyValueActive( g.getFunction(), g.getValue() );
            }
    }

    protected void notifyStateChanged( FunctionId function, InputState value )
    {
        FunctionListeners listeners = getFunctionListeners(function, false);
        if( listeners == null )
            return;
 
        listeners.notifyStateChanged( function, value );        
    }
    
    protected void notifyValueActive( FunctionId function, double value )
    {
        FunctionListeners listeners = getFunctionListeners(function, false); 
        if( listeners == null )
            return;
 
        listeners.notifyAnalogUpdate( function, value );        
    }
                                         
    protected InputState valueToState( double val )
    {
        if( val < -0.01 )
            return InputState.NEGATIVE;
        else if( val > 0.01 )
            return InputState.POSITIVE;
        else
            return InputState.OFF;
    }                                     
                                     
    protected class FunctionListeners
    {    
        SafeArrayList<StateFunctionListener> stateListeners = new SafeArrayList<StateFunctionListener>(StateFunctionListener.class);
        SafeArrayList<AnalogFunctionListener> analogListeners = new SafeArrayList<AnalogFunctionListener>(AnalogFunctionListener.class);
 
        public void notifyStateChanged( FunctionId function, InputState value )
        {
            for( StateFunctionListener l : stateListeners.getArray() )
                l.valueChanged( function, value, tpf );
        }

        public void notifyAnalogUpdate( FunctionId function, double value )
        {
            for( AnalogFunctionListener l : analogListeners.getArray() )
                l.valueActive( function, value, tpf );
        }  
    }
    
    protected class StateGroup implements Comparable<StateGroup> 
    {
        Object primaryState;
        Object[] modifiers;
        FunctionId function;
        InputState bias;
        double lastValue;
        InputState lastState;
        
        public StateGroup( FunctionId function, InputState bias, Object primaryState, Object... modifiers )
        {
            this.function = function;
            this.bias = bias;
            this.primaryState = primaryState;
            this.modifiers = modifiers;
            resetValue(); 
        }
        
        public int compareTo( StateGroup other )
        {
            // We want to sort the conditions so that the most
            // specific ones are first.  So if other is more specific
            // than us then we return -1 if it's less specific we return 1
            
            // We base this completely on size right now            
            int c = modifiers == null ? 0 : modifiers.length;
            int oc = other.modifiers == null ? 0 : other.modifiers.length;
            
            return oc - c;
        }

        public FunctionId getFunction()
        {
            return function;
        }

        public double getValue()
        {
            return lastValue;
        }

        public void updateValue( double value )
        {
            double adjusted = value * bias.asNumber();
            if( lastValue == adjusted )
                return;
            lastValue = adjusted;
 
            if( log.isTraceEnabled() )
                log.trace( "Value changed for:" + function + " bias:" + bias ); 
            
            InputState state = valueToState(value);
            updateState(state);                            
        }

        public void updateState( InputState state )
        {
            if( lastState == state )
                return;
            lastState = state;
            
            // Except maybe function state should be kept centrally by function
            // We could have muliple state groups firing against the same
            // function ID.  Like the user pressing forward and back at the same
            // time.  Hmmm... per group state might be better in that case anyway
            // since then we don't send double state every frame.  We'll send state
            // when forward changes and when back changes and that's it.
            notifyStateChanged( function, lastState );            
        }

        public void resetValue()
        {
            if( valueToState(lastValue) == InputState.OFF )
                return;
            
            lastValue = 0;
            lastState = InputState.OFF;
            
            // And we need to notify state listeners
            notifyStateChanged( function, lastState );            
        }

        public Object getPrimary()
        {
            return primaryState;
        }
        
        public boolean isPrimary( Object state )
        {
            if( state == primaryState )
                return true;
            return Objects.equal(state, primaryState);
        }
        
        public boolean isTrue()
        {
            if( !activeGroups.contains(function.getGroup()) )
                return false;                       
            if( !getIndex(primaryState, false).isOn() )
                return false;
            for( Object o : modifiers )
                {
                if( !getIndex(o, false).isOn() )
                    return false;
                }
            return true;
        }
        
        public boolean areModifiersTrue()
        {
            if( !activeGroups.contains(function.getGroup()) )
                return false;                       
            for( Object o : modifiers )
                {
                if( !getIndex(o, false).isOn() )
                    return false;
                }
            return true;
        } 
        
        public String toString()
        {
            return "StateGroup[" + function + ":" + bias + ", " + primaryState + "]";
        }
    }        
 
    protected class StateGroupIndex 
    {
        Object localState;
        List<StateGroup> groups = new ArrayList<StateGroup>();
        double lastValue;
        
        public StateGroupIndex( Object localState )
        {
            this.localState = localState;
        }
 
        public boolean isOn()
        {
            return valueToState(lastValue) != InputState.OFF;
        }

        public double getValue()
        {
            return lastValue;
        }
        
        public StateGroup addGroup( StateGroup g )
        {
            for( int i = 0; i < groups.size(); i++ )
                {
                StateGroup existing = groups.get(i);
                if( existing.compareTo(g) > 0 )
                    {
                    // the current entry is less specific than the supplied one
                    groups.add( i, g );
                    return g;
                    } 
                }
            
            // Else just add it
            groups.add( g );                
            return g;
        }
 
        public void refresh()
        {
            // Only need to activate the first primary... and
            // then only if the state would not be "OFF"                                   
            boolean activatePrimary = isOn();
            
            for( StateGroup g : groups )
                {
                if( !g.isTrue() )
                    {
                    // Deactivate it
                    deactivate(g);
                    
                    // If this wasn't its primary state then we need
                    // to refresh it's primary index since another state
                    // may now get to run
                    if( !g.isPrimary(localState) )  
                        {
                        Object primary = g.getPrimary();
                        getIndex(primary, false).refresh();
                        }
                    }
                else if( g.isPrimary(localState) )
                    {
                    if( activatePrimary )
                        {
                        // Group needs to be activated if it wasn't
                        activate(g);
                        activatePrimary = false;
                        }
                    else
                        {
                        // Group needs to be deactivated if it was active
                        // since now there is a better one.
                        deactivate(g);
                        }
                    }
                else
                    {
                    // the group is active but this is not its
                    // primary state.  So we need to have the index
                    // for the primary state refresh itself
                    Object primary = g.getPrimary();
                    getIndex(primary, false).refresh();
                    } 
                } 
        }
 
        
        public void updateValue( double val )
        {
            if( lastValue == val )
                return;
            lastValue = val;
            refresh();
        }
        
        public void instantUpdate( double val )
        {
            // Find the first primary true group and
            // send the value to its listeners
            for( StateGroup g : groups )
                {
                if( !g.areModifiersTrue() )
                    continue;
                    
                if( !g.isPrimary(localState) )
                    continue;
 
                // Notify and break
                // Technically we should send analog and a state
                // on, state off... I let the state off go because
                // I can't think of a use-case for it at the moment.
                InputState state = valueToState(val);
 
                notifyStateChanged( g.getFunction(), state );
                notifyValueActive( g.getFunction(), val );
                break;  // first one found wins                                  
                }                
        }
    }
               
 
    
    protected class InputObserver implements RawInputListener
    {
        public void onJoyAxisEvent(JoyAxisEvent evt) 
        {
            JoystickAxis a = evt.getAxis();
            Joystick j = a.getJoystick();
            float val = evt.getValue();

            // Below a certain threshold, call the value 0
            if( Math.abs(val) < 0.01 )
                val = 0;

            Axis axis = joystickAxisMap.get(a);
            if( axis == null )
                {
                System.out.println( "WARN: no axis mapping for:" + a );
                return;
                }

            StateGroupIndex index = getIndex(axis, false);
            if( index == null )
                return;
            index.updateValue(val);
        }

        public void onJoyButtonEvent(JoyButtonEvent evt) 
        {
            Button b = joystickButtonMap.get(evt.getButton());
            if( b == null )
                {
                System.out.println( "WARN: no button mapping for:" + evt.getButton() );
                return;
                }

            StateGroupIndex index = getIndex(b, false);
            if( index == null )
                return;
            double value = evt.isPressed() ? 1.0 : 0.0;
            
            index.updateValue(value);
        }

        public void beginInput() 
        {
            long time = System.nanoTime();
            tpf = (time - lastFrameNanos) / 1000000000.0;
            lastFrameNanos = time;
        }
    
        public void endInput() 
        {
            update();
        }
 
        protected void instantUpdate( Axis a, double value )
        {
            StateGroupIndex index = getIndex(a, false);
            if( index == null )
                return;
            
            index.instantUpdate( value );
        }
    
        public void onMouseMotionEvent(MouseMotionEvent evt) 
        {
            // All axes could be different so we can't really
            // consolidate.
            
            // While the numbers in these divisions are a bit magic,
            // they are to bring the values into the normal analog
            // range... roughly.
            // In a sense, these values already have tpf mixed in
            // because the deltas will be higher when frames are longer
            // because we are capturing less often.  So that the user
            // can multiply by tpf, we will divide it out again.
            if( evt.getDeltaWheel() != 0 )
                {
                //instantUpdate( Axis.MOUSE_WHEEL, evt.getDeltaWheel() / (1024.0 * tpf) );
                // The mouse wheel is kind of a special case because the
                // spinning tends to have hard-stops that make the progressions
                // in even increments.  So it doesn't act analog.  We'll just
                // hardcode a divisor to get it typically in the 1.0 range (based
                // on experimentation)
                instantUpdate( Axis.MOUSE_WHEEL, evt.getDeltaWheel() / 120.0 );    
                }
            if( evt.getDX() != 0 )
                {
                instantUpdate( Axis.MOUSE_X, evt.getDX() / (1024.0 * tpf) ); 
                } 
            if( evt.getDY() != 0 )
                {
                instantUpdate( Axis.MOUSE_Y, evt.getDY() / (1024.0 * tpf) ); 
                } 
        }
    
        public void onMouseButtonEvent(MouseButtonEvent evt) 
        {
            Button b = null;
            switch( evt.getButtonIndex() )
                {
                case 0:
                    b = Button.MOUSE_BUTTON1;
                    break;
                case 1:
                    b = Button.MOUSE_BUTTON2;
                    break;
                case 2:
                    b = Button.MOUSE_BUTTON3;
                    break;
                default:
                    int i = evt.getButtonIndex() + 1; 
                    b = new Button( "mouse_" + i, "Mouse Button " + i );
                    break;
                }

            StateGroupIndex index = getIndex(b, false);
            if( index == null )
                return;
            double value = evt.isPressed() ? 1.0 : 0.0;
            
            index.updateValue(value);
        }
    
        public void onKeyEvent(KeyInputEvent evt) 
        {
            if( evt.isRepeating() )
                return;
            
            StateGroupIndex index = getIndex(evt.getKeyCode(), false);
            if( index == null )
                return;
            double value = evt.isPressed() ? 1.0 : 0.0;
            index.updateValue(value);
        }
    
        public void onTouchEvent(TouchEvent evt) 
        {
        }            
    }
}

