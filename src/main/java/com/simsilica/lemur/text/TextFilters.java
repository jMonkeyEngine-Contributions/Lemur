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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;


/**
 *  A collection of convenience filters for input and output.
 *
 *  @author    Paul Speed
 */
public class TextFilters {
 
    private static IsDigit IS_DIGIT = new IsDigit();
    private static IsLetter IS_LETTER = new IsLetter();
    private static Function<Character, Character> ALPHA = charFilter(IS_LETTER);
    private static Function<Character, Character> NUMERIC = charFilter(IS_DIGIT);
    private static Function<Character, Character> ALPHA_NUMERIC = charFilter(Predicates.or(IS_DIGIT, IS_LETTER));
    private static ToLowerCase TO_LOWER_CASE = new ToLowerCase(); 
    private static ToUpperCase TO_UPPER_CASE = new ToUpperCase(); 
    
    /**
     *  A text output transform that replaces all characters in the string (including
     *  non-line feed whitespace) with the specified character.  This is useful
     *  for fields that must obscure the real data like password fields. 
     */
    public static Function<String, String> constantTransform( char c ) {
        return new ConstantOutputTransform(c);
    }
 
    /**
     *  A text output transform that converts all characters to upper case in
     *  the output string.
     */
    public static Function<String, String> upperCaseTransform() {
        return new CharOutputTransform(TO_UPPER_CASE);
    }
 
    /**
     *  A text output transform that converts all characters to lower case in
     *  the output string.
     */
    public static Function<String, String> lowerCaseTransform() {
        return new CharOutputTransform(TO_LOWER_CASE);
    }
    
    /** 
     *  A text output transform that passes all characters through a character
     *  filter when passing them to the output string.  Note: any character
     *  filters that skip characters will cause the standard DocumentModelFilter
     *  carat position to be inaccruate... ie: the user will see their cursor
     *  in the wrong place.  For standard DocumentModelFilter usage, make sure
     *  the supplied filter always returns something for every character.
     */
    public static Function<String, String> charOutputTransform( Function<Character, Character> transform ) {
        return new CharOutputTransform(transform);
    }
    
    /**
     *  A character filter that only allows numeric digits.
     */
    public static Function<Character, Character> alpha() {
        return ALPHA;
    }

    /**
     *  A character filter that only allows numeric digits.
     */
    public static Function<Character, Character> numeric() {
        return NUMERIC;
    }

    /**
     *  A character filter that only allows numeric digits.
     */
    public static Function<Character, Character> alphaNumeric() {
        return ALPHA_NUMERIC;
    }
    
    /**
     *  A character filter that skips characters that do not pass the
     *  specified predicate.
     */
    public static Function<Character, Character> charFilter( Predicate<Character> predicate ) {
        return new CharFilter(predicate);
    }
 
    /**
     *  A character filter that converts all passed characters to upper case
     *  using Character.toUpperCase().
     */
    public static Function<Character, Character> toUpperCase() {
        return TO_UPPER_CASE;
    }

    /**
     *  A character filter that converts all passed characters to upper case
     *  using Character.toUpperCase().
     */
    public static Function<Character, Character> toLowerCase() {
        return TO_LOWER_CASE;
    }
    
    /**
     *  Returns a predicate that returns true for letter characters as
     *  is passed to Character.isLetter().
     */
    public static Predicate<Character> isLetter() {
        return IS_LETTER;
    }

    /**
     *  Returns a predicate that returns true for numeric digit characters as
     *  is passed to Character.isDigit().
     */
    public static Predicate<Character> isDigit() {
        return IS_DIGIT;
    }
 
    /**
     *  Returns a predicate that returns true for alpha or numeric characters
     *  is in Character.isLetterOrDigit().
     */
    public static Predicate<Character> isLetterOrDigit() {
        return Predicates.or(isLetter(), isDigit());
    }
    
    /**
     *  Returns a predicate that returns true for any character in the specified
     *  list of characters.
     */
    public static Predicate<Character> isInChars( char... chars ) {
        return new IsInChars(chars);
    }
    
    private static class ConstantOutputTransform implements Function<String, String> {
        
        private char output;
        
        public ConstantOutputTransform( char output ) {
            this.output = output;
        }
        
        public String apply( String input ) {
            if( input == null ) {
                return null;
            }
            if( input.length() == 0 ) {
                return input;
            }
            StringBuilder result = new StringBuilder();
            for( int i = 0; i < input.length(); i++ ) {
                char c = input.charAt(i);
                if( c == '\r' || c == '\n' ) {
                    result.append(c);
                } else {
                    result.append(output);
                }
            }
            return result.toString(); 
        } 
    }

    private static class CharOutputTransform implements Function<String, String> {
        
        private Function<Character, Character> transform;
        
        public CharOutputTransform( Function<Character, Character> transform ) {
            this.transform = transform;
        }
        
        public String apply( String input ) {
            if( input == null ) {
                return null;
            }
            if( input.length() == 0 ) {
                return input;
            }
            StringBuilder result = new StringBuilder();
            for( int i = 0; i < input.length(); i++ ) {
                Character c = input.charAt(i);
                if( c != null ) {
                    result.append(transform.apply(c));
                }
            }
            return result.toString(); 
        } 
    }
    
    private static class CharFilter implements Function<Character, Character> {
        
        private Predicate<Character> predicate;
    
        public CharFilter( Predicate<Character> predicate ) {
            this.predicate = predicate;
        }
        
        public Character apply( Character c ) {
            return predicate.apply(c) ? c : null;
        } 
    }
 
    private static class ToUpperCase implements Function<Character, Character> {
        public Character apply( Character c ) {
            return Character.toUpperCase(c);
        }       
    }
     
    private static class ToLowerCase implements Function<Character, Character> {
        public Character apply( Character c ) {
            return Character.toLowerCase(c);
        }
    } 
    
    private static class IsDigit implements Predicate<Character> {
        public boolean apply( Character c ) {
            return Character.isDigit(c);
        }
    }
       
    private static class IsLetter implements Predicate<Character> {
        public boolean apply( Character c ) {
            return Character.isLetter(c);
        }
    }
    
    private static class IsInChars implements Predicate<Character> {
        private char[] chars;
        
        public IsInChars( char[] chars ) {
            this.chars = chars;
        }
    
        public boolean apply( Character c ) {
            for( char check : chars ) {
                if( c.charValue() == check ) {
                    return true;
                }
            }
            return false;
        }
    }   
}
