/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenDeclaration.java,v 1.2 2003/08/14 08:08:34 haraldalbrecht Exp $
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
 * The <code>JrpcgenDeclaration</code> class represents a single declaration
 * from an rpcgen "x"-file.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/14 08:08:34 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
public class JrpcgenDeclaration implements Cloneable {

    /**
     * Identifier.
     */
    public String identifier;

    /**
     * Type specifier.
     */
    public String type;

    /**
     * Kind of declaration (scalar, fixed size vector, dynamic vector).
     *
     * @see JrpcgenDeclaration#SCALAR
     * @see JrpcgenDeclaration#FIXEDVECTOR
     * @see JrpcgenDeclaration#DYNAMICVECTOR
     * @see JrpcgenDeclaration#INDIRECTION
     */
    public int kind;

    /**
     * Fixed size or upper limit for size of vector.
     */
    public String size;

    /**
     * Indicates that a scalar is declared.
     */
    public static final int SCALAR = 0;

    /**
     * Indicates that a vector (an array) with fixed size is declared.
     */
    public static final int FIXEDVECTOR = 1;

    /**
     * Indicates that a vector (an array) with dynamic (or unknown) size
     * is declared.
     */
    public static final int DYNAMICVECTOR = 2;

    /**
     * Indicates that an indirection (reference, pointer, whatever you like
     * to call it nowadays) is declared.
     */
    public static final int INDIRECTION = 3;

    /**
     * Returns the identifier.
     */
    public String toString() {
        return identifier;
    }

    /**
     * Constructs a <code>JrpcgenDeclaration</code> and sets the identifier
     * and its data type. The {@link JrpcgenDeclaration#kind} of the
     * declaration is assumed to be {@link JrpcgenDeclaration#SCALAR}.
     *
     * @param identifier Identifier to be declared.
     * @param type Data type the identifier is declared of.
     */
    public JrpcgenDeclaration(String identifier, String type) {
        this.identifier = identifier;
        this.type = type;
        this.kind = SCALAR;
    }

    /**
     * Constructs a <code>JrpcgenDeclaration</code> and sets the identifier,
     * its data type, kind and size of vector. This constructur is typically
     * used when declaring either fixed-size or dynamic arrays.
     *
     * @param identifier Identifier to be declared.
     * @param type Data type the identifier is declared of.
     * @param kind Kind of declaration (scalar, vector, indirection).
     * @param size Size of array (if fixed-sized, otherwise <code>null</code>).
     */
    public JrpcgenDeclaration(String identifier, String type, int kind, String size) {
        this.identifier = identifier;
        this.type = type;
        this.kind = kind;
        this.size = size;
    }

    /**
     * Dumps the declaration to <code>System.out</code>.
     */
    public void dump() {
        System.out.print(type);
        System.out.print(kind == JrpcgenDeclaration.INDIRECTION ? " *" : " ");
        System.out.print(identifier);
        switch ( kind ) {
        case JrpcgenDeclaration.FIXEDVECTOR:
            System.out.print("[" + size + "]");
            break;
        case JrpcgenDeclaration.DYNAMICVECTOR:
            if ( size != null ) {
                 System.out.print("<" + size + ">");
            } else {
                System.out.print("<>");
            }
            break;
        }
        System.out.println();
    }

    /**
     * Clones declaration object.
     */
    public Object clone()
           throws CloneNotSupportedException {
        return super.clone();
    }

}

// End of JrpcgenDeclaration.java