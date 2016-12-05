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

import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;

/**
 *  DocumentModel is a container for text that provides basic editing
 *  interaction as used by things like TextField.
 *
 *  @author    Paul Speed
 */
public interface DocumentModel extends VersionedObject<DocumentModel> {

    /**
     *  Deep clones this document model. 
     */
    public DocumentModel clone();

    /**
     *  Replaces the text contained in this DocumentModel.
     */
    public void setText( String text );

    /**
     *  Returns the current text value contained in this DocumentModel.
     */
    public String getText(); 

    /**
     *  Returns the string representing just the specified line of text.
     */
    public String getLine( int line ); 

    /**
     *  Returns the current number of lines in this document.
     */
    public int getLineCount(); 

    /**
     *  Returns the current 'carat' position.  The 'carat' is where
     *  new text characters will be inserted.  It's the current edit 
     *  position.
     */
    public int getCarat(); 

    /**
     *  Returns the line number containing the current carat position.
     */
    public int getCaratLine(); 

    /**
     *  Returns the column number of the current carat position in the
     *  line returned by getCaratLine().
     */
    public int getCaratColumn(); 

    /**
     *  Returns the line number containing the current anchor position.
     */
    public int getAnchorLine(); 

    /**
     *  Returns the column number of the current anchor position in the
     *  line returned by getAnchorLine().
     */
    public int getAnchorColumn();

    /**
     *  During text selection, this is one end of the selection where the
     *  other end is the carat position.
     */
    public int getAnchor(); 

    /**
     *  Moves the carat to the document's home position or the current line's
     *  home position depending on the specified 'currentLine' value.  If 
     *  currentLine is true then the home position is just before the first
     *  character in the current line.  If the currentLine parameter is false then
     *  the home position is just before the first character in the whole
     *  document.
     */
    public int home( boolean currentLine ); 

    /**
     *  Moves the carat to the document's end position or the current line's
     *  end position depending on the specified 'currentLine' value.  If 
     *  currentLine is true then the end position is just after the last
     *  character in the current line.  If the currentLine parameter is false then
     *  the end position is just after the last character in the whole
     *  document.
     */
    public int end( boolean currentLine ); 
    
    /**
     *  Moves the carat position to the previous line if there is one.  After
     *  this call, the new column position is implementation dependent.
     */
    public int up(); 
 
    /**
     *  Moves the carat position to the next line if there is one.  After this
     *  call, the new column position is implementation dependent.
     */   
    public int down(); 

    /**
     *  Moves the carat one position to the left, potentially moving it to the
     *  previous line depending on the actual DocumentModel implementation.
     */
    public int left(); 

    /**
     *  Moves the carat one position to the right, potentially moving it to the
     *  next line depending on the actual DocumentModel implementation.
     */
    public int right();

    /**
     *  Inserts a new line at the current carat position.
     */
    public void insertNewLine(); 

    /**
     *  Deletes the character at the specified position.
     */
    public void deleteCharAt( int pos ); 

    /**
     *  Deletes the character immediately before the current carat position.
     *  This may move the carat to the previous line if the carat was previously
     *  at the beginning of a line.
     */
    public void backspace(); 

    /**
     *  Deletes the character immediately after the current carat position.
     */
    public void delete(); 

    /**
     *  Inserts a character at the current carat position.
     */
    public void insert( char c ); 

    /**
     *  Bulk inserts a string of text.
     */
    public void insert( String text );

    /**
     *  Returns a VersionedReference that can be watched for changes to
     *  the carat position.
     */
    public VersionedReference<Integer> createCaratReference();
} 
