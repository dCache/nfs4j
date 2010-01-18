package org.dcache.chimera.nfs;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.dcache.chimera.nfs.ExportClient.IO;
import org.dcache.chimera.nfs.ExportClient.Root;


public class ExportFile {

    private final Map<String, FsExport> _exports ;

    /**
     * Create a new empty exports list
     */
    public ExportFile()  {
        _exports = new HashMap<String, FsExport>();
    }

    public ExportFile(File file) throws IOException  {

        _exports = parse(file);

    }


    public List<String> getExports() {

        return new ArrayList<String>(_exports.keySet());

    }

    private static Map<String, FsExport> parse(File exportFile) throws IOException {


        BufferedReader br = new BufferedReader(new FileReader(exportFile));
        Map<String, FsExport> exports = new HashMap<String, FsExport>();

        String line;
        try {
            int lineCount = 0;
            while ((line = br.readLine()) != null) {

                ++lineCount;

                line = line.trim();
                if (line.length() == 0)
                    continue;

                if (line.charAt(0) == '#')
                    continue;

                FsExport  export = null;
                StringTokenizer st = new StringTokenizer(line);
                String path = st.nextToken();

                if( st.hasMoreTokens() ) {
                    List<ExportClient> clients = new ArrayList<ExportClient>();
                    while(st.hasMoreTokens() ) {

                        String hostAndOptions = st.nextToken();
                        StringTokenizer optionsTokenizer = new StringTokenizer(hostAndOptions, "(),");

                        String host = optionsTokenizer.nextToken();
                        Root isTrusted = ExportClient.Root.NOTTRUSTED;
                        IO rw = ExportClient.IO.RO;
                        while(optionsTokenizer.hasMoreTokens()) {

                            String option = optionsTokenizer.nextToken();
                            if( option.equals("rw") ) {
                                rw = ExportClient.IO.RW;
                                continue;
                            }

                            if( option.equals("no_root_squash") ) {
                                isTrusted = ExportClient.Root.TRUSTED;
                                continue;
                            }

                        }

                        ExportClient client = new ExportClient(host,isTrusted, rw );
                        clients.add(client);

                    }
                    export  = new FsExport(path, clients);
                }else{
                    ExportClient everyOne = new ExportClient("*",ExportClient.Root.NOTTRUSTED, ExportClient.IO.RO );

                    List<ExportClient> clients = new ArrayList<ExportClient>(1);
                    clients.add(everyOne);
                    export = new FsExport(path, clients );

                }
                exports.put(path, export);

            }
        } finally {
            try {
                br.close();
            } catch (IOException dummy) {
                // ignored
            }
        }

        return exports;

    }


    public FsExport getExport(String path) {

        return _exports.get(path);

    }

    // FIXME: one trusted client has an access to all tree
    public  boolean isTrusted( java.net.InetAddress client ){


        List<String> exports = getExports();
        for( String path: exports ) {

            FsExport fsExport = getExport(path);
            if( fsExport.isTrusted(client) ) {
                return true;
            }

        }

        return false;

    }

    /**
     * add a new export to existing exports
     *
     * @param export
     */
    public void addExport( FsExport export) {
        _exports.put(export.getPath(), export);
    }

}
