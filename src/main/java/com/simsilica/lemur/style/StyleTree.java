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

import java.util.HashMap;
import java.util.Map;


/**
 *  Used internally by the Styles API to track the style
 *  definition hierarchy in tail-first form.
 *
 *  @author    Paul Speed
 */
public class StyleTree {

    private Styles styles;
    private Node root = new Node(null);

    public StyleTree( Styles styles ) {
        this.styles = styles;
    }

    protected Node getRoot() {
        return root;
    }

    public Attributes getSelector( ElementId id, boolean create ) { 
        Node node = findChild(root, id.getParts(), create);   
        if( node == null ) {
            return null;
        }
        return node.getAttributes(create);
    }
    
    public Attributes getSelector( ElementId parent, ElementId child, boolean create ) {
 
        Node nested = findChild(root, child.getParts(), create);
        if( nested == null ) {
            return null;
        }
        
        Node wildCard = nested.getChild(null, create);
        if( wildCard == null ) {
            return null;
        }
 
        Node node = findChild(wildCard, parent.getParts(), create);
        if( node == null ) {
            return null;
        }
    
        return node.getAttributes(create);
    } 

    public Attributes getAttributes( ElementId elementId ) {
        Attributes results = new Attributes(styles);

        String[] parts = elementId.getParts();
        
        // Recursively descend starting at the tail of the ID
        // doing a depth first traversal.  When a wild card is
        // hit then a second stage traversal is done allowing
        // gaps.
        accumulateAttributes(root, parts, parts.length - 1, true, results);
        
        return results;
    }

    protected void accumulateAttributes( Node node, String[] parts, int index, boolean followWildCards,
                                         Attributes results ) {
 
        // At each level we check to see if there is an exact match
        // here and then traverse.  If there are no more 'parts' then we 
        // stop and just grab any attributes on the way back up.  This
        // means we'll pick up any attributes for 'up.button' if that's the
        // only ID we passed in.
        //
        // It's safe to pick up any attributes along the way because those
        // are specific selectors in the hierarchy.  If 'button' has attributes
        // then that's because the general 'button' was assigned attributes.
        if( index < 0 ) {
            return;
        }
 
        // So check for an exact match at this level       
        String key = parts[index];       
        Node child = node.getChild(key, false);
        if( child != null ) {
            accumulateAttributes(child, parts, index-1, followWildCards, results);
        }
 
        if( followWildCards ) {       
            // The above accumulate already went as low as it could go and
            // accumulated the longest specific chain that it could at that level.
            // Before we apply any of the direct child's attributes we need to
            // drop down and do a wild card search.
            // ...if we still have parts left.
            Node wildCard = node.getChild(null, false);

            //if( index > 0 && wildCard != null ) {
            // Here I had index > 0 before but I think that's wrong.  It would
            // fail to find wild-carded 'parents' at the highest level.
            // So something like "something.label" with a something * label rule
            // would fail to resolve            
            if( wildCard != null ) {
                // Check each part for a wild card child and apply
                // each in turn.  Because we start with most current then
                // we _should_ get most specific first.
                for( int i = index; i >= 0; i-- ) {
                    Node n = wildCard.getChild(parts[i], false);
                    if( n == null ) {
                        continue;
                    }
                    accumulateAttributes(n, parts, i-1, false, results);
 
                    // If this node has attributes then that means the
                    // less specific wild-carded container had attributes.
                    // We need to apply them.          
                    // For example: attributes set for slider | button
                    // should hit even if the id is list.slider.up.button         
                    if( n.attributes != null ) {
                        results.applyNew(n.attributes);
                    }
                }
            }
        } 
                
        if( child != null ) {
            // Add any attributes we may have found specifically at this
            // level
            if( child.attributes != null ) {
                results.applyNew(child.attributes);
            }
        }
    }

    protected Node findChild( Node node, String[] parts, boolean create ) {
        for( int i = parts.length - 1; i >= 0; i-- ) {
            node = node.getChild(parts[i], create);
            if( node == null ) {
                return null;
            }
        }
        return node;                
    }
    
    protected void dump( Node node, String indent ) {
        System.out.println( indent + node + " {" );
        if( node.children != null ) {
            for( Node n : node.children.values() ) {
                dump(n, indent + "    ");
            }
        }
        System.out.println( indent + "}" );
    }

    protected void debug() {
        System.out.println( "Style tree:" );
        dump(root, "   ");
    }

    public static void main( String... args ) {
        
        Styles styles = new Styles();
        StyleTree tree = new StyleTree(styles);
        
        Attributes test1 = tree.getSelector(new ElementId("slider.up.button"), true);
        test1.set( "foo", "123" );
        test1.set( "bar", "345" );
        Attributes test2 = tree.getSelector(new ElementId("races.list"), new ElementId("up.button"), true);
        test2.set( "bar", "789" );
        test2.set( "color", "lunch" );
        Attributes test3 = tree.getSelector(new ElementId("button"), true);
        test3.set( "color", "bacon" );
        Attributes test4 = tree.getSelector(new ElementId("races.list"), new ElementId("slider.up.button"), true);
        test4.set( "bar", "override" );
        Attributes test5 = tree.getSelector(new ElementId("list"), new ElementId("up.button"), true);
        test5.set( "baz", "arrow" );
 
        // So which should take precendence:
        // silder.up.button
        // ...or...
        // races.list | up.button
        //
        // The first one is how the algorithm will choose.
        //
        // How about:
        // races.list | slider.up.button
        //
        // It _should_ take precedence over slider.up.button because
        // it's more specific.  I will need to make sure that the algorithm does
        // it though.
 
        
        tree.dump(tree.root, "");
 
        // Given the current precedence rules
        String[][] tests = { { "slider.up.button", "foo=123, bar=345, color=bacon" },
                             { "races.list.slider.up.button", "foo=123, bar=override, color=lunch, baz=arrow" },
                             { "button", "color=bacon" }
                           };
        for( int i = 0; i < tests.length; i++ ) {
            Attributes attrs = tree.getAttributes(new ElementId(tests[i][0]));
            System.out.println( "test [" + tests[i][0] + "] = " + attrs );
            System.out.println( "    should be:" + tests[i][1] );
        }                                    
    }

    protected class Node {
        private String id;
        private Attributes attributes;
        private Map<String, Node> children;
 
        public Node( String id ) {
            this.id = id;
        }
        
        public Node getChild( String childId, boolean create ) {
            if( children == null ) {
                if( !create ) {
                    return null;
                } else {
                    children = new HashMap<String, Node>();
                }
            }
            Node result = children.get(childId);
            if( result == null && create ) {
                result = new Node(childId);
                children.put(childId, result);
            }
            return result;
        }
        
        public Attributes getAttributes( boolean create ) {
            if( attributes == null && create ) {
                attributes = new Attributes(styles);
            }
            return attributes;
        }   
        
        protected Map<String, Node> getChildren() {
            return children;
        }
        
        public String toString() {
            return "Node[" + (id == null ? "*":id) + (attributes == null ? "" : (", " + attributes)) + "]";
        }
    }
}


