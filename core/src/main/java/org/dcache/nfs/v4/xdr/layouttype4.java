/*
 * Copyright (c) 2009 - 2017 Deutsches Elektronen-Synchroton,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY
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
package org.dcache.nfs.v4.xdr;

import org.dcache.nfs.status.BadLayoutException;

/**
 * Enumeration (collection of constants).
 */
public enum layouttype4 {

    LAYOUT4_NFSV4_1_FILES {
        @Override
        public int getValue() {
            return 0x1;
        }
    },
    LAYOUT4_OSD2_OBJECTS {
        @Override
        public int getValue() {
            return 0x2;
        }
    },
    LAYOUT4_BLOCK_VOLUME {
        @Override
        public int getValue() {
            return 0x3;
        }
    },
    LAYOUT4_FLEX_FILES {
        @Override
        public int getValue() {
            return 0x4;
        }
    };

    public abstract int getValue();

    public static layouttype4 valueOf(int value) throws BadLayoutException {
        for (layouttype4 l : values()) {
            if (l.getValue() == value) {
                return l;
            }
        }

        throw new BadLayoutException("Bad layouttype: " + value);
    }
}
// End of layouttype4.java
