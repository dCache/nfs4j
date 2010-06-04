/*
 * $Header: /cvsroot/remotetea/remotetea/src/org/acplt/oncrpc/apps/jrpcgen/JrpcgenUnionArm.java,v 1.2 2003/08/14 08:09:59 haraldalbrecht Exp $
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
 * The <code>JrpcgenUnionArm</code> class represents a single union arm defined
 * for a particular union in an rpcgen "x"-file.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/14 08:09:59 $ $State: Exp $ $Locker:  $
 * @author Harald Albrecht
 */
public class JrpcgenUnionArm {

    /**
     * Value for which the descriminated union arm is valid.
     */
    public String value;

    /**
     * Attribute element of descriminated arm (of class
     * {@link JrpcgenDeclaration}).
     */
    public JrpcgenDeclaration element;

    /**
     * Constructs a <code>JrpcgenUnionArm</code> and sets decrimated arm's
     * value and the associated attribute element.
     *
     * @param value Value for which descriminated arm is valid.
     * @param element Descriminated arm element of class
     *   {@link JrpcgenDeclaration}.
     */
    public JrpcgenUnionArm(String value,
                           JrpcgenDeclaration element) {
        this.value = value;
        this.element = element;
    }

    /**
     * Dumps the union arm to <code>System.out</code>.
     */
    public void dump() {
        if ( value == null ) {
            if ( element == null ) {
                System.out.println("  default: -");
            } else if ( element.identifier != null ) {
                System.out.print("  default: ");
                element.dump();
            } else {
                System.out.println("  default: void");
            }
        } else {
            if ( element == null ) {
                System.out.println(" " + value + ": -");
            } else if ( element.identifier != null ) {
                System.out.print("  " + value + ": ");
                element.dump();
            } else {
                System.out.println("  " + value + ": void");
            }
        }
    }

}

// End of JrpcgenUnionArm.java