package org.dcache.chimera.nfs;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.dcache.chimera.nfs.ExportClient.Root;

public class FsExport {

    private final String _path;
    private final List<ExportClient> _clients = new ArrayList<ExportClient>();

    /**
     * NFS clients may be specified in a number of ways:<br>
     * <p>
     *
     * <b>single host</b>
     * <p>
     * This is the most common format. You may specify a host either by an
     * abbreviated name recognized be the resolver, the fully qualified domain
     * name, or an IP address.
     * <p>
     *
     * <b>wildcards</b>
     * <p>
     * Machine names may contain the wildcard characters * and ?. This can be
     * used to make the exports file more compact; for instance, .cs.foo.edu
     * matches all hosts in the domain cs.foo.edu. As these characters also
     * match the dots in a domain name, the given pattern will also match all
     * hosts within any subdomain of cs.foo.edu.
     * <p>
     *
     * <b>IP networks</b>
     * <p>
     * You can also export directories to all hosts on an IP (sub-) network
     * simultaneously. This is done by specifying an IP address and netmask pair
     * as address/netmask where the netmask can be specified in dotted-decimal
     * format, or as a contiguous mask length (for example, either
     * `/255.255.252.0' or `/22' appended to the network base address result in
     * identical subnetworks with 10 bits of host). Wildcard characters
     * generally do not work on IP addresses, though they may work by accident
     * when reverse DNS lookups fail.
     * <p>
     *
     *
     * @param path
     * @param clients list of {@link ExportClient} which allowed to mount this export.
     */
    public FsExport(String path, List<ExportClient> clients) {

        _path = path;
        _clients.addAll(clients);

    }

    public String getPath() {
        return _path;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(_path).append(":");

        if (_clients.isEmpty()) {
            sb.append(" *");
        } else {
            for (ExportClient client : _clients) {
                sb.append(" ").append(client.ip()).append("(").append(
                        client.io()).append(",").append(client.trusted())
                        .append(")");
            }
        }

        return sb.toString();

    }

    public boolean isAllowed(InetAddress client) {

        // localhost always allowed
        if( client.isLoopbackAddress() ) {
            return true;
        }else{

            for (ExportClient exportClient : _clients) {
                if(  IPMatcher.match(exportClient.ip(), client) ) {
                    return true;
                }
            }

        }

        return false;
    }

    public boolean isTrusted(InetAddress client) {

        // localhost always allowed
        if( client.isLoopbackAddress() ) {
            return true;
        }else{

            for (ExportClient exportClient : _clients) {
                if( exportClient.trusted() == Root.TRUSTED && IPMatcher.match(exportClient.ip(), client)) {
                    return true;
                }
            }

        }

        return false;
    }

    public List<String> client() {
        List<String> client = new ArrayList<String>(_clients.size());

        for (ExportClient exportClient : _clients) {
            client.add(exportClient.ip());
        }

        return client;
    }
}
