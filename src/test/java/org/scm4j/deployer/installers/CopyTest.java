package org.scm4j.deployer.installers;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scm4j.deployer.api.DeploymentContext;
import org.scm4j.deployer.api.DeploymentResult;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CopyTest {

    private static final File TEST_FOLDER = new File(System.getProperty("java.io.tmpdir"), "test-copy");
    private static final File FOLDER_FOR_COPY = new File(TEST_FOLDER, "file");
    private static final File OUTPUT_FOLDER = new File(TEST_FOLDER, "output");
    private DeploymentContext depCtx;

    @Before
    public void setUp() throws Exception {
        FOLDER_FOR_COPY.mkdirs();
        depCtx = new DeploymentContext(FOLDER_FOR_COPY.getName());
        depCtx.setDeploymentPath(OUTPUT_FOLDER.getPath());
        Map<String, File> artifacts = new HashMap<>();
        artifacts.put(FOLDER_FOR_COPY.getName(), FOLDER_FOR_COPY);
        depCtx.setArtifacts(artifacts);
        for (int i = 0; i < 5; i++) {
            File file = new File(FOLDER_FOR_COPY, String.valueOf(i));
            file.mkdir();
            File file1 = new File(file, String.valueOf(i) + ".txt");
            file1.createNewFile();
            FileUtils.writeStringToFile(file1, "hello" + String.valueOf(i), "UTF-8");
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(TEST_FOLDER);
    }

    @Test
    public void testDeploy() throws Exception {
        Copy copy = new Copy();
        copy.init(depCtx);
        copy.deploy();
        for (int i = 0; i < 5; i++)
            assertTrue(FileUtils.contentEquals(new File(FOLDER_FOR_COPY, String.valueOf(i) + ".txt"),
                    new File(OUTPUT_FOLDER, String.valueOf(i) + ".txt")));
    }

    @Test
    public void testFail() throws Exception {
        Copy copy = new Copy();
        copy.init(depCtx);
        for (File file : copy.getFilesForDeploy()) {
            FileUtils.forceDelete(file);
        }
        DeploymentResult res = copy.deploy();
        assertEquals(DeploymentResult.NEED_REBOOT, res);
    }

    @Test
    public void testInit() throws Exception {
        Copy copy = new Copy();
        copy.init(depCtx);
        assertEquals(copy.getFilesForDeploy(), depCtx.getArtifacts().values());
        assertEquals(copy.getOutputFile(), new File(depCtx.getDeploymentPath()));
    }

    @Test
    public void testInitSetPath() throws Exception {
        Copy copy = new Copy();
        copy.setDefaultFolderName("hello");
        copy.init(depCtx);
        assertEquals(new File(OUTPUT_FOLDER, "hello"), copy.getOutputFile());
    }
}