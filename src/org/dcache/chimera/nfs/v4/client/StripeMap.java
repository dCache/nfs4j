package org.dcache.chimera.nfs.v4.client;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Provides mapping between file blocks and layouts.
 *
 */
public class StripeMap {

    /**
     * List off all know stripes.
     */
    private final List<Stripe> _fileStripeLsit = new ArrayList<Stripe>();

    /**
     * Get list of stripes for the range.
     * @param offset
     * @param len
     * @return list of stripes or empty list if the is no stripes found.
     */
    public List<Stripe> getStripe(long offset , long len) {

        List<Stripe> ioStripe = new LinkedList<Stripe>();

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

}
