/*
 * $Id$
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */

package com.simsilica.lemur.input;

import java.util.*;

import com.google.common.base.Objects;


/**
 *
 *  @version   $Revision$
 *  @author    Paul Speed
 */
public class FunctionId
{
    public static final String DEFAULT_GROUP = "default";
    
    private static Set<FunctionId> existing = new HashSet<FunctionId>(); 
    
    private String group;
    private String id;
    private String name;
    
    public FunctionId( String id )
    {
        this(DEFAULT_GROUP, id, id);
    }
    
    public FunctionId( String group, String id )
    {
        this(group, id, id);
    }
    
    public FunctionId( String group, String id, String name )
    {
        this.group = group;
        this.id = id;
        this.name = name;
        
        // Have to check last
        if( !existing.add(this) )
            {
            // This is a duplicate
            throw new RuntimeException( "FunctionId already exists for:" + group + ", " + id );
            }  
    }
    
    public String getGroup()
    {
        return group;
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
        FunctionId other = (FunctionId)o;
        if( !Objects.equal( id, other.id ) )
            return false;
        if( !Objects.equal( group, other.group ) )
            return false;
        return true;
    }
    
    @Override
    public String toString()
    {
        return "FunctionId[" + group + ":" + id + "]";
    }
}
