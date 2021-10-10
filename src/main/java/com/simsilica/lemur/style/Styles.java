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

package com.simsilica.lemur.style;

import java.lang.reflect.*;
import java.util.*;

import com.simsilica.lemur.core.GuiComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;

/**
 *  Provides support for automatically configuring GUI elements
 *  based on a style attribute system similar to cascading style
 *  sheets.
 *
 *  <p>The attributes that are available for a given GUI element
 *  are based on the StyleAttribute annotations specified on the
 *  class.  StyleAttribute setters expose style attributes that
 *  can be set in an Attributes set.</p>
 *
 *  <p>Attributes are accessed by selector that is a simplified
 *  version of the way cascading style sheets work.  A selector
 *  can either by the raw style name, a specific element ID
 *  for a style, or a parent/child relationship for a style.
 *  A particular element may inherit from one or more of these
 *  depending on how its element ID is setup.</p>
 *
 *  <p>In normal cascading style sheets, the parent/child relationship
 *  between elements is based on the full page hierarchy.  In Lemur's
 *  Style system, parent/child relationships are limited to the dotted
 *  notation with an element ID itself.  So an element ID of "button"
 *  has no parent/child relationship.  An element ID of "slider.thumb.button"
 *  has direct parent/child relationships between "slider"/"thumb" and
 *  "thumb"/"button", and an intrinsic parent/child relationship between
 *  "slider"/"button".</p>
 *
 *  <p>Attribute sets are built in such a way that they inherit attributes
 *  up this parent/child hierarchy.  Most specific styles override less
 *  specific ones.  The precendence goes roughly in the following order:</p>
 *  <ul>
 *  <li>Specific element ID, fully qualified</li>
 *  <li>For every dotted part, from right to left, all parent/child
 *      relationships and then the element ID for that dotted part.</li>
 *  <li>The global settings for the particular style.</li>
 *  </ul>
 *
 *  <p>For example, an ElementId of "A.B.C" would refer to an Attribute hierarchy
 *  from most specific to least specific, as:</p>
 *  <ul>
 *  <li>A.B.C</li>
 *  <li>B contains C</li>
 *  <li>A contains C</li>
 *  <li>C</li>
 *  <li>A contains B</li>
 *  <li>B</li>
 *  <li>style-defaults</li>
 *  </ul>
 *
 *  <p>This is "font" is accessed for "A.B.C" the configured Attribute sets
 *  are checked for a "font" attribute in the order listed above.  The first
 *  one found wins.</p>
 *
 *  <p>The values of the attributes are set based on a selector similar to
 *  the specific steps listed above.  For example, a style's attributes can
 *  be specifically set for "A.B.C" or can be set for any case of "A contains C".
 *  These attribute sets are accessed using the getSelector() methods and return
 *  a non-hierarchical, selector-specific attribute set.</p>
 *
 *  <p>To retrieve attributes for a GUI element, the getAttributes() methods
 *  are used. These return a full hierarchy of attribute sets as described above.
 *  </p>
 *
 *  <p>Using this system, an application could easily configure style-global
 *  attributes while also overriding the defaults for specific cases.  For example,
 *  if the application is defining a "beveled" style, it might set the default
 *  "font" for all style="beveled" elements.  It might then set the default background
 *  for all style="beveled", element="button" elements.  At that point, all labels,
 *  text fields, and text entry fields would be using the specified font.  All buttons,
 *  whether they are in sliders, scroll bars, or on their own, would have the specified
 *  background.  The application could then define settings for "any button in a
 *  slider" with getSelector( "slider", "button", "beveled" ) or even get more specific
 *  and target "slider"/"thumb" directly or any "thumb"/"button" that might exist in
 *  sliders or scrollbars, etc..</p>
 *
 *  @author    Paul Speed
 */
public class Styles {

    static Logger log = LoggerFactory.getLogger(Styles.class);

    public static final String ROOT_STYLE = "root";
    public static final ElementId DEFAULT_ELEMENT = new ElementId("default");

    private static Map<Class, List<Method>> methodIndex = new HashMap<Class, List<Method>>();
    private Set<Class> initialized = new HashSet<Class>();


    /**
     *  Maps a particular style to its style tree.  The style tree
     *  contains the tail-first hierarchy of selectors that are
     *  then composed to form a given elements attributes.
     */
    private Map<String, StyleTree> styleTrees = new HashMap<String, StyleTree>();

    /**
     *  Contains the map of lazily compiled attributes for a given
     *  style + element ID.  Each of these Attributes objects is a compiled
     *  hierarchy formed by breaking down the element ID into separate
     *  selectors.
     */
    private Map<String, Attributes> attributeMap = new HashMap<String, Attributes>();

    private Map<Class, Object> defaults = new HashMap<Class, Object>();

    /**
     *  The default style that is used when no style is specified by a GUI
     *  element.  This is different than the "root" style which is the style
     *  that is inherited by all other styles.  The default style is only used
     *  when looking up attributes for application to components.
     */
    private String defaultStyle = ROOT_STYLE;

    public Styles() {
        /*
            This object works in a few different modes.

            In setup mode, the caller can set the attributes of certain selectors
            and these are stored in the styleMap.

            In access mode, the attribute sets are compiled from all of the selectors
            so that a single Attributes object can be presented for a given style
            and element Id that has everything needed.

            This works because our element hierarchy is generally flat and
            known.  It's not like HTML/CSS where you can nest indefinitely.
         */
    }

    /**
     *  Sets the default style that is used by GUI elements when no other
     *  style is specified.  This is an easy way to provide a common look
     *  to the entire user interface without having to pass styles to every
     *  created component.
     *  Defaults to the root style.
     */
    public void setDefaultStyle( String style ) {
        if( style == null ) {
            style = ROOT_STYLE;
        }
        this.defaultStyle = style;
    }

    public String getDefaultStyle() {
        return defaultStyle;
    }

    public void clearCache() {
        attributeMap.clear();
    }

    public void setDefault( Object value ) {
        defaults.put(value.getClass(), value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefault( Class<T> type ) {
        return (T)defaults.get(type);
    }

    /**
     *  Retrieves the compiled attributes for the specified element ID
     *  and default style.  The attributes are compiled based on the
     *  selector rules and attributes setup prior to this call.
     */
    public Attributes getAttributes( ElementId elementId ) {
        return getAttributes(elementId, null);
    }

    /**
     *  Retrieves the compiled attributes for the specified element ID
     *  and style.  The attributes are compiled based on the
     *  selector rules and attributes setup prior to this call.
     */
    public Attributes getAttributes( ElementId elementId, String style ) {

        if( style == null ) {
            style = defaultStyle;
        }
        // See if we already have a cached version
        String key = styleKey(elementId, style);
        Attributes result = attributeMap.get(key);
        if( result == null ) {
            // Look it up and cache it
            result = getTree(style, true).getAttributes(elementId);

            // If this is not the default element then apply any
            // style-specific default attributes
            if( !DEFAULT_ELEMENT.equals(elementId) ) {
                result = result.merge(getTree(style, true).getAttributes(DEFAULT_ELEMENT));
            }

            // Apply default styles too if necessary
            if( !ROOT_STYLE.equals(style) ) {
                // Look-up the element ID in the default style
                Attributes toMerge = getAttributes(elementId, ROOT_STYLE);
                result = result.merge(toMerge);
            }

            // Cache it
            attributeMap.put(key, result);
        }
        return result;
    }

    /**
     *  Retrieves the compiled attributes for the specified element ID
     *  and default style.  The attributes are compiled based on the
     *  selector rules and attributes setup prior to this call.
     */
    public Attributes getAttributes( String elementId ) {
        return getAttributes(new ElementId(elementId), null);
    }

    /**
     *  Retrieves the compiled attributes for the specified element ID
     *  and style.  The attributes are compiled based on the
     *  selector rules and attributes setup prior to this call.
     */
    public Attributes getAttributes( String elementId, String style ) {
        return getAttributes(new ElementId(elementId), style);
    }

    protected String styleKey( ElementId elementId, String style ) {
        if( style == null || style.equals(ROOT_STYLE) ) {
            return elementId.getId();
        }
        return style + ":" + elementId.getId();
    }

    protected StyleTree getTree( String style, boolean create ) {
        if( style == null ) {
            style = ROOT_STYLE;
        }
        StyleTree tree = styleTrees.get(style);
        if( tree == null && create ) {
            tree = new StyleTree(this);
            styleTrees.put(style, tree);
        }
        return tree;
    }

    public Attributes getSelector( String style ) {
        return getSelector(DEFAULT_ELEMENT, style);
    }

    public Attributes getSelector( ElementId id, String style ) {
        // The implication is that we're about to set new style attributes...
        // so clear the cache
        clearCache();
        return getTree(style, true).getSelector(id, true);
    }

    public Attributes getSelector( String id, String style ) {
        return getSelector(new ElementId(id), style);
    }

    public Attributes getSelector( ElementId parent, ElementId child, String style ) {
        clearCache();
        return getTree(style, true).getSelector(parent, child, true);
    }

    public Attributes getSelector( ElementId parent, String child, String style ) {
        return getSelector(parent, new ElementId(child), style);
    }

    public Attributes getSelector( String parent, ElementId child, String style ) {
        return getSelector(new ElementId(parent), child, style);
    }

    public Attributes getSelector( String parent, String child, String style ) {
        return getSelector(new ElementId(parent), new ElementId(child), style);
    }

    public static void main( String... args ) {

        ElementId id = new ElementId( "slider.thumb.button" );
        System.out.println( "Parts:" + Arrays.asList(id.getParts()) );

        Styles test = new Styles();

        test.getSelector( "slider.thumb.button", null ).set( "color", "red" );
        test.getSelector( "thumb", "button", null ).set( "background", "angry" );
        test.getSelector( "slider.thumb.button", "foo").set( "background", "happy" );
        test.getSelector( "button", null ).set( "border", "outline" );
        test.getSelector( "button", "foo" ).set( "border", "dotted" );
        test.getSelector( "slider", "button", null).set( "action", "depress" );
        test.getSelector( "button", "foo" ).set( "action", "null" );
        test.getSelector( "foo" ).set( "color", "yellow" );

        Attributes a1 = test.getAttributes( "slider.thumb.button", ROOT_STYLE );
        System.out.println( "a1:" + a1 );

        Attributes a2 = test.getAttributes( "slider.thumb.button", "foo" );
        System.out.println( "a2:" + a2 );
    }
    // the old way for compatibility reasons
    public void initializeStyles(Class c){
        initializeStyles(c,null); 
   }
    
    public void initializeStyles(Class c, String style) {
        if( initialized.contains(c) )
            return;
        initialized.add(c);

        if( c.getSuperclass() != Object.class ) {
             initializeStyles(c.getSuperclass(), style);
        }

        // Find the right method
        Method[] methods = c.getMethods();
        for( Method m : methods ) {
            int mods = m.getModifiers();
            if( !Modifier.isStatic(mods) )
                continue;
            if( !Modifier.isPublic(mods) )
                continue;
            if( !m.isAnnotationPresent(StyleDefaults.class) )
                continue;

            StyleDefaults styleDefaults = m.getAnnotation(StyleDefaults.class);
            try {
                // It's the one... figure out how we should call it.
                Class[] parmTypes = m.getParameterTypes();
                Object[] args = new Object[parmTypes.length];
                for( int i = 0; i < parmTypes.length; i++ ) {
                    if( Styles.class.isAssignableFrom(parmTypes[i]) ) {
                        args[i] = this;
                    } else if( Attributes.class.isAssignableFrom(parmTypes[i]) ) {
                        args[i] = getSelector(styleDefaults.value(), style);
                    }
                }
                m.invoke(c, args);
            } catch( IllegalAccessException e ) {
                throw new RuntimeException("Error initializing styles for:" + c, e);
            } catch( InvocationTargetException e ) {
                throw new RuntimeException("Error initializing styles for:" + c, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Object getExistingValue( Object o, Method m ) {
        Class c = o.getClass();
        String name = m.getName();
        if( !name.startsWith("set") )
            return null;
        name = "g" + name.substring(1);
        try {
            m = c.getMethod(name);
        } catch( NoSuchMethodException e ) {
            return null;
        }
        try {
            return m.invoke(o);
        } catch( IllegalAccessException e ) {
            throw new RuntimeException("Error getting existing value from:" + m + " on:" + o, e);
        } catch( InvocationTargetException e ) {
            throw new RuntimeException("Error getting existing value from:" + m + " on:" + o, e);
        }
    }

    protected static List<Method> getStyleAttributeMethods( Class c ) {
        List<Method> results = methodIndex.get(c);
        if( results != null )
            return results;

        results = new ArrayList<Method>();
        for( Method m : c.getMethods() ) {
            if( m.isAnnotationPresent(StyleAttribute.class) ) {
                StyleAttribute attribute = m.getAnnotation(StyleAttribute.class);
                results.add(m);
            }
        }

        methodIndex.put(c, results);
        return results;
    }

    @Deprecated
    public void applyStyles( Object o, String elementId ) {
        applyStyles(o, new ElementId(elementId), null);
    }

    @Deprecated
    public void applyStyles( Object o, String elementId, String style ) {
        applyStyles(o, new ElementId(elementId), style);
    }

    public void applyStyles( Object o, ElementId elementId ) {
        applyStyles(o, elementId, null);
    }

    public void applyStyles( Object o, ElementId elementId ) {
        applyStyles(o, elementId, null);
    }
    
    @SuppressWarnings("unchecked")
    public void applyStyles( Object o, ElementId elementId, String style ) {

        Class c = o.getClass();
        if (style == null) style = this.getDefaultStyle();
        initializeStyles(c, style);

        if( log.isTraceEnabled() ) {
            log.trace("applyStyles elementId:" + elementId + " style:" + style + (style==null?"(" + defaultStyle + ")":""));
        }

        Attributes attrs = getAttributes(elementId, style);
        if( log.isTraceEnabled() ) {
            log.trace("style attributes:" + attrs);
        }

        Cloner cloner = null;

        for( Method m : getStyleAttributeMethods(c) ) {
            StyleAttribute attribute = m.getAnnotation(StyleAttribute.class);

            Class type = m.getParameterTypes()[0];

            // See if there is a method for getting the existing value
            // ...this only works for attributes that would be null when unset
            //Object existing = getExistingValue( o, m );
            //if( existing != null )
            //    continue;

            Object value = attrs.get(attribute.value(), type, attribute.lookupDefault());
            if( value == null )
                continue;

            // See if the value needs cloning
            Object original = value;
            value = clone(value, null);

            if( log.isTraceEnabled() && original != value ) {
                log.trace("Cloned value.\nOriginal:" + original + "\nClone:" + value);
            }

            // Else call it with the value
            try {
                if( log.isTraceEnabled() ) {
                    log.trace("calling " + m.getName() + " with:" + value);
                }
                m.invoke(o, value);
            } catch( IllegalAccessException e ) {
                throw new RuntimeException("Error applying attribute:" + attribute + " to:" + o, e);
            } catch( InvocationTargetException e ) {
                throw new RuntimeException("Error applying attribute:" + attribute + " to:" + o, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Object clone( Object value, Cloner cloner ) {

        // The cloner argument may be null when we're called from
        // the 'root' object.  This is to cover the case where we're just
        // cloning a GuiComponent directly... cloner can't do that and there is
        // no reason to create one just to throw it away.  Also no reason
        // to create one if the object isn't even cloneable.
        // As much as possible, this method tries to avoid creating garbage
        // if nothing is going to get cloned anyway.

        // Check the simple case first
        if( value instanceof GuiComponent ) {
            if( log.isTraceEnabled() ) {
                log.trace("Cloning GuiComponent:" + value);
            }
            // These can be cloned directly but we'll check the cloner
            // to see if we already may have
            if( cloner != null ) {
                if( cloner.isCloned(value) ) {
                    // Return what we've already cloned previously
                    return cloner.clone(value);
                } else {
                    // Save the clone for next time
                    Object result = ((GuiComponent)value).clone();
                    cloner.setClonedValue(value, result);
                    return result;
                }
            }
            // Else just clone it directly
            return ((GuiComponent)value).clone();
        }

        if( value instanceof List ) {
            if( log.isTraceEnabled() ) {
                log.trace("Attempting to clone list:" + value);
            }
            return cloneList((List<Object>)value, cloner == null ? new Cloner() : cloner);
        } else if( value instanceof Map ) {
            if( log.isTraceEnabled() ) {
                log.trace("Attempting to clone map:" + value);
            }
            return cloneMap((Map<Object, Object>)value, cloner == null ? new Cloner() : cloner);
        } else if( value instanceof Cloneable ) {
            if( log.isTraceEnabled() ) {
                log.trace("Attempting to clone value:" + value);
            }
            if( cloner == null ) {
                cloner = new Cloner();
            }
            return cloner.clone(value);
        }

        // Value was not cloneable... just return it
        return value;
    }

    protected Map<Object, Object> cloneMap( Map<Object, Object> source, Cloner cloner ) {

        // See if we've already cloned this one
        if( cloner.isCloned(source) ) {
            // clone() will just lookup the previous value
            return (Map<Object, Object>)cloner.clone(source);
        }

        // We'll delay creating a map until we have a real
        // cloned value that requires it
        Map<Object, Object> result = null;

        // Go through each of the entries and clone the values if possible
        for( Map.Entry<Object, Object> e : source.entrySet() ) {
            Object value = e.getValue();
            Object clone = clone(value, cloner);
            if( value != clone ) {
                // We actually cloned the value... so we'll store it.
                if( result == null ) {
                    // Need to create the map into which we'll poor our values
                    // First do a shallow clone if possible to keep the implementation
                    // the same in case it's a special map class
                    if( source instanceof Cloneable ) {
                        try {
                            result = (Map<Object, Object>)cloner.javaClone(source);
                        } catch( CloneNotSupportedException ex ) {
                            log.warn("Map implementation cannot be cloned:" + source.getClass() + " values:" + source, ex);

                            // Else just create any old map
                            result = new HashMap<>(source);
                        }
                    } else {
                        // Else just create any old map
                        result = new HashMap<>(source);
                    }
                }
                result.put(e.getKey(), clone);
            }
        }

        if( result != null ) {
            // Cache it for later
            cloner.setClonedValue(source, result);
            return result;
        }

        // Else we didn't clone it so just return the value directly
        return source;
    }

    protected List<Object> cloneList( List<Object> source, Cloner cloner ) {

        // See if we've already cloned this one
        if( cloner.isCloned(source) ) {
            // clone() will just lookup the previous value
            return (List<Object>)cloner.clone(source);
        }

        // We'll delay creating a list until we have a real
        // cloned value that requires it
        List<Object> result = null;

        for( int i = 0; i < source.size(); i++ ) {
            Object value = source.get(i);
            Object clone = clone(value, cloner);
            if( clone != value ) {
                // We actually cloned the value... so we'll store it.
                if( result == null ) {
                    // First do a shallow clone if possible to keep the implementation
                    // the same in case it's a special map class
                    if( source instanceof Cloneable ) {
                        try {
                            result = (List<Object>)cloner.javaClone(source);
                        } catch( CloneNotSupportedException e ) {
                            log.warn("List implementation cannot be cloned:" + source.getClass() + " values:" + source, e);

                            // Else just create any old map
                            result = new ArrayList<>(source);
                        }
                    } else {
                        // Else just create any old map
                        result = new ArrayList<>(source);
                    }
                }
                result.set(i, clone);
            }
        }

        if( result != null ) {
            // Cache it for later
            cloner.setClonedValue(source, result);
            return result;
        }

        // Else we didn't clone it so just return the value directly
        return source;
    }

}
