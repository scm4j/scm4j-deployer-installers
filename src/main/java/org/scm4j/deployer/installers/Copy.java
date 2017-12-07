package org.scm4j.deployer.installers;

import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.scm4j.deployer.api.DeploymentResult;
import org.scm4j.deployer.api.IComponentDeployer;
import org.scm4j.deployer.api.IDeploymentContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.scm4j.deployer.api.DeploymentResult.NEED_REBOOT;
import static org.scm4j.deployer.api.DeploymentResult.OK;

public class Copy implements IComponentDeployer {

    @Getter
    private File outputFile;
    @Getter
    private Collection<File> filesForDeploy;
    private String folderName;

    @Override
    public DeploymentResult deploy() {
        try {
            for (File file : filesForDeploy)
                FileUtils.copyDirectory(file, outputFile);
            return OK;
        } catch (IOException e) {
            return NEED_REBOOT;
        }
    }

    @Override
    public DeploymentResult undeploy() {
        try {
            for (File file : filesForDeploy) {
                File fileForUndeploy = new File(outputFile.getPath(), file.getName());
                FileUtils.forceDelete(fileForUndeploy);
            }
            return OK;
        } catch (IOException e) {
            return NEED_REBOOT;
        }
    }

    @Override
    public DeploymentResult stop() {
        return OK;
    }

    @Override
    public DeploymentResult start() {
        return OK;
    }

    @Override
    public void init(IDeploymentContext depCtx) {
        if (folderName != null)
            outputFile = new File(depCtx.getDeploymentPath(), folderName);
        else
            outputFile = new File(depCtx.getDeploymentPath());
        filesForDeploy = depCtx.getArtifacts().values();
    }

    public Copy setDefaultFolderName(String folderName) {
        this.folderName = folderName;
        return this;
    }
}