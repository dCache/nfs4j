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

package org.dcache.xdr;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

public class SpringRunner {

    private SpringRunner() {
        // this class it used only to bootstrap the Spring IoC
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        if( args.length != 1 ) {
            System.err.println("Usage: SpringRunner <config>");
            System.exit(1);
        }
        BeanFactory factory = new XmlBeanFactory(new FileSystemResource(args[0]));
        OncRpcSvc service = (OncRpcSvc)factory.getBean("oncrpcsvc");
        service.start();

    }
}
