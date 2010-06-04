/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenUnion.java,v 1.1.1.1 2003/08/13 12:03:47 haraldalbrecht Exp $
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

import java.util.Vector;

/**
 * The <code>JrpcgenUnion</code> class represents a single union defined
 * in an rpcgen "x"-file.
 *
 * @version $Revision: 1.1.1.1 $ $Date: 2003/08/13 12:03:47 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
public class JrpcgenUnion {

    /**
     * Union identifier.
     */
    public String identifier;

    /**
     * {@link JrpcgenDeclaration} of descriminant element (containing its
     * identifier and data type).
     */
    public JrpcgenDeclaration descriminant;

    /**
     * Contains arms of union. The arms are of class
     * {@link JrpcgenDeclaration}. The keys are the descriminant values.
     */
    public Vector elements;

    /**
     * Returns just the identifier.
     */
    public String toString() {
        return identifier;
    }

    /**
     * Constructs a <code>JrpcgenUnion</code> and sets the identifier, the
     * descrimant element as well as all attribute elements.
     *
     * @param identifier Identifier to be declared.
     * @param descriminant Descriminant element of class
     *   {@link JrpcgenDeclaration}.
     * @param elements Vector of atrribute elements of class
     *   {@link JrpcgenDeclaration}.
     */
    public JrpcgenUnion(String identifier, JrpcgenDeclaration descriminant,
                        Vector elements) {
        this.identifier = identifier;
        this.descriminant = descriminant;
        this.elements = elements;
    }

    /**
     * Dumps the union together with its attribute elements end the
     * descriminant to <code>System.out</code>.
     */
    public void dump() {
        System.out.println("UNION " + identifier + ":");
        System.out.println("  switch (" + descriminant.type + " " + descriminant.identifier + ")");

        int size = elements.size();
        for ( int idx = 0; idx < size; ++idx ) {
            JrpcgenUnionArm a = (JrpcgenUnionArm) elements.elementAt(idx);
            System.out.print("  ");
            a.dump();
        }
        System.out.println();
    }

}

// End of JrpcgenUnion.java