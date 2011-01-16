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

package org.dcache.chimera.nfs.v4.client;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import jline.ArgumentCompletor;
import jline.ConsoleReader;
import jline.SimpleCompletor;

import org.dcache.chimera.nfs.v4.xdr.COMPOUND4args;
import org.dcache.chimera.nfs.v4.xdr.COMPOUND4res;
import org.dcache.chimera.nfs.ChimeraNFSException;
import org.dcache.chimera.nfs.v4.NFSv41Error;
import org.dcache.chimera.nfs.v4.xdr.clientid4;
import org.dcache.chimera.nfs.v4.xdr.deviceid4;
import org.dcache.chimera.nfs.v4.xdr.entry4;
import org.dcache.chimera.nfs.v4.xdr.fattr4_fs_locations;
import org.dcache.chimera.nfs.v4.xdr.fattr4_type;
import org.dcache.chimera.nfs.v4.xdr.fs_location4;
import org.dcache.chimera.nfs.v4.xdr.fs_locations4;
import org.dcache.chimera.nfs.v4.xdr.layout4;
import org.dcache.chimera.nfs.v4.xdr.layoutiomode4;
import org.dcache.chimera.nfs.v4.xdr.layouttype4;
import org.dcache.chimera.nfs.v4.xdr.nfs4_prot;
import org.dcache.chimera.nfs.v4.xdr.nfs_argop4;
import org.dcache.chimera.nfs.v4.xdr.nfs_fh4;
import org.dcache.chimera.nfs.v4.xdr.nfs_opnum4;
import org.dcache.chimera.nfs.v4.xdr.nfsstat4;
import org.dcache.chimera.nfs.v4.xdr.nfsv4_1_file_layout4;
import org.dcache.chimera.nfs.v4.xdr.nfsv4_1_file_layout_ds_addr4;
import org.dcache.chimera.nfs.v4.xdr.sequenceid4;
import org.dcache.chimera.nfs.v4.xdr.sessionid4;
import org.dcache.chimera.nfs.v4.xdr.state_protect_how4;
import org.dcache.chimera.nfs.v4.xdr.stateid4;
import org.dcache.chimera.nfs.v4.xdr.uint32_t;
import org.dcache.chimera.nfs.v4.xdr.uint64_t;
import org.dcache.chimera.nfs.v4.xdr.utf8str_cs;
import org.dcache.chimera.nfs.v4.xdr.utf8string;
import org.dcache.chimera.nfs.v4.xdr.verifier4;
import org.dcache.chimera.posix.Stat;
import org.dcache.utils.net.InetSocketAddresses;
import org.dcache.xdr.IpProtocolType;
import org.dcache.xdr.OncRpcException;

public class Main {

    private final nfs4_prot_NFS4_PROGRAM_Client _nfsClient;
    private final Map<deviceid4, InetSocketAddress> _knowDevices = new HashMap<deviceid4, InetSocketAddress>();
    private nfs_fh4 _cwd = null;
    private nfs_fh4 _rootFh = null;
    // FIXME:
    private nfs_fh4 _ioFH = null;
    private clientid4 _clientIdByServer = null;
    private sequenceid4 _sequenceID = null;
    private sessionid4 _sessionid = null;
    private long _lastUpdate = -1;
    private final static long LEASETIME = 10 * 1000;
    private boolean _isMDS = false;
    private boolean _isDS = false;
    private static final String PROMPT = "NFSv41: ";

    public static COMPOUND4args generateCompound(String tag,
            List<nfs_argop4> opList) {

        COMPOUND4args compound4args = new COMPOUND4args();
        compound4args.tag = new utf8str_cs(new utf8string(tag.getBytes()));
        compound4args.minorversion = new uint32_t(1);

        compound4args.argarray = opList.toArray(new nfs_argop4[opList.size()]);

        return compound4args;

    }

    public static void main(String[] args) throws IOException, OncRpcException, InterruptedException {

        System.out.println("Started the NFS4 Client ....");
        String line;

        Timer timer = new Timer();
        Main nfsClient = null;

        final String[] commands = {
            "mount",
            "cd",
            "ls",
            "lookup",
            "mkdir",
            "read",
            "filebomb",
            "remove",
            "umount",
            "write",
            "fs_locations"
        };

        PrintWriter out = new PrintWriter(System.out);
        ConsoleReader reader = new ConsoleReader(System.in, out);
        reader.setUseHistory(true);
        List<SimpleCompletor> completors = new LinkedList<SimpleCompletor>();
        completors.add(new SimpleCompletor(commands));
        reader.addCompletor(new ArgumentCompletor(completors));


        if (args.length > 0) {
            String[] share = args[0].split(":");
            String host = share[0];
            String root = share.length == 2? share[1] : "/";
            nfsClient = new Main(InetAddress.getByName(share[0]));
            nfsClient.mount(root);
        }

        while ((line = reader.readLine(PROMPT)) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }

            String[] commandArgs = line.split("[ \t]+");

            if (commandArgs[0].equals("mount")) {

                String host = commandArgs.length > 1 ? commandArgs[1]
                        : "localhost";
                String root =  commandArgs.length > 2 ? commandArgs[2]
                        : "/";
                nfsClient = new Main(InetAddress.getByName(host));
                nfsClient.mount(root);

            } else if (commandArgs[0].equals("umount")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                nfsClient.umount();
                timer.purge();
                nfsClient = null;

            } else if (commandArgs[0].equals("ls")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length == 2) {
                    nfsClient.readdir(commandArgs[1]);
                } else {
                    nfsClient.readdir();
                }

            } else if (commandArgs[0].equals("cd")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: cd <path>");
                    continue;
                }
                nfsClient.cwd(commandArgs[1]);

            } else if (commandArgs[0].equals("lookup")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: lookup <path>");
                    continue;
                }
                nfsClient.lookup(commandArgs[1]);

            } else if (commandArgs[0].equals("mkdir")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: mkdir <path>");
                    continue;
                }
                nfsClient.mkdir(commandArgs[1]);

            } else if (commandArgs[0].equals("read")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: read <file>");
                    continue;
                }
                nfsClient.read(commandArgs[1]);

            } else if (commandArgs[0].equals("fs_locations")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: fs_locations <file>");
                    continue;
                }

                nfsClient.get_fs_locations(commandArgs[1]);

            } else if (commandArgs[0].equals("remove")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: remove <file>");
                    continue;
                }
                nfsClient.remove(commandArgs[1]);

            } else if (commandArgs[0].equals("write")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 3) {
                    System.out.println("usage: write <src> <dest>");
                    continue;
                }
                nfsClient.write(commandArgs[1], commandArgs[2]);

            } else if (commandArgs[0].equals("filebomb")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                if (commandArgs.length != 2) {
                    System.out.println("usage: filebomb <num>");
                    continue;
                }
                nfsClient.filebomb(Integer.parseInt(commandArgs[1]));

            } else if (commandArgs[0].equals("gc")) {

                if (nfsClient == null) {
                    System.out.println("Not mounted");
                    continue;
                }

                nfsClient.gc();

            } else if (line.equalsIgnoreCase("quit")
                    || line.equalsIgnoreCase("exit")) {

                if (nfsClient != null) {
                    nfsClient.destroy_session();
                }
                timer.purge();
                System.exit(0);
            } else {
                out.println("Supported commands: ");
                for (String command : commands) {
                    out.println("    " + command);
                }
            }
            out.flush();

            timer.schedule(new LeasUpdater(nfsClient), LEASETIME, LEASETIME);

        }
    }

    /**
     * generate set of files and delete them after words
     * @param string
     * @param string2
     * @throws IOException
     * @throws OncRpcException
     */
    private void filebomb(int count) throws OncRpcException, IOException {

        List<String> files = new ArrayList<String>(count);
        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < count; i++) {
                String file = UUID.randomUUID().toString();
                write("/etc/profile", file);
                files.add(file);
            }
        } finally {
            for (String file : files) {
                System.out.println("Remove: " + file);
                remove(file);
            }
            System.out.println(count + " files in " + (System.currentTimeMillis() - start) / 1000);
        }

    }

    private void gc() throws OncRpcException, IOException {

        exchange_id();
        create_session();
        sequence();

    }

    private boolean needUpdate() {
        // 60 seconds
        return System.currentTimeMillis() - _lastUpdate > 60000;
    }

    private static class LeasUpdater extends TimerTask {

        private final Main _nfsClient;

        LeasUpdater(Main nfsClient) {
            _nfsClient = nfsClient;
        }

        @Override
        public void run() {
            try {
                if (_nfsClient.needUpdate()) {
                    _nfsClient.sequence();
                }
            } catch (OncRpcException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public Main(InetAddress host) throws OncRpcException, IOException {
        _nfsClient = new nfs4_prot_NFS4_PROGRAM_Client(host, IpProtocolType.TCP);

        _servers.put(_nfsClient.getTransport().getRemoteSocketAddress(), this);

    }

    public Main(InetSocketAddress address) throws OncRpcException,
            IOException {
        _nfsClient = new nfs4_prot_NFS4_PROGRAM_Client(address.getAddress(),
                address.getPort(), IpProtocolType.TCP);

        _servers.put(address, this);
    }

    public void mount(String root) throws OncRpcException, IOException {
        exchange_id();
        create_session();

        getRootFh(root);
        get_supported_attributes();
        if (_isMDS) {
            get_devicelist();
        }

        _lastUpdate = System.currentTimeMillis();

    }

    public void dsMount() throws OncRpcException, IOException {
        exchange_id();
        create_session();
        _lastUpdate = System.currentTimeMillis();
    }

    public void umount() throws OncRpcException, IOException {
        destroy_session();
    }

    private void exchange_id() throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        String domain = "nairi.desy.de";
        String name = "dCache.ORG java based client";

        ops.add(ExchangeIDStub.normal(domain, name, UUID.randomUUID().toString(), 0, state_protect_how4.SP4_NONE));

        COMPOUND4res compound4res = sendCompound(ops, "exchange_id");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            if (compound4res.resarray[0].opexchange_id.eir_resok4.eir_server_impl_id.length > 0) {
                String serverId = new String(
                        compound4res.resarray[0].opexchange_id.eir_resok4.eir_server_impl_id[0].nii_name.value.value);
                System.out.println("Connected to: " + serverId);
            } else {
                System.out.println("Connected to: Mr. X");
            }

            _clientIdByServer = compound4res.resarray[0].opexchange_id.eir_resok4.eir_clientid;
            _sequenceID = compound4res.resarray[0].opexchange_id.eir_resok4.eir_sequenceid;

            if ((compound4res.resarray[0].opexchange_id.eir_resok4.eir_flags.value
                    & nfs4_prot.EXCHGID4_FLAG_USE_PNFS_MDS) > 0) {
                _isMDS = true;
            }

            if ((compound4res.resarray[0].opexchange_id.eir_resok4.eir_flags.value
                    & nfs4_prot.EXCHGID4_FLAG_USE_PNFS_DS) > 0) {
                _isDS = true;
            }

            System.out.println("pNFS MDS: " + _isMDS);
            System.out.println("pNFS  DS: " + _isDS);

        } else {
            System.out.println("exchangeId failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private void create_session() throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(CreateSessionStub.standard(_clientIdByServer, _sequenceID));

        COMPOUND4res compound4res = sendCompound(ops, "create_session");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            _sessionid = compound4res.resarray[0].opcreate_session.csr_resok4.csr_sessionid;
            // FIXME: no idea why, but other wise server reply MISORDER
            _sequenceID.value.value = 0;

        } else {
            System.out.println("create session failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private void destroy_session() throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(DestroySessionStub.standard(_sessionid));
        @SuppressWarnings("unused")
        COMPOUND4res compound4res = sendCompound(ops, "destroy_session");
        _nfsClient.close();

    }

    private void getRootFh(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutrootfhStub.generateRequest());
        String[] pathElements = path.split("/");
        for (String p : pathElements) {
            if (p != null && p.length() > 0) {
                ops.add(LookupStub.generateRequest(p));
            }
        }
        ops.add(GetfhStub.generateRequest());

        COMPOUND4res compound4res = sendCompound(ops, "get_root_fh");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            _rootFh = compound4res.resarray[ops.size() - 1].opgetfh.resok4.object;
            _cwd = _rootFh;
            System.out.println("root fh = " + toHexString(_rootFh.value));

        } else {
            System.out.println("getRootFh failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    public void readdir() throws OncRpcException, IOException {

        try {
            for (String entry : list(_cwd)) {
                System.out.println(entry);
            }
        } catch (Exception e) {
            System.out.println("readdir: " + e.getMessage());
        }
    }

    public void readdir(String path) throws OncRpcException, IOException {

        try {
            for (String entry : list(_cwd, path)) {
                System.out.println(entry);
            }
        } catch (Exception e) {
            System.out.println("readdir: " + e.getMessage());
        }
    }

    public String[] list(nfs_fh4 fh) throws OncRpcException, IOException, ChimeraNFSException {

        boolean done = false;
        List<String> list = new ArrayList<String>();
        long cookie = 0;
        verifier4 verifier = new verifier4(new byte[nfs4_prot.NFS4_VERIFIER_SIZE]);

        do {

            List<nfs_argop4> ops = new LinkedList<nfs_argop4>();
            ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                    _sequenceID.value.value, 12, 0));
            ops.add(PutfhStub.generateRequest(fh));
            ops.add(ReaddirStub.generateRequest(cookie, verifier));

            COMPOUND4res compound4res = sendCompound(ops, "readdir");

            if (compound4res.status == nfsstat4.NFS4_OK) {
                verifier = compound4res.resarray[2].opreaddir.resok4.cookieverf;
                done = compound4res.resarray[2].opreaddir.resok4.reply.eof;

                entry4 dirEntry = compound4res.resarray[2].opreaddir.resok4.reply.entries;
                while (dirEntry != null) {
                    cookie = dirEntry.cookie.value.value;
                    list.add(new String(dirEntry.name.value.value.value));
                    dirEntry = dirEntry.nextentry;
                }

            } else {
                System.out.println("readdir failed. Error = "
                        + NFSv41Error.errcode2string(compound4res.status));
                done = true;
                throw new ChimeraNFSException(compound4res.status, NFSv41Error.errcode2string(compound4res.status));
            }

        } while (!done);


        return list.toArray(new String[list.size()]);

    }

    public String[] list(nfs_fh4 fh, String path) throws OncRpcException, IOException, ChimeraNFSException {

        boolean done = false;
        List<String> list = new ArrayList<String>();
        long cookie = 0;
        verifier4 verifier = new verifier4(new byte[nfs4_prot.NFS4_VERIFIER_SIZE]);

        do {

            List<nfs_argop4> ops = new LinkedList<nfs_argop4>();
            ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                    _sequenceID.value.value, 12, 0));

            if (path.charAt(0) == '/') {
                ops.add(PutfhStub.generateRequest(_rootFh));
            } else {
                ops.add(PutfhStub.generateRequest(fh));
            }

            String[] pathElements = path.split("/");
            for (String p : pathElements) {

                if (p != null && p.length() > 0) {
                    if (p.equals("..")) {
                        ops.add(LookuppStub.generateRequest());
                    } else {
                        ops.add(LookupStub.generateRequest(p));
                    }
                }
            }

            ops.add(ReaddirStub.generateRequest(cookie, verifier));

            COMPOUND4res compound4res = sendCompound(ops, "readdir");

            int opCount = ops.size();

            if (compound4res.status == nfsstat4.NFS4_OK) {

                verifier = compound4res.resarray[opCount - 1].opreaddir.resok4.cookieverf;
                done = compound4res.resarray[opCount - 1].opreaddir.resok4.reply.eof;

                entry4 dirEntry = compound4res.resarray[opCount - 1].opreaddir.resok4.reply.entries;
                while (dirEntry != null) {
                    cookie = dirEntry.cookie.value.value;
                    list.add(new String(dirEntry.name.value.value.value));
                    dirEntry = dirEntry.nextentry;
                }

            } else {
                System.out.println("readdir failed. Error = "
                        + NFSv41Error.errcode2string(compound4res.status));
                done = true;
                throw new ChimeraNFSException(compound4res.status, NFSv41Error.errcode2string(compound4res.status));
            }

        } while (!done);


        return list.toArray(new String[list.size()]);

    }

    private void mkdir(String path) throws OncRpcException, IOException {


        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        ops.add(PutfhStub.generateRequest(_cwd));
        ops.add(SavefhStub.generateRequest());
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_CHANGE));
        ops.add(MkdirStub.generateRequest(path));
        ops.add(RestorefhStub.generateRequest());
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_CHANGE));
        ops.add(LookuppStub.generateRequest());
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_CHANGE));

        COMPOUND4res compound4res = sendCompound(ops, "mkdir");

        if (compound4res.status != nfsstat4.NFS4_OK) {
            System.out.println("mkdir failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private void get_fs_locations(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(_cwd));
        ops.add(LookupStub.generateRequest(path));
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_FS_LOCATIONS));

        COMPOUND4res compound4res = sendCompound(ops, "get_fs_locations");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            Map<Integer, Object> attrMap = GetattrStub.decodeType(compound4res.resarray[ops.size()-1].opgetattr.resok4.obj_attributes);

            fattr4_fs_locations locations = (fattr4_fs_locations) attrMap.get(nfs4_prot.FATTR4_FS_LOCATIONS);
            if (locations != null) {
                System.out.println("fs_locations fs_root: " + locations.value.fs_root.value[0].value.toString());
                System.out.println("fs_locations locations rootpath: " + locations.value.locations[0].rootpath.value[0].value.toString());
                System.out.println("fs_locations locations server: " + new String(locations.value.locations[0].server[0].value.value));

            }

        } else {
            System.out.println("get_fs_locations failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    nfs_fh4 cwd(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        if (path.charAt(0) == '/') {
            ops.add(PutfhStub.generateRequest(_rootFh));
        } else {
            ops.add(PutfhStub.generateRequest(_cwd));
        }

        String[] pathElements = path.split("/");
        for (String p : pathElements) {
            if (p != null && p.length() > 0) {
                if (p.equals("..")) {
                    ops.add(LookuppStub.generateRequest());
                } else {
                    ops.add(LookupStub.generateRequest(p));
                }
            }
        }
        ops.add(GetfhStub.generateRequest());

        COMPOUND4res compound4res = sendCompound(ops, "lookup (cwd)");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            _cwd = compound4res.resarray[ops.size() - 1].opgetfh.resok4.object;
            System.out.println("CWD fh = " + toHexString(_cwd.value));

        } else {
            System.out.println("cwd failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

        return new nfs_fh4(_cwd.value);
    }

    public Stat stat(nfs_fh4 fh) throws OncRpcException, IOException {


        Stat stat = new Stat();

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(fh));
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_SIZE,nfs4_prot.FATTR4_TYPE));

        COMPOUND4res compound4res = sendCompound(ops, "getattr (stat)");

        if (compound4res.status == nfsstat4.NFS4_OK) {


            Map<Integer, Object> attrMap = GetattrStub.decodeType(compound4res.resarray[2].opgetattr.resok4.obj_attributes);

            uint64_t size = (uint64_t) attrMap.get(nfs4_prot.FATTR4_SIZE);
            if (size != null) {
                stat.setSize(size.value);
            }

            fattr4_type type = (fattr4_type) attrMap.get(nfs4_prot.FATTR4_TYPE);

            System.out.println("Type is: " + type.value);




        } else {
            System.out.println("getAttr failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }


        return stat;
    }

    private void read(String path) throws OncRpcException, IOException {

        OpenReply or = open(path);

        if (_isMDS) {
            StripeMap stripeMap = layoutget(or.fh(), or.stateid(), layoutiomode4.LAYOUTIOMODE4_READ);

            List<Stripe> stripes = stripeMap.getStripe(0, 4096);
            Stripe stripe = stripes.get(0);
            deviceid4 device = stripe.getDeviceId();
            InetSocketAddress deviceAddr = _knowDevices.get(device);
            Main dsClient = _servers.get(deviceAddr);

            dsClient.dsRead(dsClient, stripe.getFh(), or.stateid());

            layoutreturn(0, -1, new byte[0], or.stateid());

        } else {
            nfsRead(or.fh(), or.stateid());
        }
        close(or.fh(), or.stateid());

    }

    private void write(String source, String path) throws OncRpcException, IOException {

        File f = new File(source);
        if (!f.exists()) {
            System.out.println("file not found: " + f);
        }

        OpenReply or = create(path);

        if (_isMDS) {

            StripeMap stripeMap = layoutget(or.fh(), or.stateid(), layoutiomode4.LAYOUTIOMODE4_RW);

            RandomAccessFile raf = null;
            try {

                raf = new RandomAccessFile(source, "r");
                byte[] data = new byte[4096];
                long offset = 0;
                while (true) {

                    int n = raf.read(data);
                    if (n == -1) {
                        break;
                    }

                    /* we got less than 4K wipe the tail */
                    if (n < data.length) {
                        byte[] b = new byte[n];
                        System.arraycopy(data, 0, b, 0, n);
                    }

                    List<Stripe> stripes = stripeMap.getStripe(offset, 4096);
                    Stripe stripe = stripes.get(0);
                    deviceid4 device = stripe.getDeviceId();
                    InetSocketAddress deviceAddr = _knowDevices.get(device);
                    Main dsClient = _servers.get(deviceAddr);

                    dsClient.dsWrite(dsClient, stripe.getFh(), data, offset, or.stateid());
                    offset += n;
                }

            } catch (IOException ie) {
                System.out.println("Write failed: " + ie.getMessage());
            } finally {
                if (raf != null) {
                    raf.close();
                }
                layoutreturn(0, -1, new byte[0], or.stateid());
            }

        } else {
            // not a pNFS server
            nfsWrite(or.fh(), or.stateid());
        }
        close(or.fh(), or.stateid());
    }

    private OpenReply open(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));


        if (path.charAt(0) == '/') {
            ops.add(PutfhStub.generateRequest(_rootFh));
        } else {
            ops.add(PutfhStub.generateRequest(_cwd));
        }

        String[] pathElements = path.split("/");
        for (int i = 0; i < pathElements.length - 1; i++) {
            String p = pathElements[i];
            if (p != null && p.length() > 0) {
                if (p.equals("..")) {
                    ops.add(LookuppStub.generateRequest());
                } else {
                    ops.add(LookupStub.generateRequest(p));
                }
            }
        }

        ops.add(OpenStub.normalREAD(pathElements[pathElements.length - 1], _sequenceID.value.value,
                _clientIdByServer, nfs4_prot.OPEN4_SHARE_ACCESS_READ));
        ops.add(GetfhStub.generateRequest());

        COMPOUND4res compound4res = sendCompound(ops, "open_read");

        int opCount = ops.size();

        if (compound4res.status == nfsstat4.NFS4_OK) {

            nfs_fh4 fh = compound4res.resarray[opCount - 1].opgetfh.resok4.object;
            stateid4 stateid = compound4res.resarray[opCount - 2].opopen.resok4.stateid;
            System.out.println("open_read fh = " + toHexString(fh.value));

            return new OpenReply(fh, stateid);

        } else {
            System.out.println("open failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

        return null;

    }

    private OpenReply create(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        if (path.charAt(0) == '/') {
            ops.add(PutfhStub.generateRequest(_rootFh));
        } else {
            ops.add(PutfhStub.generateRequest(_cwd));
        }

        String[] pathElements = path.split("/");
        for (int i = 0; i < pathElements.length - 1; i++) {
            String p = pathElements[i];
            if (p != null && p.length() > 0) {
                if (p.equals("..")) {
                    ops.add(LookuppStub.generateRequest());
                } else {
                    ops.add(LookupStub.generateRequest(p));
                }
            }
        }

        ops.add(OpenStub.normalCREATE(pathElements[pathElements.length - 1], _sequenceID.value.value,
                _clientIdByServer, nfs4_prot.OPEN4_SHARE_ACCESS_BOTH));
        ops.add(GetfhStub.generateRequest());

        COMPOUND4res compound4res = sendCompound(ops, "open_create");

        int opCount = ops.size();

        if (compound4res.status == nfsstat4.NFS4_OK) {

            nfs_fh4 fh = compound4res.resarray[opCount - 1].opgetfh.resok4.object;
            stateid4 stateid = compound4res.resarray[opCount - 2].opopen.resok4.stateid;
            System.out.println("open_read fh = " + toHexString(fh.value));

            return new OpenReply(fh, stateid);

        } else {
            System.out.println("open failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

        return null;

    }

    private void close(nfs_fh4 fh, stateid4 stateid) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        ops.add(PutfhStub.generateRequest(fh));
        ops.add(CloseStub.generateRequest(stateid));

        COMPOUND4res compound4res = sendCompound(ops, "close");

        if (compound4res.status != nfsstat4.NFS4_OK) {

            System.out.println("close failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private StripeMap layoutget(nfs_fh4 fh, stateid4 stateid, int layoutiomode) throws OncRpcException,
            IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(fh));
        ops.add(LayoutgetStub.generateRequest(false,
                layouttype4.LAYOUT4_NFSV4_1_FILES,
                layoutiomode, 0, 0xffffffff, 0xff, 0xffff,
                stateid));

        COMPOUND4res compound4res = sendCompound(ops, "layoutget");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            layout4[] layout = compound4res.resarray[2].oplayoutget.logr_resok4.logr_layout;
            System.out.println("Layoutget for fh: " + toHexString(fh.value));
            System.out.println("    roc   : " + compound4res.resarray[2].oplayoutget.logr_resok4.logr_return_on_close);

            StripeMap stripeMap = new StripeMap();

            for (layout4 l : layout) {
                nfsv4_1_file_layout4 fileDevice = LayoutgetStub.decodeLayoutId(l.lo_content.loc_body);
                System.out.println("       sd # "
                        + Arrays.toString(fileDevice.nfl_deviceid.value) + " size "
                        + fileDevice.nfl_deviceid.value.length);

                _ioFH = fileDevice.nfl_fh_list[0];
                System.out.println("     io fh: " + toHexString(_ioFH.value));
                System.out.println("    length: " + l.lo_length.value.value);
                System.out.println("    offset: " + l.lo_offset.value.value);
                System.out.println("    type  : " + l.lo_content.loc_type);
                System.out.println("    unit  : " + fileDevice.nfl_util.value.value);

                deviceid4 deviceID = fileDevice.nfl_deviceid;
                Stripe stripe = new Stripe(deviceID, fileDevice.nfl_fh_list[0], l.lo_length.value.value, l.lo_offset.value.value);
                stripeMap.addStripe(stripe);

                if (!_knowDevices.containsKey(deviceID)) {
                    System.out.println("    new: true");
                    get_deviceinfo(deviceID);
                } else {
                    System.out.println("    new: false");
                }
                InetSocketAddress address = _knowDevices.get(deviceID);
                if (address == null) {
                    System.out.println("    address: failed to get");
                } else {
                    System.out.println("    address: " + address.getHostName()
                            + ":" + address.getPort());
                }

                return stripeMap;

            }

        } else {
            System.out.println("layoutget failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

        return null;

    }

    private void layoutreturn(long offset, long len, byte[] body, stateid4 stateid) throws OncRpcException,
            IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(LayoutreturnStub.generateRequest(offset, len, body, stateid));

        COMPOUND4res compound4res = sendCompound(ops, "layoutreturn");

        if (compound4res.status != nfsstat4.NFS4_OK) {
            System.out.println("layoutreturn failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private COMPOUND4res sendCompound(List<nfs_argop4> ops, String tag)
            throws OncRpcException, IOException {

        COMPOUND4res compound4res;
        COMPOUND4args compound4args = generateCompound(tag, ops);
        /*
         * wail if server is in the grace period.
         *
         * TODO: escape if it takes too long
         */
        do {
            compound4res = _nfsClient.NFSPROC4_COMPOUND_4(compound4args);
            processSequence(compound4res);
            if (compound4res.status == nfsstat4.NFS4ERR_GRACE) {
                System.out.println("Server in GRACE period....retry");
            }
        } while (compound4res.status == nfsstat4.NFS4ERR_GRACE);

        return compound4res;
    }

    private void get_deviceinfo(deviceid4 deviceId) throws OncRpcException,
            IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(GetdeviceinfoStub.generateRequest(deviceId));

        COMPOUND4res compound4res = sendCompound(ops, "get_deviceinfo");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            nfsv4_1_file_layout_ds_addr4 addr = GetDeviceListStub.decodeFileDevice(compound4res.resarray[1].opgetdeviceinfo.gdir_resok4.gdir_device_addr.da_addr_body);

            InetSocketAddress inetAddr = InetSocketAddresses.forUaddrString(
                    addr.nflda_multipath_ds_list[0].value[0].na_r_addr);

            _knowDevices.put(deviceId, inetAddr);

        } else {
            System.out.println("getdeviceinfo failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }
    }

    private void get_devicelist() throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(_rootFh));
        ops.add(GetDeviceListStub.normal());

        COMPOUND4res compound4res = sendCompound(ops, "get_devicelist");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            deviceid4[] deviceList = compound4res.resarray[2].opgetdevicelist.gdlr_resok4.gdlr_deviceid_list;

            System.out.println("Know devices: ");
            for (deviceid4 device : deviceList) {
                System.out.println("      Device: # " + Arrays.toString(device.value));
            }

        } else {
            System.out.println("get_devicelist failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
            _isMDS = false;
            _isDS = false;
        }

    }

    private void dsRead(Main client, nfs_fh4 fh, stateid4 stateid)
            throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        ops.add(PutfhStub.generateRequest(fh));
        ops.add(ReadStub.generateRequest(4096, 0, stateid));

        COMPOUND4res compound4res = sendCompound(ops, "pNFS read");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            byte[] data = new byte[compound4res.resarray[2].opread.resok4.data.remaining()];
            compound4res.resarray[2].opread.resok4.data.get(data);
            System.out.println("[" + new String(data) + "]");
        } else {
            System.out.println("read failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private void nfsRead(nfs_fh4 fh, stateid4 stateid) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        ops.add(PutfhStub.generateRequest(fh));
        ops.add(ReadStub.generateRequest(4096, 0, stateid));

        COMPOUND4res compound4res = sendCompound(ops, "nfs read");

        if (compound4res.status == nfsstat4.NFS4_OK) {

            byte[] data = new byte[compound4res.resarray[2].opread.resok4.data.remaining()];
            compound4res.resarray[2].opread.resok4.data.get(data);
            System.out.println("[" + new String(data) + "]");
        } else {
            System.out.println("read failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private void dsWrite(Main client, nfs_fh4 fh, byte[] data, long offset, stateid4 stateid)
            throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(fh));

        ops.add(WriteStub.generateRequest(offset, data, stateid));

        COMPOUND4res compound4res = sendCompound(ops, "pNFS write");

        if (compound4res.status != nfsstat4.NFS4_OK) {
            throw new IOException(NFSv41Error.errcode2string(compound4res.status));
        }


        // OK
    }

    private void nfsWrite(nfs_fh4 fh, stateid4 stateid) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(fh));

        byte[] data = "hello".getBytes();
        ops.add(WriteStub.generateRequest(0, data, stateid));

        COMPOUND4res compound4res = sendCompound(ops, "nfs write");

        if (compound4res.status != nfsstat4.NFS4_OK) {
            System.out.println("write failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        } else {

            System.out.println(compound4res.resarray[2].opwrite.resok4.count.value.value
                    + " bytes written.");
        }

    }

    private void sequence() throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        COMPOUND4res compound4res = sendCompound(ops, "sequence");

        if (compound4res.status != nfsstat4.NFS4_OK) {

            System.out.println("sequence failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));

        } else {
            // ok
        }
    }

    private void get_supported_attributes() throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(_rootFh));
        ops.add(GetfhStub.generateRequest());
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_CHANGE));

        COMPOUND4res compound4res = sendCompound(ops, "get_supported_attributes");

        if (compound4res.status == nfsstat4.NFS4_OK) {
            //    uint32_t supported = compound4res.resarray[1].opgetattr.resok4.obj_attributes.attrmask.value[0];
            //    System.out.println(supported);
            // TODO:
        } else {
            System.out.println("get_supported_attributes failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    public void remove(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));
        ops.add(PutfhStub.generateRequest(_cwd));
        ops.add(RemoveStub.generateRequest(path));

        COMPOUND4res compound4res = sendCompound(ops, "remove");

        if (compound4res.status == nfsstat4.NFS4_OK) {
            // ok
        } else {
            System.out.println("remove failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    private void lookup(String path) throws OncRpcException, IOException {

        List<nfs_argop4> ops = new LinkedList<nfs_argop4>();

        ops.add(SequenceStub.generateRequest(false, _sessionid.value,
                _sequenceID.value.value, 12, 0));

        ops.add(PutfhStub.generateRequest(_cwd));
        ops.add(SavefhStub.generateRequest());
        ops.add(LookupStub.generateRequest(path));
        ops.add(GetfhStub.generateRequest());
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_CHANGE,
                nfs4_prot.FATTR4_SIZE, nfs4_prot.FATTR4_TIME_MODIFY));
        ops.add(RestorefhStub.generateRequest());
        ops.add(GetattrStub.generateRequest(nfs4_prot.FATTR4_CHANGE,
                nfs4_prot.FATTR4_SIZE, nfs4_prot.FATTR4_TIME_MODIFY));

        COMPOUND4res compound4res = sendCompound(ops, "lookup-sun");

        if (compound4res.status == nfsstat4.NFS4_OK) {
            // ok
        } else {
            System.out.println("lookup-sun failed. Error = "
                    + NFSv41Error.errcode2string(compound4res.status));
        }

    }

    public void processSequence(COMPOUND4res compound4res) {

        if (compound4res.resarray[0].resop == nfs_opnum4.OP_SEQUENCE && compound4res.resarray[0].opsequence.sr_status == nfsstat4.NFS4_OK) {
            _lastUpdate = System.currentTimeMillis();
            ++_sequenceID.value.value;
        }
    }

    private static class OpenReply {

        private final nfs_fh4 _fh;
        private final stateid4 _stateid;

        private OpenReply(nfs_fh4 fh, stateid4 stateid) {
            _stateid = stateid;
            _fh = fh;
        }

        nfs_fh4 fh() {
            return _fh;
        }

        stateid4 stateid() {
            return _stateid;
        }
    }

    public String toHexString(byte[] data) {

        StringBuilder sb = new StringBuilder();

        for (byte b : data) {
            sb.append(Integer.toHexString(b));
        }

        return sb.toString();
    }
    private final ConcurrentMap<InetSocketAddress, Main> _servers =
            new MapMaker().makeComputingMap(new Connector());

    private static class Connector implements Function<InetSocketAddress, Main> {

        @Override
        public Main apply(InetSocketAddress f) {
            try {
                Main client = new Main(f);
                client.dsMount();
                return client;
            } catch (Exception e) {
                return null;
            }
        }
    }

}
