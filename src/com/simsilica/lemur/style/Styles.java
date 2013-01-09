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

import com.jme3.font.BitmapFont;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.core.GuiComponent;
import com.simsilica.lemur.core.GuiLayout;


/**
 *
 *  @author    Paul Speed
 */
public class Styles
{
    public static final String KEY_DEFAULT = "default";
    public static final String DEFAULT_STYLE = "default";
    public static final String DEFAULT_ELEMENT = "default";
    
    private static Map<Class, List<Method>> methodIndex = new HashMap<Class, List<Method>>();    
    private Set<Class> initialized = new HashSet<Class>();


    // Key is the "style" value is the attributes for
    // specific selector types.  Note: if we "compile" this then
    // we could flatten it considerably.
    // Without that, Attributes objects can be used as setters
    // but not as a general purpose "all attributes for this thing".
    // You know... we could support recompiling.  Any time a new attribute
    // is pushed then we mark us as "not compiled" and recompile on next
    // access.
    // Except we compile on access... since we don't necessarily know
    // full element IDs until they are requested.  If we don't already
    // have one then we look it up.
    private Map<String,Map<Selector,Attributes>> styleMap = new HashMap<String,Map<Selector,Attributes>>();

    // The compiled attributes.  Null if the attribute sets
    // have not yet been compiled.  
    private Map<String,Attributes> attributeMap = new HashMap<String,Attributes>();
    
    private Map<Class,Object> defaults = new HashMap<Class,Object>(); 

    public Styles()
    {
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
 
    public void clearCache()
    {
        attributeMap.clear();   
    }

    public void setDefault( Object value )
    {
        defaults.put( value.getClass(), value );
    }
    
    public <T> T getDefault( Class<T> type )
    {
        return (T)defaults.get(type);
    }  

    public Attributes getAttributes( String elementId )
    {
        return getStyle(elementId, DEFAULT_STYLE, true);
    }    
 
    public Attributes getAttributes( String elementId, String style )
    {
        if( style == null )
            style = DEFAULT_STYLE;
        return getStyle(elementId, style, true);
    }    
 
    protected Attributes getStyle( String id, String style, boolean create )
    {
        Attributes result = attributeMap.get(id + "." + style);
        if( result == null && create )
            {
            result = new Attributes(this);
            compile(id, style, result);
            if( style != DEFAULT_STYLE )
                compile(id, DEFAULT_STYLE, result);
            attributeMap.put(id, result);            
            }
        return result;        
    }
 
    protected void compile( String id, String style, Attributes attributes )
    {
        Map<Selector,Attributes> map = styleMap.get(style);
        if( map == null || map.size() == 0 )
            return;
    
        Selector[] parts = getSelectors(id);        
        for( Selector s : parts )
            {
            Attributes sub = map.get(s);
            if( sub == null )
                continue;
            attributes.applyNew(sub);                
            }   
    }
 
    protected Map<Selector,Attributes> getSelectorMap( String style, boolean create )
    {
        Map<Selector,Attributes> result = styleMap.get(style);
        if( result == null && create )
            {
            result = new HashMap<Selector,Attributes>();
            styleMap.put(style, result);
            }
        return result;
    } 
 
    protected Attributes getAttributes( Selector key, String style, boolean create )
    {
        if( style == null )
            style = DEFAULT_STYLE;
        Map<Selector, Attributes> selMap = getSelectorMap(style,create);
        if( selMap == null )
            return null;
             
        Attributes result = selMap.get(key);
        if( result == null && create )
            {
            result = new Attributes(this);
            selMap.put(key, result);
            }
        return result;
    }
 
    public Attributes getSelector( String style )
    {
        return getSelector( DEFAULT_ELEMENT, style );
    } 
 
    public Attributes getSelector( String id, String style )
    {
        Selector key = new ElementSelector(id);
        return getAttributes( key, style, true );        
    }
 
    public Attributes getSelector( String parent, String child, String style )
    {
        Selector key = new ContainsSelector(parent, child);
        return getAttributes( key, style, true );        
    }
 
    public static void main( String... args )
    {
        ElementId id = new ElementId( "slider.thumb.button" );
        System.out.println( "Parts:" + Arrays.asList(id.getParts()) );
 
        for( Selector s : id.getSelectors() )
            System.out.println( "   " + s );
     
        Styles test = new Styles();
        
        test.getSelector( "slider.thumb.button", null ).set( "color", "red" );
        test.getSelector( "thumb", "button", null ).set( "background", "angry" );
        test.getSelector( "slider.thumb.button", "foo").set( "background", "happy" );
        test.getSelector( "button", null ).set( "border", "outline" );
        test.getSelector( "button", "foo" ).set( "border", "dotted" );
        test.getSelector( "slider", "button", null).set( "action", "depress" );
        test.getSelector( "button", "foo" ).set( "action", "null" );
        test.getSelector( "foo" ).set( "color", "yellow" );
 
        Attributes a1 = test.getStyle( "slider.thumb.button", DEFAULT_STYLE, true );
        System.out.println( "a1:" + a1 );

        Attributes a2 = test.getStyle( "slider.thumb.button", "foo", true );
        System.out.println( "a2:" + a2 );
    }
 
    public static Selector[] getSelectors( String id )
    {
        String[] parts = id.split("\\.");
            
        List<Selector> list = new ArrayList<Selector>();
        list.add( new ElementSelector(id) );
        
        int last = parts.length - 1;
        while( last >= 0 )
            {
            String top = parts[last--];
            for( int i = last; i >= 0; i-- )
                {
                list.add( new ContainsSelector(parts[i], top) );
                }
            list.add( new ElementSelector(top) ); 
            }
        list.add( new ElementSelector(KEY_DEFAULT) );
            
        Selector[] results = list.toArray( new Selector[list.size()] );
        return results;
    }
    
    public void initializeStyles(Class c)
    {
        if( initialized.contains(c) )
            return;
        initialized.add(c);
 
        if( c.getSuperclass() != Object.class )
            initializeStyles(c.getSuperclass());
                
        // Find the right method
        Method[] methods = c.getMethods();
        for( Method m : methods )
            {
            int mods = m.getModifiers();
            if( !Modifier.isStatic(mods) )
                continue;
            if( !Modifier.isPublic(mods) )
                continue;
            if( !m.isAnnotationPresent(StyleDefaults.class) )
                continue;
 
            StyleDefaults styleDefaults = m.getAnnotation(StyleDefaults.class);
            try
                {               
                // It's the one... figure out how we should call it.
                Class[] parmTypes = m.getParameterTypes();
                Object[] args = new Object[parmTypes.length];
                for( int i = 0; i < parmTypes.length; i++ )
                    {
                    if( Styles.class.isAssignableFrom(parmTypes[i]) )
                        args[i] = this;
                    else if( Attributes.class.isAssignableFrom(parmTypes[i]) )
                        args[i] = getSelector(styleDefaults.value(), null); 
                    }
                m.invoke(c, args);
                }                
            catch( IllegalAccessException e )
                {
                throw new RuntimeException( "Error initializing styles for:" + c, e );
                }                                                            
            catch( InvocationTargetException e )
                {
                throw new RuntimeException( "Error initializing styles for:" + c, e );
                }                                                            
            }        
    }
 
    protected Object getExistingValue( Object o, Method m ) 
    {
        Class c = o.getClass();
        String name = m.getName();
        if( !name.startsWith("set") )
            return null;
        name = "g" + name.substring(1);
        try
            {
            m = c.getMethod(name);
            }
        catch( NoSuchMethodException e )
            {
            return null;
            }
        try
            {
            return m.invoke(o);
            }
        catch( IllegalAccessException e )
            {
            throw new RuntimeException( "Error getting existing value from:" + m + " on:" + o, e );
            }                                                            
        catch( InvocationTargetException e )
            {
            throw new RuntimeException( "Error getting existing value from:" + m + " on:" + o, e );
            }                                                            
    }
    
    protected static List<Method> getStyleMethods( Class c )
    {
        List<Method> results = methodIndex.get(c);
        if( results != null )
            return results;
        
        results = new ArrayList<Method>();
        for( Method m : c.getMethods() )
            {
            if( m.isAnnotationPresent(StyleAttribute.class) )
                {
                StyleAttribute attribute = m.getAnnotation(StyleAttribute.class);
                results.add(m);
                }
            }
        
        methodIndex.put(c, results);
        return results;        
    }

    public void applyStyles( Object o, String elementId )
    {
        applyStyles( o, elementId, DEFAULT_STYLE );
    }
 
    public void applyStyles( Object o, String elementId, String style )
    {
        if( style == null )
            style = DEFAULT_STYLE;
            
        Class c = o.getClass();
        initializeStyles(c);
        
        Attributes attrs = getAttributes( elementId, style );
        
        for( Method m : getStyleMethods(c) )
            {
            StyleAttribute attribute = m.getAnnotation(StyleAttribute.class);

            Class type = m.getParameterTypes()[0];

            // See if there is a method for getting the existing value
            // ...this only works for attributes that would be null when unset
            //Object existing = getExistingValue( o, m );
            //if( existing != null )
            //    continue;

            Object value = attrs.get( attribute.value(), type, attribute.lookupDefault() );
            if( value == null )
                continue;
 
            // FIXME: better cloner here
            if( value instanceof GuiComponent )
                {
                value = ((GuiComponent)value).clone();
                }
    
            // Else call it with the value
            try
                {
                m.invoke( o, value );
                }
            catch( IllegalAccessException e )
                {
                throw new RuntimeException( "Error applying attribute:" + attribute + " to:" + o, e );
                }                                                            
            catch( InvocationTargetException e )
                {
                throw new RuntimeException( "Error applying attribute:" + attribute + " to:" + o, e );
                }                                                            
            }
    }
}
