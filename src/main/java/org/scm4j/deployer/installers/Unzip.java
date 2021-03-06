package org.scm4j.deployer.installers;

import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.scm4j.deployer.api.DeploymentResult;
import org.scm4j.deployer.api.IComponentDeployer;
import org.scm4j.deployer.api.IDeploymentContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.scm4j.deployer.api.DeploymentResult.REBOOT_CONTINUE;

@Slf4j
public class Unzip implements IComponentDeployer {

	@Getter
	private File outputDir;
	@Getter
	private File zipFileName;
	private String folderName;

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
			log.warn(e.getMessage());
			DeploymentResult dr = REBOOT_CONTINUE;
			dr.setErrorMsg(e.toString());
			return dr;
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
			log.warn(e.getMessage());
			DeploymentResult dr = REBOOT_CONTINUE;
			dr.setErrorMsg(e.toString());
			return dr;
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
		if (folderName != null)
			this.outputDir = new File(depCtx.getDeploymentPath(), folderName);
		else
			this.outputDir = new File(depCtx.getDeploymentPath());
		this.zipFileName = depCtx.getArtifacts().get(depCtx.getMainArtifact());
		try (ZipFile zipFile = new ZipFile(this.zipFileName)) {
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException("Not a zip file!", e);
		}
	}

	public Unzip setDefaultFolderName(String folderName) {
		this.folderName = folderName;
		return this;
	}
}
