package org.scm4j.deployer.installers;

import lombok.Cleanup;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.scm4j.deployer.api.DeploymentResult;
import org.scm4j.deployer.api.IComponentDeployer;
import org.scm4j.deployer.api.IDeploymentContext;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


@Data
public class Unzip implements IComponentDeployer {

    private File outputDir;
    private File zipFileName;

    @Override
    public DeploymentResult deploy() {
        try {
            @Cleanup
            ZipFile zipFile = new ZipFile(zipFileName);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    @Cleanup
                    InputStream in = zipFile.getInputStream(entry);
                    @Cleanup
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                }
            }
            return DeploymentResult.OK;
        } catch (IOException e) {
            return DeploymentResult.NEED_REBOOT;
        }
    }

    @Override
    public DeploymentResult undeploy() {
        try {
            @Cleanup
            ZipFile zipFile = new ZipFile(zipFileName);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File entryDestination = new File(outputDir, entry.getName());
                if (entryDestination.exists())
                    FileUtils.forceDelete(entryDestination);
            }
            return DeploymentResult.OK;
        } catch (IOException e) {
            return DeploymentResult.NEED_REBOOT;
        }
    }

    @Override
    public DeploymentResult stop() {
        return DeploymentResult.OK;
    }

    @Override
    public DeploymentResult start() {
        return DeploymentResult.OK;
    }

    @Override
    public void init(IDeploymentContext depCtx) {
        this.outputDir = new File(depCtx.getDeploymentPath());
        this.zipFileName = depCtx.getArtifacts().get(depCtx.getMainArtifact());
        try (ZipFile zipFile = new ZipFile(this.zipFileName)) {
        } catch (IOException e) {
            throw new RuntimeException("Not a zip file!", e);
        }
    }
}
