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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.core.VersionedReference;


/**
 *  A default implementation of the DocumentModel interface.
 *
 *  @author    Paul Speed
 */
public class DefaultDocumentModel implements DocumentModel, Cloneable {

    private long version;
    private List<StringBuilder> lines = new ArrayList<StringBuilder>();
    private String composite = null;
    private Carat carat = new Carat();
    private int line = 0;
    private int column = 0;

    public DefaultDocumentModel() {
        parseText("");
    }

    public DefaultDocumentModel( String text ) {
        parseText(text!=null?text:"");
    }
    
    @Override
    public DefaultDocumentModel clone() {
        try {
            DefaultDocumentModel result = (DefaultDocumentModel)super.clone();
            
            // Deep clone the lists
            result.lines = new ArrayList<StringBuilder>(lines.size());
            for( int i = 0; i < result.lines.size(); i++ ) {
                StringBuilder sb = lines.get(i);
                result.lines.set(i, new StringBuilder(sb));
            }
            
            result.carat = carat.clone();
            
            // And reset the version because it's ok for this document to start
            // over
            result.version = 0;
 
            return result;           
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Clone not supported", e);
        }
    }

    @Override
    public void setText( String text ) {
        parseText(text!=null?text:"");
    }

    @Override
    public String getText() {
        if( composite == null )
            createComposite();
        return composite;
    }

    @Override
    public String getLine( int line ) {
        return lines.get(line).toString();
    }

    @Override
    public int getLineCount() {
        return lines.size();
    }

    @Override
    public int getCarat() {
        return carat.get();
    }

    @Override
    public int getCaratLine() {
        return line;
    }

    @Override
    public int getCaratColumn() {
        return column;
    }

    @Override
    public int getAnchorLine() {
        return getCaratLine();
    }

    @Override
    public int getAnchorColumn() {
        return getCaratColumn();
    }

    @Override
    public int getAnchor() {
        return getCarat();
    }

    @Override
    public int home( boolean currentLine ) {
        if( currentLine ) {
            carat.move(-column);
            column = 0;
        } else {
            carat.set(0);
            column = 0;
            line = 0;
        }
        return carat.get();
    }

    @Override
    public int end( boolean currentLine ) {
        if( currentLine ) {
            StringBuilder row = lines.get(line);
            carat.move(row.length() - column);
            column = row.length();
        } else {
            // Find the end of the document
            carat.set(0);
            column = 0;
            line = 0;
            for( int i = 0; i < lines.size(); i++ ) {
                if( i > 0 ) {
                    carat.increment();
                }
                StringBuilder row = lines.get(i);
                carat.move(row.length());
                column = row.length();
            }
            line = lines.size() - 1;
        }
        return carat.get();
    }

    @Override
    public int up() {
        if( line == 0 )
            return carat.get();

        // Carat needs to lose the beginning of this line
        // Take it home
        carat.move(-column);

        // Take it to the end of the previous line
        line--;
        carat.decrement();

        if( column <= lines.get(line).length() ) {
            // Then we need to move the carat by the
            // rest of this line, too
            carat.move(-(lines.get(line).length() - column));
        } else {
            // Don't need to adjust the carat because it is already in the
            // right place.
            column = lines.get(line).length();
        }

        return carat.get();
    }

    @Override
    public int down() {
        if( line == lines.size() - 1 )
            return carat.get();

        // Take the carat to the end of this line
        int restOfLine = lines.get(line).length() - column;
        carat.move(restOfLine);

        // Take it to the beginning of the next line
        line++;
        carat.increment();

        // Then move it out as much as we can to fit the previous column
        column = Math.min(column, lines.get(line).length());
        carat.move(column);

        return carat.get();
    }

    @Override
    public int left() {
        if( carat.get() == 0 )
            return 0;
        carat.decrement();
        column--;
        if( column < 0 ) {
            line--;

            if( line < 0 ) {
                System.out.println( "How did this happen?  carat:" + carat );
            }

            column = lines.get(line).length();
        }
        return carat.get();
    }

    @Override
    public int right() {
        column++;
        carat.increment();
        if( column > lines.get(line).length() ) {
            if( line < lines.size() - 1 ) {
                line++;
                column = 0;
            } else {
                column--;
                carat.decrement();
            }
        }
        return carat.get();
    }

    @Override
    public void insertNewLine() {
        if( line == lines.size() - 1 && column == lines.get(line).length() ) {
            lines.add(new StringBuilder());
        } else {
            // Otherwise... we need to split the current line
            StringBuilder row = lines.get(line);
            StringBuilder next = new StringBuilder(row.substring(column));
            row.delete(column, row.length());
            lines.add(line+1, next);
        }

        line++;
        column=0;
        carat.increment();  // A new line is still a "character"

        composite = null;
        version++;
    }

    @Override
    public void deleteCharAt( int pos ) {
        // Some optimized paths
        if( pos == carat.get() - 1 ) {
            backspace();
            return;
        } else if( pos == carat.get() ) {
            delete();
            return;
        }

        int[] location = new int[2];
        findPosition(pos, location);

        if( location[0] >= lines.size() )
            return; // nothing to delete

        StringBuilder row = lines.get(location[0]);
        if( location[1] == row.length() ) {
            if( location[0] < lines.size() - 1 ) {
                // Need to merge this line with the next
                row.append(lines.get(location[0]+1));
                lines.remove(location[0] + 1);
            } else {
                // Nothing to do and I don't know how the earlier
                // check failed.
                return;
            }
        } else {
            // Just delete the proper character
            row.deleteCharAt(location[1]);
        }

        // If the carat is after the delete position then
        // we need to adjust it... and the current line and column.
        if( carat.get() <= pos ) {
            carat.decrement();
            findPosition(carat.get(), location);
            line = location[0];
            column = location[1];
        }

        composite = null;
        version++;
    }

    @Override
    public void backspace() {
        if( carat.get() == 0 )
            return;

        if( column == 0 ) {
            if( line > 0 ) {
                // Need to merge this line with the previous
                column = lines.get(line-1).length();
                lines.get(line-1).append(lines.remove(line));
                carat.decrement();
                line--;
            } else {
                // Nothing to do
                return;
            }
        } else {
            StringBuilder row = lines.get(line);
            row.deleteCharAt(column - 1);
            column--;
            carat.decrement();
        }
        composite = null;
        version++;
    }

    @Override
    public void delete() {
        StringBuilder row = lines.get(line);
        if( column == row.length() ) {
            if( line >= lines.size() - 1 )
                return;

            row.append(lines.remove(line+1));
        } else {
            row.deleteCharAt(column);
        }
        composite = null;
        version++;
    }

    /**
     *  Find the line and column of the specified text position.
     */
    protected void findPosition( int pos, int[] location ) {
        int index = 0;
        location[0] = 0;
        location[1] = 0;
        for( int r = 0; r < lines.size(); r++ ) {
            StringBuilder l = lines.get(r);
            if( pos - index <= l.length() ) {
                // Found the line
                location[0] = r;
                location[1] = pos - index;
                return;
            }

            // Else we need to advance
            index += l.length() + 1;
        }
        location[0] = lines.size();
        location[1] = 0;
    }

    @Override
    public void insert( char c ) {
        if( c < 32 )
            return;

        switch( c ) {
            default:
                // For now
                lines.get(line).insert(column, c);
                //carat++;
                carat.increment();
                column++;
                break;
        }

        composite = null;
        version++;
    }

    @Override
    public void insert( String text ) {
        for( int i = 0; i < text.length(); i++ ) {
            insert(text.charAt(i));
        }
    }

    @Override
    public long getVersion() {
        return version;
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
        return carat.createReference();
    }

    protected void parseText( String text ) {
        composite = null;
        lines.clear();
        StringTokenizer st = new StringTokenizer(text, "\r\n");
        while( st.hasMoreTokens() ) {
            String token = st.nextToken();
            lines.add(new StringBuilder(token));
        }

        // Always at least one line
        if( lines.isEmpty() ) {
            lines.add(new StringBuilder());
        }

        end(false);
        version++;
    }

    protected void createComposite() {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < lines.size(); i++ ) {
            sb.append(lines.get(i));
            if( i < lines.size() - 1 ) {
                sb.append( "\n" );
            }
        }
        this.composite = sb.toString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[]";
    }
    
    private class Carat implements VersionedObject<Integer> {
        private int value;
        private long version;
        
        public Carat() {
        }
 
        public Carat clone() {
            Carat result = new Carat();
            result.value = value;
            // Don't need to set the version because it's a new object and
            // can start over.
            return result;
        }
 
        public final int get() {
            return value;
        }
 
        public final int set( int value ) {
            if( this.value == value ) {
                return value;
            }
            this.value = value;
            version++;
            return value;
        }
        
        public final int move( int amount ) {
            value += amount;
            version++;
            return value;
        }
        
        public final int increment() {
            value++;
            version++;
            return value;
        }
        
        public final int decrement() {
            value--;
            version++;
            return value;
        }

        @Override       
        public final long getVersion() {
            return version;
        } 

        @Override       
        public final Integer getObject() {
            return value;
        }

        @Override       
        public final VersionedReference<Integer> createReference() {
            return new VersionedReference<Integer>(this);
        }
 
        @Override       
        public final String toString() {
            return "Carat[" + value + "]";
        }
    }
}
