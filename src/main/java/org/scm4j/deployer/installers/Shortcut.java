package org.scm4j.deployer.installers;

import lombok.extern.slf4j.Slf4j;
import mslinks.ShellLink;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.scm4j.deployer.api.DeploymentResult;
import org.scm4j.deployer.api.IComponentDeployer;
import org.scm4j.deployer.api.IDeploymentContext;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.scm4j.deployer.api.DeploymentResult.FAILED;
import static org.scm4j.deployer.api.DeploymentResult.OK;

@Slf4j
public class Shortcut implements IComponentDeployer {

	private String mainArtifactName;
	private String shortcutName;
	private String image;
	private String pathToExistingFile;
	private String args;
	private String deploymentPath;
	private String workingDirectory;
	private boolean runAsAdmin;

	public Shortcut setShortcutName(String shortcutName) {
		this.shortcutName = shortcutName;
		return this;
	}

	public Shortcut setImage(String image) {
		this.image = image;
		return this;
	}

	public Shortcut setPathToExistingFile(String pathToExistingFile) {
		this.pathToExistingFile = pathToExistingFile;
		return this;
	}

	public Shortcut setWorkingDir(String workingDir) {
		this.workingDirectory = workingDir;
		return this;
	}

	public Shortcut setRunAsAdmin() {
		this.runAsAdmin = true;
		return this;
	}

	public Shortcut setArgs(String args) {
		this.args = args;
		return this;
	}

	public Shortcut setDeploymentPath(String deploymentPath) {
		this.deploymentPath = deploymentPath;
		return this;
	}

	@Override
	public DeploymentResult deploy() {
		String name = Optional.ofNullable(shortcutName).orElse(StringUtils.substringBeforeLast(mainArtifactName,
				"-"));
		ShellLink sl = ShellLink.createLink(pathToExistingFile)
				.setCMDArgs(Optional.ofNullable(args).orElse(""))
				.setName(name);
		if (image != null)
			sl.setIconLocation(image);
		if (runAsAdmin)
			sl.getHeader().getLinkFlags().setRunAsUser();
		if (workingDirectory != null)
			sl.setWorkingDir(workingDirectory);
		File dest = new File(Optional.ofNullable(deploymentPath).orElse(System.getProperty("user.home") + "/Desktop"));
		if (!dest.exists())
			dest.mkdirs();
		try {
			String destFile = dest.getPath().replace('\\', '/') + '/' + name + ".lnk";
			sl.saveTo(destFile);
		} catch (IOException e) {
			DeploymentResult dr = FAILED;
			dr.setErrorMsg(e.toString());
			return dr;
		}
		return OK;
	}

	@Override
	public DeploymentResult undeploy() {
		String name = Optional.ofNullable(shortcutName).orElse(StringUtils.substringBeforeLast(mainArtifactName,
				"-"));
		File dest = new File(Optional.ofNullable(deploymentPath).orElse(System.getProperty("user.home") + "/Desktop"));
		File shortcutFile = new File(dest, name + ".lnk");
		FileUtils.deleteQuietly(shortcutFile);
		if (shortcutFile.exists()) {
			String errorMsg = "Can't delete shortcut " + shortcutFile.getPath();
			log.warn(errorMsg);
			DeploymentResult dr = FAILED;
			dr.setErrorMsg(errorMsg);
			return dr;
		} else {
			return OK;
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
		this.mainArtifactName = StringUtils.substringBeforeLast(depCtx.getMainArtifact(), "-");
	}

}
