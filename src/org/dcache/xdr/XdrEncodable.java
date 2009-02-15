/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.dcache.xdr;

/**
 *
 * @author tigran
 */
public interface XdrEncodable {

	void encode(Xdr xdr) throws XdrException ;

}
