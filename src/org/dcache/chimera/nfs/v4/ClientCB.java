/*
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

package org.dcache.chimera.nfs.v4;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.dcache.utils.net.InetSocketAddresses;

class ClientCB {

	
	private InetAddress _addr = null;
	private int _port = 0;
	private String _type = null;	
	private int _program = 0;
	private int _version = 0;
	
	// TODO: make it nicer and faster
	ClientCB(String address, String type, int program) throws UnknownHostException {
		String[] cb_addr = address.trim().split("[.]");
		_type = type;
						
		byte[] addr = new byte[4];
		addr[0] = Integer.valueOf(cb_addr[0]).byteValue();
		addr[1] = Integer.valueOf(cb_addr[1]).byteValue();
		addr[2] = Integer.valueOf(cb_addr[2]).byteValue();
		addr[3] = Integer.valueOf(cb_addr[3]).byteValue();
		
		_addr = InetAddress.getByAddress(addr);
		
        StringBuilder sb = new StringBuilder();
		
		Integer p1 = new Integer(cb_addr[4]);
		Integer p2 = new Integer(cb_addr[5]);
		sb.append( Integer.toHexString(p1.intValue()) ).append(Integer.toHexString(p2.intValue()));
		_port = Integer.valueOf( sb.toString(), 16 );
		_program = program;
	}
	
	
	public int port() {
		return _port;
	}
	
	public int program() {
		return _program;
	}
	
	public int version() {
		return _version;
	}
	
	public InetAddress address() {
		return _addr;
	}
	
	public int protocol() {			
		// TODO: parse type
		return 6; //TCP
	}	
	
	public String toString() {
		return _type + "://" +_addr.getHostAddress() + ":" + _port + "/" + _program;
	}
	
	
    public static void main(String args[]) {

        try {
            System.out.println(new ClientCB("127.0.0.2.4.63", "tcp", 1005));
            System.out.println(new ClientCB(InetSocketAddresses.uaddrOf("127.0.0.2", 1087), "tcp", 1005));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
	
}

/*
 * $Log: ClientCB.java,v $
 * Revision 1.8  2006/12/08 15:03:24  tigran
 * catch only defined exceptions
 *
 * Revision 1.7  2006/09/14 14:40:43  tigran
 * added CB_LAYOUTRECALL
 * recall layout from the client on close
 *
 * Revision 1.6  2006/09/12 17:00:36  tigran
 * sync with linux clinet repository
 *
 * Revision 1.5  2006/08/17 09:18:27  tigran
 * cosmetic change
 *
 * Revision 1.4  2006/07/03 14:25:56  tigran
 * added draft03
 * added getdevicelist
 * abopted .x file to linux implementation
 *
 * Revision 1.3  2006/03/29 10:28:56  tigran
 * replaces StringBuffer by StringBuilder
 *
 * Revision 1.2  2006/03/27 11:48:50  tigran
 * fixed string to InetAddress conversion
 *
 * Revision 1.1  2006/03/26 22:09:05  tigran
 * added dummy callback
 *
 */

