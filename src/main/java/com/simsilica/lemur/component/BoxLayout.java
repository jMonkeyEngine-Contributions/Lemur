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
 * SOFTWARE, Even IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.lemur.component;

import java.util.*;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.Axis;
import com.simsilica.lemur.FillMode;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;

/**
 *  A layout that manages children similar to Swing's BoxLayout.
 *  Components are arranged along an Axis and the children are
 *  stretched to fit the maximum component width.  This is similar
 *  to a single column or single row SpringGridLayout and in the
 *  future may be deprecated in lieu of that class.
 *
 *  @author    Paul Speed
 */
public class BoxLayout extends AbstractGuiComponent
                       implements GuiLayout, Cloneable {
    private GuiControl parent;
    private Axis axis;
    private FillMode fill;
    private List<Node> children = new ArrayList<Node>();
    private List<Vector3f> preferredSizes = new ArrayList<Vector3f>();
    private Vector3f lastPreferredSize;

    public BoxLayout() {
        this(Axis.Y, FillMode.Even);
    }

    public BoxLayout( Axis axis, FillMode fill ) {
        this.axis = axis;
        this.fill = fill;
    }

    @Override
    public BoxLayout clone() {
        BoxLayout result = (BoxLayout)super.clone();
        result.parent = null;
        result.children = new ArrayList<Node>();
        result.preferredSizes = new ArrayList<Vector3f>();
        result.lastPreferredSize = null;
        return result;
    }

    @Override
    protected void invalidate() {
        if( parent != null ) {
            parent.invalidate();
        }
    }

    public void calculatePreferredSize( Vector3f size ) {
        // Calculate the size we'd like to be to let
        // all of the children have space
        Vector3f pref = new Vector3f();
        preferredSizes.clear();
        for( Node n : children ) {
            Vector3f v = n.getControl(GuiControl.class).getPreferredSize();

            preferredSizes.add(v.clone());

            // We do a little trickery here by adding the
            // axis direction to the returned preferred size.
            // That way we can just "max" the whole thing.
            switch( axis ) {
                case X:
                    v.x += pref.x;
                    break;
                case Y:
                    v.y += pref.y;
                    break;
                case Z:
                    v.z += pref.z;
                    break;
            }

            pref.maxLocal(v);
        }
        lastPreferredSize = pref.clone();

        // The preferred size is the size... because layouts will always
        // be the decider in a component stack.  They are always first
        // in the component chain for preferred size and last for reshaping.
        size.set(pref);
    }

    public void reshape(Vector3f pos, Vector3f size) {
        // We are potentially asked to be a different size than we
        // prefer and we have to distribute the difference nicely.
        //Vector3f diff = size.subtract(lastPreferredSize);

        // Make sure there is a last preferred size to base
        // the reshaping on.
        calculatePreferredSize(new Vector3f());

        // Along the axis we will have to change each component
        // a little bit.  We give each one an even amount but we
        // also have the information to distribute it based on
        // their proportion.  We could even alter how we fill
        // to not increase their size at all.  Possible modes
        // for stretching:
        // None, Even, Proportional

        float axisPrefTotal = 0;
        float axisSizeTotal = 0;
        switch( axis ) {
            case X:
                axisPrefTotal = lastPreferredSize.x;
                axisSizeTotal = size.x;
                break;
            case Y:
                axisPrefTotal = lastPreferredSize.y;
                axisSizeTotal = size.y;
                break;
            case Z:
                axisPrefTotal = lastPreferredSize.z;
                axisSizeTotal = size.z;
                break;
        }


        Vector3f p = pos.clone();
        for( int i = 0; i < children.size(); i++ ) {
            Node n = children.get(i);
            Vector3f pref = preferredSizes.get(i).clone();

            // So the child size will depend on axis and fill
            float axisPref = 0;
            float axisSize = 0;
            switch( axis ) {
                case X:
                    axisPref = pref.x;
                    axisSize = size.x;

                    pref.y = size.y;
                    pref.z = size.z;
                    break;
                case Y:
                    axisPref = pref.y;
                    axisSize = size.y;

                    pref.x = size.x;
                    pref.z = size.z;
                    break;
                case Z:
                    axisPref = pref.z;
                    axisSize = size.z;

                    pref.x = size.x;
                    pref.y = size.y;
                    break;
            }

            switch( fill ) {
                case None:
                    axisSize = axisPref;
                    break;
                case Even:
                    // Even means that they all grow evenly... not
                    // that they are forced to be the same size.  So
                    // we take the total difference and divide it evenly
                    // among the children.
                    axisSize = axisPref + (axisSizeTotal - axisPrefTotal)/children.size();
                    break;
                case Proportional:
                    // All children expand proportional to their relation
                    // to the overall preferred size.  Bigger components get more
                    // share.
                    float relation = axisPref / axisPrefTotal;
                    axisSize = relation * axisSizeTotal;
                    break;
            }

            // Set the location while "pos" is correct.
            n.setLocalTranslation(p.clone());

            // Now set back the axis-specific size and adjust
            // position for the next component
            switch( axis ) {
                case X:
                    pref.x = axisSize;
                    p.x += axisSize;
                    break;
                case Y:
                    pref.y = axisSize;
                    p.y -= axisSize;
                    break;
                case Z:
                    pref.z = axisSize;
                    p.z += axisSize;
                    break;
            }

            // Now set the size of the child
            n.getControl(GuiControl.class).setSize(pref);
        }
    }

    public <T extends Node> T addChild( T n, Object... constraints ) {
        if( n.getControl( GuiControl.class ) == null )
            throw new IllegalArgumentException( "Child is not GUI element." );
        if( constraints != null && constraints.length > 0 )
            throw new IllegalArgumentException( "Box layout does not take constraints." );

        children.add(n);

        if( parent != null ) {
            // We are attached
            parent.getNode().attachChild(n);
        }

        invalidate();
        return n;
    }

    public void removeChild( Node n ) {
        if( !children.remove(n) )
            return; // we didn't have it as a child anyway
        if( parent != null ) {
            parent.getNode().detachChild(n);
        }
        invalidate();
    }

    @Override
    public void attach( GuiControl parent ) {
        this.parent = parent;
        Node self = parent.getNode();
        for( Node n : children ) {
            self.attachChild(n);
        }
    }

    @Override
    public void detach( GuiControl parent ) {
        this.parent = null;
        // Have to make a copy to avoid concurrent mod exceptions
        // now that the containers are smart enough to call remove
        // when detachChild() is called.  A small side-effect.
        // Possibly a better way to do this?  Disable loop-back removal
        // somehow?
        Collection<Node> copy = new ArrayList<Node>(children);    
        for( Node n : copy ) {
            n.removeFromParent();
        }
    }

    public Collection<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void clearChildren() {
        if( parent != null ) {
            // Have to make a copy to avoid concurrent mod exceptions
            // now that the containers are smart enough to call remove
            // when detachChild() is called.  A small side-effect.
            // Possibly a better way to do this?  Disable loop-back removal
            // somehow?
            Collection<Node> copy = new ArrayList<Node>(children);    
            for( Node n : copy ) {
                parent.getNode().detachChild(n);
            }
        }
        children.clear();
        invalidate();        
    }

}
