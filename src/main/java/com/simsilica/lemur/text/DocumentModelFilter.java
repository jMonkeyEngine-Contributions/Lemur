/*
 * $Id$
 * 
 * Copyright (c) 2016, Simsilica, LLC
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

package com.simsilica.lemur.text;

import java.util.Objects;

import com.google.common.base.Function;

import com.simsilica.lemur.core.VersionedReference;

/**
 *  A convenient base class for DocumentModel filtering that simply passes
 *  most methods through to the wrapped delegate DocumentModel, while intercepting
 *  others for subclasses to filter.  Subclasses can override the filterInput()
 *  and filterOutput() methods to provide simpler filtering or simply set
 *  Function implementations for those transforms.
 *  Advanced output filtering (where the output string maybe a different
 *  size then what is kept by the delegate) would require more extensive overriding
 *  of behavior to track carat location, line, column, and so on.
 *
 *  @author    Paul Speed
 */
public class DocumentModelFilter implements DocumentModel {
 
    private DocumentModel delegate;
    private Function<Character, Character> inputTransform;
    private Function<String, String> outputTransform;
    private String lastOutput = null;
    private String lastTransformedOutput = null;
    private long version;
    
    public DocumentModelFilter() {
        this(new DefaultDocumentModel());
    }
    
    public DocumentModelFilter( DocumentModel delegate ) {
        this.delegate = delegate;
    } 

    /**
     *  Returns the delegate document model for which this document model
     *  filter is filtering,
     */
    public DocumentModel getDelegate() {
        return delegate;
    }

    /**
     *  Sets an optional output transform function that will convert
     *  text before returning it from getText().  Note: with the default
     *  implementation of DocumentModelFilter, the transformed text must
     *  be a character for character replacement or the carat position
     *  will be incorrectly reported from the callers' perspective.
     *  If the supplied function is null then no output transformation
     *  is done by default.
     */
    public void setOutputTransform( Function<String, String> f ) {
        if( outputTransform == f ) {
            return;
        }
        this.outputTransform = f;
        version++;
    }
    
    public Function<String, String> getOutputTransform() {
        return outputTransform;
    }
    
    /**
     *  Sets an optional input transform that will be used to convert
     *  all input from setText() or insert().  If the function returns null
     *  for any character then that character is skipped in the input.
     *  If the supplied function is null then no input transformation is 
     *  done by default.
     */
    public void setInputTransform( Function<Character, Character> f ) {
        if( inputTransform == f ) {
            return;
        }
        this.inputTransform = f;
        version++;
    }
    
    public Function<Character, Character> getInputTransform() {
        return inputTransform;
    } 
    
    public DocumentModel clone() {
        return new DocumentModelFilter(delegate.clone());
    }

    /**
     *  Can be overridden to bulk filter input provided to
     *  setText().  Default implementation returns the text directly
     *  unless there is an input transform function set.  If there is
     *  an input transform function set then each character is first
     *  passed through that function to build a new string.  
     */
    protected String filterInput( String text ) {
        if( inputTransform != null ) {
            StringBuilder sb = new StringBuilder();
            for( int i = 0; i < text.length(); i++ ) {
                char c = text.charAt(i);
                Character x = inputTransform.apply(c);
                if( x != null ) {
                    sb.append(x);
                }
            }
            return sb.toString();
        }
        return text;
    }

    /**
     *  Can be overridden to filter the input provided to
     *  insert().  The default implementation returns the character directly.
     */
    protected Character filterInput( char c ) {
        if( inputTransform != null ) {
            return inputTransform.apply(c);
        }        
        return c;
    }

    /**
     *  Can be overridden to filter the output from the getText()
     *  method, for example replacing all characters with '*' for
     *  a password field.  The default implementation checks for
     *  a transform function and uses it or just returns the string
     *  directly. 
     */
    protected String filterOutput( String text ) {
        if( outputTransform != null ) {
            return outputTransform.apply(text);
        }
        return text;
    }

    @Override
    public void setText( String text ) {
        delegate.setText(filterInput(text));
    }

    /**
     *  Passes the delegate's getText() through the local filterOutput()
     *  method before returning.
     */
    @Override
    public String getText() {
        String output = delegate.getText();
        if( Objects.equals(output, lastOutput) ) {
            return lastTransformedOutput;
        }
        // Cache the results for next time.  If the delegate's text
        // doesn't change then we avoid needlessly calling the transform.
        lastOutput = output;
        lastTransformedOutput = filterOutput(output);  
        return lastTransformedOutput;  
    } 

    /**
     *  Passes the delegate's getLine() through the local filterOutput()
     *  method before returning.
     */
    @Override
    public String getLine( int line ) {
        return filterOutput(delegate.getLine(line));
    } 

    @Override
    public int getLineCount() {
        return delegate.getLineCount();
    } 

    @Override
    public int getCarat() {
        return delegate.getCarat();
    } 

    @Override
    public int getCaratLine() {
        return delegate.getCaratLine();
    } 

    @Override
    public int getCaratColumn() {
        return delegate.getCaratColumn();
    } 

    @Override
    public int getAnchorLine() {
        return delegate.getAnchorLine();
    } 

    @Override
    public int getAnchorColumn() {
        return delegate.getAnchorColumn();
    }

    @Override
    public int getAnchor() {
        return delegate.getAnchor();
    } 

    @Override
    public int home( boolean currentLine ) {
        return delegate.home(currentLine);
    } 

    @Override
    public int end( boolean currentLine ) {
        return delegate.end(currentLine);
    } 
    
    @Override
    public int up() {
        return delegate.up();
    } 
 
    @Override
    public int down() {
        return delegate.down();
    } 

    @Override
    public int left() {
        return delegate.left();
    } 

    @Override
    public int right() {
        return delegate.right();
    }

    @Override
    public void insertNewLine() {
        delegate.insertNewLine();
    } 

    @Override
    public void deleteCharAt( int pos ) {
        delegate.deleteCharAt(pos);
    } 

    @Override
    public void backspace() {
        delegate.backspace();
    } 

    @Override
    public void delete() {
        delegate.delete();
    } 

    @Override
    public void insert( char c ) {
        Character x = filterInput(c);
        if( x != null ) { 
            delegate.insert(x);
        }
    } 

    @Override
    public void insert( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            insert(text.charAt(i));
        }
    }

    @Override
    public long getVersion() {
        // We include our local version in case we need to update views
        // of ourselves even if the underlying model hasn't changed.
        return delegate.getVersion() + version;
    }
    
    @Override
    public DocumentModel getObject() {
        return this;
    }
    
    @Override
    public VersionedReference<DocumentModel> createReference() {
        return new VersionedReference<DocumentModel>(this);
    }
    
    @Override
    public VersionedReference<Integer> createCaratReference() {
        return delegate.createCaratReference();
    }    
}
