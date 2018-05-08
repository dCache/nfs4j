package org.dcache.nfs;

import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 */
public class ExportFileTest {

    private final Random random = new Random();

    private File exportDir;
    private File export;

    private int fileNumber = 5;

    @Before
    public void setUp() throws IOException {

        export = File.createTempFile("exports", null);
        export.deleteOnExit();

        Files.write("/export_main 1.1.1.1(sec=sys)", export, UTF_8);

        do {
            // create new unique tmp-directory
            exportDir = new File(export.getParentFile(), "exports.d" + random.nextInt(Integer.MAX_VALUE));
        } while (!exportDir.mkdir());

        exportDir.deleteOnExit();

        for (int i = 0; i < fileNumber; i++) {
            File extraExport = File.createTempFile("extra", ".exports", exportDir);
            extraExport.deleteOnExit();
            Files.write("/extra_export" + i + " 2.2.2." + i + "(sec=sys)", extraExport, UTF_8);
        }

        File hiddenExport = File.createTempFile(".hidden", ".exports", exportDir);
        hiddenExport.deleteOnExit();
        Files.write("/hidden_export *(sec=sys)", hiddenExport, UTF_8);

        File randomFile = File.createTempFile("random", null, exportDir);
        randomFile.deleteOnExit();
        Files.write("/random_file_export *(sec=sys)", randomFile, UTF_8);
    }

    @Test
    public void testMainExportFile() throws IOException {
        ExportFile ef = new ExportFile(export, exportDir);
        assertExportExists("/export_main", ef);
    }

    @Test
    public void testNoHiddenExportFile() throws IOException {
        ExportFile ef = new ExportFile(export, exportDir);
        assertExportNotExists("/hidden_export", ef);
    }

    @Test
    public void testNoRandomnExportFile() throws IOException {
        ExportFile ef = new ExportFile(export, exportDir);
        assertExportNotExists("/random_file_export", ef);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotADir() throws IOException {
        File dir = mock(File.class);
        given(dir.exists()).willReturn(true);
        given(dir.isDirectory()).willReturn(false);
        given(dir.getAbsolutePath()).willReturn("/some/existing/path");

        new ExportFile(export, dir);
    }

    @Test
    public void testDirNotExist() throws IOException {
        File dir = mock(File.class);
        given(dir.exists()).willReturn(false);

        ExportFile ef = new ExportFile(export, dir);
        assertExportExists("/export_main", ef);
    }

    @Test
    public void testDirIsNull() throws IOException {

        ExportFile ef = new ExportFile(export, null);
        assertExportExists("/export_main", ef);
    }

    @Test
    public void testExtraExports() throws IOException {

        ExportFile ef = new ExportFile(export, exportDir);

        assertExportExists("/export_main", ef);
        for (int i = 0; i < fileNumber; i++) {
            assertExportExists("/extra_export" + i, ef);
        }
    }

    @Test
    public void testAddingNewFile() throws IOException {

        ExportFile ef = new ExportFile(export, exportDir);

        assertExportNotExists("/added_export", ef);
        File addedFile = File.createTempFile("new_export", ".exports", exportDir);
        addedFile.deleteOnExit();
        Files.write("/added_export *(sec=sys)", addedFile, UTF_8);

        ef.rescan();
        assertExportExists("/added_export", ef);
    }

    private void assertExportExists(String path, ExportFile exportFile) {
        assertTrue("export " + path + " doesn't exists", exportFile.getExports()
                .filter(e -> e.getPath().equals(path))
                .findAny()
                .isPresent());
    }

    private void assertExportNotExists(String path, ExportFile exportFile) {
        assertFalse("export " + path + " exist, but shouldn't", exportFile.getExports()
                .filter(e -> e.getPath().equals(path))
                .findAny()
                .isPresent());
    }
}
