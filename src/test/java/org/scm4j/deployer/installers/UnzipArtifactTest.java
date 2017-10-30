package org.scm4j.deployer.installers;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scm4j.deployer.api.DeploymentContext;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

public class UnzipArtifactTest {

    private static final File ZIP_FOLDER = new File(System.getProperty("java.io.tmpdir"), "test-zip");
    private static final File ZIP_FILE = new File(ZIP_FOLDER, "file.zip");
    private DeploymentContext depCtx;
    private Map<String, Object> params;

    @Before
    public void setUp() throws Exception {
        ZIP_FOLDER.mkdirs();
        ZIP_FILE.createNewFile();
        depCtx = new DeploymentContext(ZIP_FILE.getName());
        depCtx.setDeploymentURL(new URL("file://" + ZIP_FOLDER.getPath()));
        Map<String, File> artifacts = new HashMap<>();
        artifacts.put(ZIP_FILE.getName(), ZIP_FILE);
        depCtx.setArtifacts(artifacts);
        String str = "Test String";
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(ZIP_FILE));
        ZipEntry entry = new ZipEntry("mytext.txt");
        out.putNextEntry(entry);
        byte[] data = str.getBytes();
        out.write(data, 0, data.length);
        out.closeEntry();
        out.close();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(ZIP_FOLDER);
    }

    @Test
    public void testDeploy() throws Exception {
        UnzipArtifact unzipArtifact = new UnzipArtifact();
        unzipArtifact.init(depCtx, params);
        unzipArtifact.deploy();
        assertEquals(FileUtils.readFileToString(new File(ZIP_FOLDER, "mytext.txt"),"UTF-8"), "Test String");
    }

    @Test
    public void testInit() throws Exception {
        UnzipArtifact unzipArtifact = new UnzipArtifact();
        unzipArtifact.init(depCtx, params);
        assertEquals(unzipArtifact.getZipFile(), depCtx.getArtifacts().get(ZIP_FILE.getName()));
        assertTrue(FileUtils.contentEquals(unzipArtifact.getZipFile(), depCtx.getArtifacts().get(ZIP_FILE.getName())));
        assertEquals(unzipArtifact.getOutputFile(), new File(depCtx.getDeploymentURL().getFile()));
    }
}
