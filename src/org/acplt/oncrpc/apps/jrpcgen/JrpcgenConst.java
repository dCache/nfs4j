/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenConst.java,v 1.1.1.1 2003/08/13 12:03:45 haraldalbrecht Exp $
 *
 * Copyright (c) 1999, 2000
 * Lehrstuhl fuer Prozessleittechnik (PLT), RWTH Aachen
 * D-52064 Aachen, Germany.
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program (see the file COPYING.LIB for more
 * details); if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.acplt.oncrpc.apps.jrpcgen;

/**
 * The <code>JrpcgenConst</code> class represents a single constant defined
 * in an rpcgen "x"-file.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:45 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
public class JrpcgenConst {

    /**
     * Constant identifier.
     */
    public String identifier;

    /**
     * Contains value (or identifier refering to another constant) of constant.
     */
    public String value;

    /**
     * Specifies the enclosure (scope) within the identifier must be
     * addressed for a constant defined by an enumumeration.
     */
    public String enclosure;

    /**
     * Returns value as integer literal (and thus resolving identifiers
     * recursively, if necessary). This is only possible for simple
     * subsitutions, that is A is defined as B, B as C, and C as 42, thus
     * A is eventually defined as 42.
     *
     * <p>This simple kind of resolving is necessary when defining a particular
     * version of an ONC/RPC protocol. We need to be able to resolve the
     * version to an integer literal because we need to append the version
     * number to any remote procedure defined to avoid identifier clashes if
     * the same remote procedure is defined for several versions.
     *
     * @return integer literal as <code>String</code> or <code>null</code>,
     *   if the identifier could not be resolved to an integer literal.
     */
    public String resolveValue() {
        if ( value.length() > 0 ) {
            //
            // If the value is an integer literal, then we just have to
            // return it. That's it.
            //
            if ( Character.isDigit(value.charAt(0))
                 || (value.charAt(0) == '-') ) {
                return value;
            }
            //
            // It's an identifier, which we now have to resolve. First,
            // look it up in the list of global identifiers. Then recursively
            // resolve the value.
            //
            Object id = jrpcgen.globalIdentifiers.get(identifier);
            if ( (id != null)
                 && (id instanceof JrpcgenConst) ) {
                return ((JrpcgenConst) id).resolveValue();
            }
        }
        return null;
    }

    /**
     * Constructs a <code>JrpcgenConst</code> and sets the identifier and
     * the associated value.
     *
     * @param identifier Constant identifier to define.
     * @param value Value assigned to constant.
     */
    public JrpcgenConst(String identifier, String value) {
        this(identifier, value, null);
    }

    /**
     * Constructs a <code>JrpcgenConst</code> and sets the identifier and
     * the associated value of an enumeration etc.
     *
     * @param identifier Constant identifier to define.
     * @param value Value assigned to constant.
     * @param enclosure Name of enclosing enumeration, etc.
     */
    public JrpcgenConst(String identifier, String value, String enclosure) {
        this.identifier = identifier;
        this.value = value;
        this.enclosure = enclosure;
    }

    /**
     * Returns the identifier this constant depends on or <code>null</code>,
     * if no dependency exists.
     *
     * @return dependency identifier or <code>null</code>.
     */
    public String getDependencyIdentifier() {
        int len = value.length();
        int idx = 0;
        char c;

        //
        // Check to see if it's an identifier and search for its end.
        // This is necessary as elements of an enumeration might have
        // "+x" appended, where x is an integer literal.
        //
        while ( idx < len ) {
            c = value.charAt(idx++);
            if ( !(   ((c >= 'A') && (c <= 'Z'))
                   || ((c >= 'a') && (c <= 'z'))
                   || (c == '_')
                   || ((c >= '0') && (c <= '9') && (idx > 0))
                  ) )  {
                --idx; // back up to the char not belonging to the identifier.
                break;
            }
        }
        if ( idx > 0 ) {
            return value.substring(0, idx);
        }
        return null;
    }

    /**
     * Dumps the constant as well as its value to <code>System.out</code>.
     */
    public void dump() {
        System.out.println(identifier + " = " + value);
    }

    /**
     * Flag indicating whether this constant and its dependencies should be
     * traversed any more.
     */
    public boolean dontTraverseAnyMore = false;

}

// End of JrpcgenConst.java
