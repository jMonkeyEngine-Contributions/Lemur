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

import java.util.*;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.input.*;
import com.jme3.input.event.*;
import com.jme3.util.SafeArrayList;

import com.simsilica.lemur.input.InputDevice.DeviceAxis;
import com.simsilica.lemur.input.InputDevice.DeviceButton;

/**
 *  Wraps the normal InputManager to provide enhanced
 *  function mapping that completely decouples the registration
 *  of input from the listeners that are executed.  Inputs
 *  can be dynamically registered to trigger certain logical
 *  "functions" based on a FunctionId.  Separately, analog and
 *  state change listeners can be registered for specific FunctionIds.
 *  In addition to decoupling, any number of inputs can be registered
 *  to a particular FunctionId at any time.  This is different than
 *  the default InputManager which will throw an exception if new inputs
 *  are added to an existing trigger mapping.
 *
 *  <p>The design of InputMapper is setup to better handle the
 *  case where an application wants to allow the user to configure
 *  their own input mappings through a GUI... and specifically where
 *  the application may allow multiple separately configured inputs
 *  to trigger the same set of functions.  For example, an application
 *  may map a joystick axis, the mouse wheel, and/or one or two keys
 *  to a specific function.</p>
 *
 *  <p>Also, any logical FunctionId can be treated as both an analog
 *  input or a trinary state input.  Key inputs can trigger analog
 *  functions and analog axes can trigger Positive, Off, Negative
 *  state transitions.  For example, the key 'A' can be mapped to
 *  a "move" function and 'S' can be mapped to the "move" function
 *  as being inverted.  'A' and 'S' will then trigger analog values
 *  of 1.0 and -1.0 respectively.  The "move" function can then also
 *  be mapped to an analog joystick axis and the code remains the
 *  same.  On the other end, if a StateFunctionListener is registered
 *  with to a logical FunctionId being fed by an analog Axis then
 *  it will see Positive, Negative, and Off as the axis transitions
 *  from greater than 0, less than 0, and 0, respectively. (A small
 *  neutrality threshold is effected to make extremely small analog
 *  values operate as 0.)</p>
 *
 *  <p>When registering inputs, a series of combinations can be
 *  included that must all be present to invoke the function.  As
 *  an example, "mouse wheel" and "ctrl + mouse wheel" can be treated
 *  as two separate function triggers.</p>
 *
 *  <p>Entire groups of logical functions can be enabled and disabled
 *  by their groups.  Multiple groups can be active at any given time
 *  and only active groups are elegible to receive input.</p>
 *
 *  @author    Paul Speed
 */
public class InputMapper {

    static Logger log = LoggerFactory.getLogger(InputMapper.class);

    private InputManager inputManager;
    private InputProvider inputProvider;
    private InputObserver listener;

    private Set<InputProvider> alternateProviders = new HashSet<>();

    private List<InputConfigListener> configListeners = new CopyOnWriteArrayList<>();

    private Set<String> activeGroups = new HashSet<String>();

    private Map<Object,StateGroupIndex> stateIndex = new HashMap<>();
    private Set<StateGroup> activeStates = new HashSet<StateGroup>();

    private Map<Joystick,InputDevice> joystickMap = new HashMap<>();
    private Map<JoystickAxis,DeviceAxis> joystickAxisMap = new HashMap<>();
    private Map<JoystickButton,DeviceButton> joystickButtonMap = new HashMap<>();

    private Map<FunctionId, FunctionListeners> listenerMap = new HashMap<>();

    private double tpf = 0;
    private long lastFrameNanos;

    public InputMapper( InputManager inputManager ) {
        // The getJoysticks() call is why we can't completely cut over to
        // InputProvider universally and must still maintain a hook to InputManager.
        // TODO: come up with some kind of joystick registry interface that can be
        // passed to providers to more robustly map joysticks.
        // It also didn't feel right to lump the getAxisDeadZone() into the
        // InputProvider interface.
        // So for the moment, I'm keeping both the InputManager reference and creating
        // an InputProvider wrapper to do most of the interactions.  This leaves more
        // future flexibility in how to solve these problems without dropping more concrete
        // things into an interface that maybe shouldn't have them.
        this.inputManager = inputManager;
        this.inputProvider = new JmeInputProviderAdapter(inputManager);
        this.listener = new InputObserver();

        inputProvider.addRawInputListener(listener);

        Joystick[] sticks = inputManager.getJoysticks();
        if( sticks != null ) {
            for( Joystick j : sticks ) {
                mapJoystick(j);
            }
        }

        // The default group always starts active.
        activeGroups.add(FunctionId.DEFAULT_GROUP);

        lastFrameNanos = System.nanoTime();
    }

    /**
     *  Registers an alternate InputProvider that can be used to supply events
     *  to this InputMapper and call its addRawInputListener() method.  If the provider is
     *  still registered when InputMapper is released then InputMapper will remove its
     *  listener during release().
     */
    public void registerProvider( InputProvider provider ) {
        if( alternateProviders.add(provider) ) {
            provider.addRawInputListener(listener);
        }
    }

    /**
     *  Removes a previously registered alternate InputProvider and calls its
     *  removeRawInputListener() method.
     */
    public void unregisterProvider( InputProvider provider ) {
        if( alternateProviders.remove(provider) ) {
            provider.removeRawInputListener(listener);
        }
    }

    /**
     *  Activates the specified function group, allowing those function IDs to receive input.
     */
    public void activateGroup( String group ) {
        if( log.isTraceEnabled() )
            log.trace("activate:" + group);
        activeGroups.add(group);
    }

    /**
     *  Deactivates the specified function group, preventing those function IDs from receiving input.
     */
    public void deactivateGroup( String group ) {
        if( log.isTraceEnabled() )
            log.trace("deactivate:" + group);
        activeGroups.remove(group);
    }

    public void release() {
        inputProvider.removeRawInputListener(listener);
        for( InputProvider p : alternateProviders ) {
            p.removeRawInputListener(listener);
        }
    }

    protected void mapJoystick( Joystick j ) {

        InputDevice device = InputDevice.joystick(joystickMap.size() + 1);
        joystickMap.put(j, device);
        log.info("Registered:" + j + " as:" + device);

        // We attempt to determine what kind of stick it is so
        // that we can provide more intelligent button and axis mappings
        if( j.getAxis(JoystickAxis.Z_ROTATION) != null
            || j.getAxis(JoystickAxis.Z_AXIS) != null ) {

            // Some XBOX controllers seem not to have a Z_ROTATION in this context
            // but are game pads.  All gampads seem to have a Z_AXIS.  At any rate,
            // the gamepad mapping is the more thorough mapping and so it's probably
            // better to default there if we aren't sure.

            mapGamepad(device, j);
            return;
        }

        log.info("map basic joystick:" + j);

        // Else it's a generic one
        joystickAxisMap.put(j.getXAxis(), device.axis(Axis.JOYSTICK_X));
        joystickAxisMap.put(j.getYAxis(), device.axis(Axis.JOYSTICK_Y));
        joystickAxisMap.put(j.getPovXAxis(), device.axis(Axis.JOYSTICK_HAT_X));
        joystickAxisMap.put(j.getPovYAxis(), device.axis(Axis.JOYSTICK_HAT_Y));

        for( JoystickButton b : j.getButtons() ) {
            String id = b.getLogicalId();
            if( !Character.isDigit(id.charAt(0)) )
                continue;

            int idVal = Integer.parseInt(id) + 1;
            Button button = new Button("joystick_" + idVal, "Button " + idVal);
            joystickButtonMap.put(b, device.button(button));
        }
    }

    protected void mapGamepad( InputDevice device, Joystick j ) {
        log.info("mapGamepad(" + j + ")");

        // Map as many standard things as we can find and then
        // map anything left as best as we can.
        joystickAxisMap.put(j.getXAxis(), device.axis(Axis.JOYSTICK_LEFT_X));
        joystickAxisMap.put(j.getYAxis(), device.axis(Axis.JOYSTICK_LEFT_Y));
        joystickAxisMap.put(j.getAxis(JoystickAxis.Z_AXIS), device.axis(Axis.JOYSTICK_RIGHT_X));
        joystickAxisMap.put(j.getAxis(JoystickAxis.Z_ROTATION), device.axis(Axis.JOYSTICK_RIGHT_Y));

        // Replace these
        joystickAxisMap.put(j.getAxis("rx"), device.axis(Axis.JOYSTICK_LEFT_TRIGGER));
        joystickAxisMap.put(j.getAxis("ry"), device.axis(Axis.JOYSTICK_RIGHT_TRIGGER));
        // with these:
        //joystickAxisMap.put(j.getAxis(JoystickAxis.LEFT_TRIGGER), Axis.JOYSTICK_LEFT_TRIGGER);
        //joystickAxisMap.put(j.getAxis(JoystickAxis.RIGHT_TRIGGER), Axis.JOYSTICK_RIGHT_TRIGGER);
        // ...when JME 3.3 is released.

        joystickAxisMap.put(j.getPovXAxis(), device.axis(Axis.JOYSTICK_HAT_X));
        joystickAxisMap.put(j.getPovYAxis(), device.axis(Axis.JOYSTICK_HAT_Y));

        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_0), device.button(Button.JOYSTICK_BUTTON1));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_1), device.button(Button.JOYSTICK_BUTTON2));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_2), device.button(Button.JOYSTICK_BUTTON3));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_3), device.button(Button.JOYSTICK_BUTTON4));

        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_4), device.button(Button.JOYSTICK_LEFT1));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_5), device.button(Button.JOYSTICK_RIGHT1));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_6), device.button(Button.JOYSTICK_LEFT2));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_7), device.button(Button.JOYSTICK_RIGHT2));

        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_8), device.button(Button.JOYSTICK_SELECT));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_9), device.button(Button.JOYSTICK_START));

        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_10), device.button(Button.JOYSTICK_LEFT3));
        joystickButtonMap.put(j.getButton(JoystickButton.BUTTON_11), device.button(Button.JOYSTICK_RIGHT3));

        // Map any buttons that are left-over
        for( JoystickButton b : j.getButtons() ) {
            if( joystickButtonMap.containsKey(b) ) {
                continue;
            }

            // Come up with a default name that matches our
            // conventions.
            String id = b.getLogicalId();
            String name = b.getName();
            if( Character.isDigit(id.charAt(0)) ) {
                int idVal = Integer.parseInt(id) + 1;
                id = String.valueOf(idVal);
                name = "Button " + id;
            }
            String targetId = "joystick_" + id;

            log.warn("No mapping for button:" + b + "  Defaulting to:" + targetId + " name:" + name);
            joystickButtonMap.put(b, device.button(new Button(targetId, name)));
        }

        // Map any axes that are left over
        for( JoystickAxis a : j.getAxes() ) {
            if( joystickAxisMap.containsKey(a) ) {
                continue;
            }
            String targetId = "joystick_" + a.getLogicalId();
            String name = "Joystick " + a.getName();
            log.warn("no mapping for axis:" + a + "  Defaulting to:" + targetId + " name:" + name);
            joystickAxisMap.put(a, device.axis(new Axis(targetId, name)));
        }
    }

    protected StateGroupIndex getIndex( Object state, boolean create ) {
        StateGroupIndex result = stateIndex.get(state);
        if( result == null && create ) {
            result = new StateGroupIndex(state);
            stateIndex.put(state, result);
        }
        return result;
    }

    protected FunctionListeners getFunctionListeners( FunctionId f, boolean create ) {
        FunctionListeners result = listenerMap.get(f);
        if( result == null && create ) {
            result = new FunctionListeners();
            listenerMap.put(f, result);
        }
        return result;
    }

    public Mapping map( FunctionId function, Axis axis, Object... pressed ) {
        return addMapping(function, 1.0, axis, pressed);
    }

    public Mapping map( FunctionId function, InputState bias, Axis axis, Object... pressed ) {
        return addMapping(function, bias.asNumber(), axis, pressed);
    }

    public Mapping map( FunctionId function, Button button, Object... pressed ) {
        return addMapping(function, 1.0, button, pressed);
    }

    public Mapping map( FunctionId function, InputState bias, Button button, Object... pressed ) {
        return addMapping(function, bias.asNumber(), button, pressed);
    }

    public Mapping map( FunctionId function, int mainKeyCode, Object... pressed ) {
        return map(function, InputState.Positive, mainKeyCode, pressed);
    }

    public Mapping map( FunctionId function, InputState bias, int mainKeyCode, Object... pressed ) {
        return addMapping(function, bias.asNumber(), mainKeyCode, pressed);
    }

    protected Mapping addMapping( FunctionId function, double scale, Object primary,
                                  Object... modifiers ) {

        StateGroup g = new StateGroup(function, scale, primary, modifiers);
        getIndex(primary, true).addGroup(g);
        for( Object o : modifiers ) {
            getIndex(o, true).addGroup(g);
        }
        fireMappingAdded(g);
        return g;
    }

    protected StateGroup findMapping( FunctionId function, Object primary,
                                      Object... modifiers ) {
        StateGroupIndex index = getIndex(primary, false);
        if( index == null )
            return null;
        return index.findGroup(function, modifiers);
    }

    public Mapping getMapping( FunctionId function, Object primary,
                               Object... modifiers ) {
        return findMapping(function, primary, modifiers);
    }

    public void removeMapping( FunctionId function, Object primary,
                               Object... modifiers ) {
        removeMapping(findMapping(function, primary, modifiers));
    }

    public void removeMapping( Mapping mapping ) {
        StateGroup group = (StateGroup)mapping;
        boolean removed = false;
        for( StateGroupIndex index : stateIndex.values() ) {
            if( index.removeGroup(group) ) {
                removed = true;
            }
        }
        if( removed ) {
            fireMappingRemoved(mapping);
        }
    }

    /**
     *  Returns true if the specified function has any input mappings.
     */
    public boolean hasMappings( FunctionId function ) {
        // No quick way to do this... must be thorough
        for( StateGroupIndex index : stateIndex.values() ) {
            for( StateGroup group : index.groups ) {
                if( Objects.equals(function, group.function) ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *  Returns a list of all FunctionIds that have listeners registered or
     *  inputs mapped.
     */
    public Set<FunctionId> getFunctionIds() {
        Set<FunctionId> results = new HashSet<>();
        results.addAll(listenerMap.keySet());
        for( StateGroupIndex index : stateIndex.values() ) {
            for( StateGroup group : index.groups ) {
                results.add(group.function);
            }
        }
        return results;
    }

    /**
     *  Returns all of the different input mappings for a particular function ID.
     */
    public Set<Mapping> getMappings( FunctionId function ) {
        Set<Mapping> results = new LinkedHashSet<>();
        for( StateGroupIndex index : stateIndex.values() ) {
            for( StateGroup group : index.groups ) {
                if( Objects.equals(function, group.function) ) {
                    results.add(group);
                }
            }
        }
        return results;
    }

    public void addStateListener( StateFunctionListener l, FunctionId... functions ) {
        if( functions == null || functions.length == 0 ) {
            throw new RuntimeException( "No function IDs specified." );
        }
        if( l == null ) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        for( FunctionId function : functions ) {
            FunctionListeners listeners = getFunctionListeners(function, true);
            listeners.stateListeners.add(l);
        }
    }

    public void removeStateListener( StateFunctionListener l, FunctionId... functions ) {
        if( functions == null || functions.length == 0 ) {
            throw new RuntimeException( "No function IDs specified." );
        }
        if( l == null ) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        for( FunctionId function : functions ) {
            FunctionListeners listeners = getFunctionListeners(function, false);
            if( listeners == null )
                continue;
            listeners.stateListeners.remove(l);
        }
    }

    public void addAnalogListener( AnalogFunctionListener l, FunctionId... functions ) {
        if( functions == null || functions.length == 0 ) {
            throw new RuntimeException( "No function IDs specified." );
        }
        if( l == null ) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        for( FunctionId function : functions ) {
            FunctionListeners listeners = getFunctionListeners(function, true);
            listeners.analogListeners.add(l);
        }
    }

    public void removeAnalogListener( AnalogFunctionListener l, FunctionId... functions ) {
        if( functions == null || functions.length == 0 ) {
            throw new RuntimeException( "No function IDs specified." );
        }
        if( l == null ) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        for( FunctionId function : functions ) {
            FunctionListeners listeners = getFunctionListeners(function, false);
            if( listeners == null )
                continue;
            listeners.analogListeners.remove(l);
        }
    }

    public void addDelegate( FunctionId func, Object target, String methodName ) {
        addDelegate(func, target, methodName, false);
    }

    public void addDelegate( FunctionId func, Object target, String methodName,
                             boolean passArgument ) {

        addStateListener(new StateMethodDelegate(target, methodName, passArgument), func);
    }

    public void removeDelegate( FunctionId func, Object target, String methodName ) {
        FunctionListeners listeners = getFunctionListeners(func, false);
        if( listeners == null )
            return; // nothing to remove

        for( StateFunctionListener l : listeners.stateListeners ) {
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

    protected void activate( StateGroup g ) {
        if( !activeStates.add(g) )
            return;

        if( log.isTraceEnabled() )
            log.trace("activate(" + g.function + ":" + g.scale + ")");

        // So the activation state changed and now we
        // should notify those listeners... actually... that should
        // be done in the update loop since even when a group is
        // activated it's state value can change (from positive to negative
        // for example).
    }

    protected void deactivate( StateGroup g ) {
        if( !activeStates.remove(g) )
            return;

        if( log.isTraceEnabled() )
            log.trace("deactivate(" + g.function + ":" + g.scale + ")");

        // Need to make sure that the group is set back to
        // ground-state so it will show up right when activated again
        notifyValueActive(g.getFunction(), 0);
        g.resetValue();
    }

    public void update() {
        for( StateGroup g : activeStates ) {
            double value = getIndex(g.getPrimary(), false).getValue();
            g.updateValue(value);
            notifyValueActive(g.getFunction(), g.getValue());
        }
    }

    protected void notifyStateChanged( FunctionId function, InputState value ) {
        FunctionListeners listeners = getFunctionListeners(function, false);
        if( listeners == null )
            return;

        listeners.notifyStateChanged( function, value );
    }

    protected void notifyValueActive( FunctionId function, double value ) {
        FunctionListeners listeners = getFunctionListeners(function, false);
        if( listeners == null )
            return;

        listeners.notifyAnalogUpdate(function, value);
    }

    protected InputState valueToState( double val ) {
        if( val < -0.01 )
            return InputState.Negative;
        else if( val > 0.01 )
            return InputState.Positive;
        else
            return InputState.Off;
    }

    /**
     *  Adds a listener that will be notified when input mappings are
     *  added, changed, or removed.
     */
    public void addInputConfigListener( InputConfigListener l ) {
        configListeners.add(l);
    }

    /**
     *  Removes a previously registered input config listener.
     */
    public void removeInputConfigListener( InputConfigListener l ) {
        configListeners.remove(l);
    }

    protected void fireMappingAdded( Mapping m ) {
        for( InputConfigListener l : configListeners ) {
            l.mappingAdded(m);
        }
    }

    protected void fireMappingRemoved( Mapping m ) {
        for( InputConfigListener l : configListeners ) {
            l.mappingRemoved(m);
        }
    }

    protected void fireMappingChanged( Mapping m ) {
        for( InputConfigListener l : configListeners ) {
            l.mappingChanged(m);
        }
    }

    protected class FunctionListeners {

        SafeArrayList<StateFunctionListener> stateListeners
                = new SafeArrayList<StateFunctionListener>(StateFunctionListener.class);
        SafeArrayList<AnalogFunctionListener> analogListeners
                = new SafeArrayList<AnalogFunctionListener>(AnalogFunctionListener.class);

        public void notifyStateChanged( FunctionId function, InputState value ) {
            for( StateFunctionListener l : stateListeners.getArray() ) {
                l.valueChanged(function, value, tpf);
            }
        }

        public void notifyAnalogUpdate( FunctionId function, double value ) {
            for( AnalogFunctionListener l : analogListeners.getArray() ) {
                l.valueActive(function, value, tpf);
            }
        }
    }

    /**
     *  Represents a specific control mapping such that it can be reconfigured.
     */
    public interface Mapping {
        public FunctionId getFunction();
        public void setScale( double scale );
        public double getScale();

        /**
         *  Returns the primary inputs that activates this mapping.
         */
        public Object getPrimaryActivator();

        /**
         *  Returns the additional modifier inputs that must be present for
         *  this mapping to be activated.
         */
        public List<Object> getModifiers();

        // Note to self, this probably needs to be a wrapper class so
        // that a) we don't potentially expose StateGroup methods directly
        // and b) we could support runtime remapping like for a UI configuration
        // screen.
    }

    protected class StateGroup implements Comparable<StateGroup>, Mapping {

        Object primaryState;
        Object[] modifiers;
        FunctionId function;
        double scale;
        double lastValue;
        InputState lastState;

        public StateGroup( FunctionId function, double scale, Object primaryState,
                           Object... modifiers ) {

            this.function = function;
            this.scale = scale;
            this.primaryState = primaryState;
            this.modifiers = modifiers;
            resetValue();
        }

        public Object getPrimaryActivator() {
            return primaryState;
        }

        public List<Object> getModifiers() {
            return Collections.unmodifiableList(Arrays.asList(modifiers));
        }

        public void setScale( double scale ) {
            if( scale == 0 ) {
                throw new IllegalArgumentException("Scale cannot be 0.");
            }
            this.scale = scale;
            fireMappingChanged(this);
        }

        public double getScale() {
            return scale;
        }

        public boolean hasSameModifiers( Object[] mods ) {
            if( mods.length != modifiers.length ) {
                return false;
            }

            for( int i = 0; i < modifiers.length; i++ ) {
                if( !Objects.equals(mods[i], modifiers[i]) )
                    return false;
            }

            return true;
        }

        public int compareTo( StateGroup other ) {
            // We want to sort the conditions so that the most
            // specific ones are first.  So if other is more specific
            // than us then we return -1 if it's less specific we return 1

            // We base this completely on size right now
            int c = modifiers == null ? 0 : modifiers.length;
            int oc = other.modifiers == null ? 0 : other.modifiers.length;

            return oc - c;
        }

        public FunctionId getFunction() {
            return function;
        }

        public double getValue() {
            return lastValue;
        }

        public void updateValue( double value ) {
            double adjusted = value * scale;
            if( lastValue == adjusted )
                return;
            lastValue = adjusted;

            if( log.isTraceEnabled() )
                log.trace( "Value changed for:" + function + " scale:" + scale );

            InputState state = valueToState(adjusted);
            updateState(state);
        }

        public void updateState( InputState state ) {
            if( lastState == state )
                return;
            lastState = state;

            // Except maybe function state should be kept centrally by function
            // We could have muliple state groups firing against the same
            // function ID.  Like the user pressing forward and back at the same
            // time.  Hmmm... per group state might be better in that case anyway
            // since then we don't send double state every frame.  We'll send state
            // when forward changes and when back changes and that's it.
            notifyStateChanged(function, lastState);
        }

        public void resetValue() {
            if( valueToState(lastValue) == InputState.Off )
                return;

            lastValue = 0;
            lastState = InputState.Off;

            // And we need to notify state listeners
            notifyStateChanged(function, lastState);
        }

        public Object getPrimary() {
            return primaryState;
        }

        public boolean isPrimary( Object state ) {
            if( state == primaryState )
                return true;
            return Objects.equals(state, primaryState);
        }

        public boolean isTrue() {
            if( !activeGroups.contains(function.getGroup()) )
                return false;
            if( !getIndex(primaryState, false).isOn() )
                return false;
            for( Object o : modifiers ) {
                if( !getIndex(o, false).isOn() ) {
                    return false;
                }
            }
            return true;
        }

        public boolean areModifiersTrue() {
            if( !activeGroups.contains(function.getGroup()) )
                return false;
            for( Object o : modifiers ) {
                if( !getIndex(o, false).isOn() ) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "StateGroup[" + function + ":" + scale + ", " + primaryState + "]";
        }
    }

    protected class StateGroupIndex {

        Object localState;
        List<StateGroup> groups = new ArrayList<StateGroup>();
        double lastValue;

        public StateGroupIndex( Object localState ) {
            this.localState = localState;
        }

        public boolean isOn() {
            return valueToState(lastValue) != InputState.Off;
        }

        public double getValue() {
            return lastValue;
        }

        public StateGroup findGroup( FunctionId function, Object[] modifiers ) {
            for( StateGroup g : groups ) {
                if( !g.getFunction().equals(function) ) {
                    continue;
                }
                if( modifiers.length == 0 || g.hasSameModifiers(modifiers) ) {
                    return g;
                }
            }
            return null;
        }

        public StateGroup addGroup( StateGroup g ) {
            for( int i = 0; i < groups.size(); i++ ) {
                StateGroup existing = groups.get(i);
                if( existing.compareTo(g) > 0 ) {
                    // the current entry is less specific than the supplied one
                    groups.add( i, g );
                    return g;
                }
            }

            // Else just add it
            groups.add( g );
            return g;
        }

        public boolean removeGroup( StateGroup g ) {
            if( groups.remove(g) ) {
                refresh();
                return true;
            }
            return false;
        }

        public void refresh() {
            // Only need to activate the first primary... and
            // then only if the state would not be "Off"
            boolean activatePrimary = isOn();

            for( StateGroup g : groups ) {
                if( !g.isTrue() ) {
                    // Deactivate it
                    deactivate(g);

                    // If this wasn't its primary state then we need
                    // to refresh it's primary index since another state
                    // may now get to run
                    if( !g.isPrimary(localState) ) {
                        Object primary = g.getPrimary();
                        getIndex(primary, false).refresh();
                    }
                } else if( g.isPrimary(localState) ) {
                    if( activatePrimary ) {
                        // Group needs to be activated if it wasn't
                        activate(g);
                        activatePrimary = false;
                    } else {
                        // Group needs to be deactivated if it was active
                        // since now there is a better one.
                        deactivate(g);
                    }
                } else {
                    // the group is active but this is not its
                    // primary state.  So we need to have the index
                    // for the primary state refresh itself
                    Object primary = g.getPrimary();
                    getIndex(primary, false).refresh();
                }
            }
        }

        public void updateValue( double val ) {
            if( lastValue == val )
                return;
            lastValue = val;
            refresh();
        }

        public void instantUpdate( double val ) {
            // Find the first primary true group and
            // send the value to its listeners
            for( StateGroup g : groups ) {
                if( !g.areModifiersTrue() )
                    continue;

                if( !g.isPrimary(localState) )
                    continue;

                // Notify and break
                // Technically we should send analog and a state
                // on, state off... I let the state off go because
                // I can't think of a use-case for it at the moment.
                val = val * g.getScale();
                InputState state = valueToState(val);

                notifyStateChanged(g.getFunction(), state);
                notifyValueActive(g.getFunction(), val);
                break;  // first one found wins
            }
        }

        @Override
        public String toString() {
            return "StateGroupIndex[" + localState + "]";
        }
    }



    protected class InputObserver implements RawInputListener {

        public void onJoyAxisEvent( JoyAxisEvent evt ) {
            if( log.isTraceEnabled() ) {
                log.trace("onJoyAxisEvent(axis:" + evt.getAxis()
                                        + ", val:" + evt.getValue() + ")");
            }

            JoystickAxis a = evt.getAxis();
            Joystick j = a.getJoystick();
            float val = evt.getValue();

            // Below a certain threshold, call the value 0
            float aVal = Math.abs(val);
            if( aVal <= 0.01 || aVal <= Math.max(a.getDeadZone(), inputManager.getAxisDeadZone()) ) {
                val = 0;
            }

            // Note: because we send the 0 values even if the value
            // for this axis hasn't actually changed, it means that when
            // two joysticks are mapped to the same function, 'dead zone'
            // values from one stick will reset the values from the other
            // stick.  Proper dead zone management means only sending zeroes
            // through if the previous value was not zero.  Bug fix for
            // another day.

            DeviceAxis axis = joystickAxisMap.get(a);
            if( axis == null ) {
                log.warn("No axis mapping for:" + a );
                return;
            }

            // We can't tell from here which state groups are active
            // or inactive with respect to FunctionId active groups.
            // ie: we don't know if the StateGroupIndex 'accepts' our
            // change... so we'll send the change to the most specific
            // and the more general if they both have StateGroupIndex
            // objects.
            StateGroupIndex index = getIndex(axis, false);
            if( index != null ) {
                index.updateValue(val);
            }
            // Try the more generic one
            index = getIndex(axis.getAxis(), false);
            if( index != null ) {
                index.updateValue(val);
            }
        }

        public void onJoyButtonEvent( JoyButtonEvent evt ) {
            if( log.isTraceEnabled() ) {
                log.trace("onJoyButtonEvent(button:" + evt.getButton() + ", pressed:" + evt.isPressed() + ")");
            }

            DeviceButton b = joystickButtonMap.get(evt.getButton());
            if( b == null ) {
                log.warn("No button mapping for:" + evt.getButton() );
                return;
            }
            if( log.isTraceEnabled() ) {
                log.trace("Forwarding events to button mapping:" + b);
            }

            // We can't tell from here which state groups are active
            // or inactive with respect to FunctionId active groups.
            // ie: we don't know if the StateGroupIndex 'accepts' our
            // change... so we'll send the change to the most specific
            // and the more general if they both have StateGroupIndex
            // objects.
            double value = evt.isPressed() ? 1.0 : 0.0;
            StateGroupIndex index = getIndex(b, false);
            if( index != null ) {
                index.updateValue(value);
            }
            // Try the more generic version
            index = getIndex(b.getButton(), false);
            if( index != null ) {
                index.updateValue(value);
            }
        }

        public void beginInput() {
            long time = System.nanoTime();
            tpf = (time - lastFrameNanos) / 1000000000.0;
            lastFrameNanos = time;
        }

        public void endInput() {
            update();
        }

        protected void instantUpdate( Axis a, double value ) {
            StateGroupIndex index = getIndex(a, false);
            if( index == null )
                return;

            index.instantUpdate(value);
        }

        public void onMouseMotionEvent( MouseMotionEvent evt ) {
            if( log.isTraceEnabled() ) {
                log.trace("onMouseMotionEvent(" + evt + ")");
            }

            // All axes could be different so we can't really
            // consolidate.

            // While the numbers in these divisions are a bit magic,
            // they are to bring the values into the normal analog
            // range... roughly.
            // In a sense, these values already have tpf mixed in
            // because the deltas will be higher when frames are longer
            // because we are capturing less often.  So that the user
            // can multiply by tpf, we will divide it out again.
            if( evt.getDeltaWheel() != 0 ) {
                //instantUpdate( Axis.MOUSE_WHEEL, evt.getDeltaWheel() / (1024.0 * tpf) );
                // The mouse wheel is kind of a special case because the
                // spinning tends to have hard-stops that make the progressions
                // in even increments.  So it doesn't act analog.  We'll just
                // hardcode a divisor to get it typically in the 1.0 range (based
                // on experimentation)
                instantUpdate(Axis.MOUSE_WHEEL, evt.getDeltaWheel() / 120.0);
            }
            if( evt.getDX() != 0 ) {
                instantUpdate(Axis.MOUSE_X, evt.getDX() / (1024.0 * tpf));
            }
            if( evt.getDY() != 0 ) {
                instantUpdate(Axis.MOUSE_Y, evt.getDY() / (1024.0 * tpf));
            }
        }

        public void onMouseButtonEvent( MouseButtonEvent evt ) {
            if( log.isTraceEnabled() ) {
                log.trace("onMouseButtonEvent(" + evt + ")");
            }

            Button b;
            switch( evt.getButtonIndex() ) {
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
                    b = new Button("mouse_" + i, "Mouse Button " + i);
                    break;
            }

            StateGroupIndex index = getIndex(b, false);
            if( index == null )
                return;
            double value = evt.isPressed() ? 1.0 : 0.0;

            index.updateValue(value);
        }

        public void onKeyEvent( KeyInputEvent evt ) {
            if( log.isTraceEnabled() ) {
                log.trace("onKeyEvent(" + evt + ")");
            }
            if( evt.isRepeating() )
                return;

            StateGroupIndex index = getIndex(evt.getKeyCode(), false);
            if( index == null )
                return;
            double value = evt.isPressed() ? 1.0 : 0.0;
            index.updateValue(value);
        }

        public void onTouchEvent( TouchEvent evt ) {
        }
    }
}

