/*
 * Copyright (c) 2009 - 2012 Deutsches Elektronen-Synchroton,
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
package org.dcache.nfs.v4.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.dcache.nfs.v4.xdr.stateid4;

/**
 *
 * Provides mapping between file blocks and layouts.
 *
 */
public class StripeMap {

    private stateid4 _stateid;
    public StripeMap(stateid4 stateid) {
        _stateid = stateid;
    }
    /**
     * List off all know stripes.
     */
    private final List<Stripe> _fileStripeLsit = new ArrayList<>();

    /**
     * Get list of stripes for the range.
     * @param offset
     * @param len
     * @return list of stripes or empty list if the is no stripes found.
     */
    public List<Stripe> getStripe(long offset , long len) {

        List<Stripe> ioStripe = new LinkedList<>();

        for(Stripe stripe: _fileStripeLsit) {

            if( stripe.getOffset() + stripe.getLen() >= offset ) {
                ioStripe.add(stripe);

                if( stripe.getOffset() + stripe.getLen() >= offset + len ) {
                    break;
                }
            }
        }

        return ioStripe;
    }

    public void addStripe(Stripe stripe) {
        _fileStripeLsit.add(stripe);
    }

    public stateid4 getStateid() {
        return _stateid;
    }
}
