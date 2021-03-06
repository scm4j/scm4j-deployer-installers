package org.scm4j.deployer.installers;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.scm4j.deployer.api.DeploymentResult;
import org.scm4j.deployer.api.IComponentDeployer;
import org.scm4j.deployer.api.IDeploymentContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.scm4j.deployer.api.DeploymentResult.OK;
import static org.scm4j.deployer.api.DeploymentResult.REBOOT_CONTINUE;

@Slf4j
public class Copy implements IComponentDeployer {

	@Getter
	private File outputFile;
	@Getter
	private Collection<File> filesForDeploy;
	private String fileName;
	private String folderName;

	@Override
	public DeploymentResult deploy() {
		try {
			for (File file : filesForDeploy) {
				if (file.isDirectory())
					FileUtils.copyDirectoryToDirectory(file, outputFile);
				else if (filesForDeploy.size() == 1 && fileName != null)
					FileUtils.copyFile(file, new File(outputFile, fileName));
				else
					FileUtils.copyFileToDirectory(file, outputFile);
			}
			return OK;
		} catch (IOException e) {
			log.warn(e.toString());
			DeploymentResult dr = REBOOT_CONTINUE;
			dr.setErrorMsg(e.toString());
			return dr;
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
			log.warn(e.toString());
			DeploymentResult dr = REBOOT_CONTINUE;
			dr.setErrorMsg(e.toString());
			return dr;
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
		if (outputFile == null) {
			if (folderName != null)
				outputFile = new File(depCtx.getDeploymentPath(), folderName);
			else
				outputFile = new File(depCtx.getDeploymentPath());
		}
		filesForDeploy = depCtx.getArtifacts().values();
	}

	public Copy setDefaultFolderName(String folderName) {
		this.folderName = folderName;
		return this;
	}

	public Copy setFullPathToOutputFolder(String fullPath) {
		this.outputFile = new File(fullPath);
		return this;
	}

	public Copy setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}
}
