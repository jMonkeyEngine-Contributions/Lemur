/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
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

package com.simsilica.lemur;

import com.google.common.base.Objects;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  A combination of a command and some attributes
 *  that define what the GUI element should look like.
 *  The action is versioned so that GUI elements can
 *  automatically update themselves as these attributes
 *  are changed.  This is similar to Swing's Action.
 *
 *  @author    Paul Speed
 */
public abstract class Action implements VersionedObject<Action>, Command<Button> {

    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_NAME = "name";
    public static final String KEY_ICON = "icon";
    public static final String KEY_LARGE_ICON = "largeIcon";
    public static final String KEY_SHORT_DESCRIPTION = "shortDescription";
    public static final String KEY_SELECTED = "selected";

    private long version;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private boolean enabled = true;
    
    public Action() {
    }

    public Action( String name ) {
        setName(name);
    }
    
    public Action( String name, GuiComponent icon ) {
        setName(name);
        setIcon(icon);
    }

    public Action( String name, GuiComponent icon, boolean enabled ) {
        setName(name);
        setIcon(icon);
        setEnabled(enabled);
    }

    /**
     *  Subclasses must override this to perform the action.
     */
    @Override
    public abstract void execute( Button source );
 
    /**
     *  Sets the enabled/disabled state of this action.  Action 
     *  GUI elements that support being disabled will adhere to
     *  this state.
     */
    public void setEnabled( boolean b ) {
        putValue(KEY_ENABLED, b);
    }
    
    public boolean isEnabled() {
        return getValue(KEY_ENABLED, true);
    }

    /**
     *  Sets the selected/deselected state of this action.
     *  Action GUI elements that support selection (checkboxes)
     *  will adhere to this state and reflect it back into the
     *  action itself.
     */
    public void setSelected( boolean b ) {
        putValue(KEY_SELECTED, b);
    }
    
    public boolean isSelected() {
        return getValue(KEY_SELECTED, true);
    }
 
    /**
     *  Sets the name of the action that will be used as the
     *  label in wrapping Action GUI elements.
     */
    public void setName( String name ) {
        putValue(KEY_NAME, name);
    }
    
    public String getName() {
        return getValue(KEY_NAME);
    }
    
    /**
     *  Sets the icon that will be used as the icon
     *  in wrapping Action GUI elements.  This is used when
     *  a smaller icon is needed or when there is no large
     *  icon specified.
     */
    public void setIcon( GuiComponent component ) {
        putValue(KEY_ICON, component);
    }
    
    public GuiComponent getIcon() {
        return getValue(KEY_ICON);
    }

    /**
     *  Sets the icon that will be used as the icon
     *  in wrapping Action GUI elements.  ActionButton will
     *  use this as its icon if specified.  getLargeIcon()
     *  defaults to getIcon() when not set.  
     */
    public void setLargeIcon( GuiComponent component ) {
        putValue(KEY_LARGE_ICON, component);
    }
    
    public GuiComponent getLargeIcon() {
        return getValue(KEY_LARGE_ICON, getIcon());
    }
 
    /**
     *  Sets a general value onto this action that other
     *  action users or custom GUI elements can reference.
     */
    public void putValue( String key, Object value ) {
        Object existing = properties.put(key, value);
        if( !Objects.equal(existing, value) ) {
            incrementVersion();
        }        
    }
 
    /**
     *  Returns a previously set value from this action or
     *  null if the property has not been set.
     */
    @SuppressWarnings("unchecked") 
    public <T> T getValue( String key ) {
        return (T)properties.get(key);
    }

    /**
     *  Returns a previously set value from this action or
     *  defaultValue if the property has not been set.
     */
    @SuppressWarnings("unchecked") 
    public <T> T getValue( String key, T defaultValue ) {
        Object result = properties.get(key);
        return result == null ? defaultValue : (T)result;
    }
 
    protected void incrementVersion() {
        version++;
    }
    
    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Action getObject() {
        return this;
    }

    @Override
    public VersionedReference<Action> createReference() {
        return new VersionedReference<Action>(this);
    }
 
    protected void appendFields( StringBuilder sb ) {
        if( sb.length() > 0 ) {
            sb.append(", ");
        }
        sb.append("properties=");        
        sb.append(properties);
    }
    
    @Override   
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendFields(sb);
        return getClass().getName() + "[" + sb + "]";
    }
}
