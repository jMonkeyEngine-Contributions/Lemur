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
package com.simsilica.lemur;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A <code>CheckboxGroup</code> is a class in charge of grouping {@link com.simsilica.lemur.Checkbox},
 * in a group only one {@link com.simsilica.lemur.Checkbox} can be selected.
 * <p>
 * This class is similar to <code>javax.swing.ButtonGroup</code> from the Java-Swing API
 * 
 * @author wil
 */
public class CheckboxGroup {
    
    /**
     * The list of checkboxes participating in this group.
     */
    protected List<Checkbox> checkboxs = new ArrayList<>();
    
    /**
     * The current selection.
     */
    protected Checkbox selection;

    /**
     * Creates a new <code>CheckboxGroup</code>.
     */
    public CheckboxGroup() {
    }
    
    /**
     * Add the checkbox to the group.
     * @param c the checkbox to be added
     * @return boolean
     */
    public boolean add(Checkbox c) {
        if (c == null) {
            return false;
        }
        
        boolean bool = checkboxs.add(c);
        if (c.isChecked()) {
            if (selection == null) {
                selection = c;
            } else {
                c.getModel().setChecked(false);
            }
        }
        
        c.getModel().setGroup(this);
        return bool;
    }
    
    /**
     * Remove the group checkbox.
     * @param c the checkbox to be removed
     * @return boolean
     */
    public boolean remove(Checkbox c) {
        if (c == null) {
            return false;
        }
        boolean bool = checkboxs.remove(c);
        if (c == selection) {
            selection = null;
        }
        c.getModel().setGroup(null);
        return bool;
    }
    
    /**
     * Clears the selection so that none of the checkboxes in <code>CheckboxGroup</code>
     * are selected.
     */
    public void clearSelection() {
        if (selection != null) {
            Checkbox oldSelection = selection;
            selection = null;
            oldSelection.getModel().setChecked(false);
        }
    }

    /**
     * Returns the selected checkbox.
     * @return Checkbox
     */
    public Checkbox getSelection() {
        return selection;
    }
    
    /**
     * Sets the selected value for the <code>Checkbox</code>. Only one checkbox
     * in the group may be selected at a time.
     * 
     * @param c the <code>Checkbox</code>
     * @param b <code>true</code> if this checkbox is to be selected, otherwise 
     * <code>false</code>
     */
    public void setSelected(Checkbox c, boolean b) {
        if (b && c != null && c != selection) {
            Checkbox oldSelection = selection;
            selection = c;
            if (oldSelection != null) {
                oldSelection.getModel().setChecked(false);
            }
            c.getModel().setChecked(true);
        }
    }
    
    /**
     * Check if <code>Checkbox</code> is selected.
     * @param c the <code>Checkbox</code>
     * @return boolean
     */
    public boolean isSelected(Checkbox c) {
        return (c == selection);
    }
    
    /**
     * Returns the number of checkboxes.
     * @return number
     */
    public int getCheckboxCount() {
        if (checkboxs == null || checkboxs.isEmpty()) {
            return 0;
        } else {
            return checkboxs.size();
        }
    }
    
    /**
     * Returns a <code>Iterator</code> of the elements that belong to this group.
     * @return Iterator
     */
    public Iterator<Checkbox> getElements() {
        return checkboxs.iterator();
    }
}
