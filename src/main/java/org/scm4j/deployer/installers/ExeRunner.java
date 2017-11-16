package org.scm4j.deployer.installers;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.scm4j.deployer.api.DeploymentResult;
import org.scm4j.deployer.api.IComponentDeployer;
import org.scm4j.deployer.api.IDeploymentContext;
import org.scm4j.deployer.installers.exception.EParamsNotFoundException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ExeRunner implements IComponentDeployer {

    @Getter
    private String mainArtifact;
    @Getter
    private File outputDir;
    @Getter
    private File defaultExecutable;
    @Getter
    private File undeployExecutable;
    @Getter
    private File stopExecutable;
    @Getter
    private String undeployCmd;
    @Getter
    private String deployCmd;
    @Deprecated
    private Map<String, Object> params;

    ProcessBuilder getBuilder(String str, File executableFile) {
        if (null == executableFile) {
            executableFile = defaultExecutable;
        }
        String deploymentPath = "$deploymentPath";
        ProcessBuilder builder = new ProcessBuilder();
        List<String> cmds = new ArrayList<>();
        cmds.add("cmd");
        cmds.add("/c");
        builder.directory(executableFile.getParentFile());
        cmds.add(executableFile.getName());
        Arrays.stream(str.split("\\s(?=/)"))
                .filter(cmd -> !cmd.equals(""))
                .map(cmd -> StringUtils.replace(cmd, deploymentPath, outputDir.toString()))
                .map(cmd -> StringUtils.replace(cmd, "\\", "/"))
                .forEach(cmds::add);
        builder.command(cmds);
        return builder;
    }

    ProcessBuilder getBuilder(String str) {
        return getBuilder(str, null);
    }

    @SneakyThrows
    private DeploymentResult executeCommand(String param, File executableFile) {
        Process p = getBuilder(param, executableFile).start();
        int code = p.waitFor();
        return code == 0 ? DeploymentResult.OK : code == 1 || code == 777 ? DeploymentResult.NEED_REBOOT : DeploymentResult.FAILED;
    }

    @SneakyThrows
    private DeploymentResult executeCommand(String param) {
        return executeCommand(param, null);
    }

    @Override
    @SneakyThrows
    public DeploymentResult deploy() {
        if (deployCmd != null)
            return executeCommand(deployCmd);
        else
            throw new EParamsNotFoundException("Can't find command line for this operation ");
    }

    @Override
    @SneakyThrows
    public DeploymentResult undeploy() {
        if (undeployCmd != null && undeployExecutable != null)
            return executeCommand(undeployCmd, undeployExecutable);
        else
            throw new EParamsNotFoundException(String.format("Can't find params for this operation cmd = %s executable= %s",
                    undeployCmd, undeployExecutable));
    }

    @Override
    @SneakyThrows
    public DeploymentResult stop() {
        return DeploymentResult.OK;
    }

    @Override
    @SneakyThrows
    public DeploymentResult start() {
        return DeploymentResult.OK;
    }

    @Override
    public void init(IDeploymentContext depCtx) {
        mainArtifact = depCtx.getMainArtifact();
        outputDir = new File(depCtx.getDeploymentURL().getPath());
        defaultExecutable = depCtx.getArtifacts().get(mainArtifact);
    }

    @Override
    @Deprecated
    public void init(IDeploymentContext depCtx, Map<String, Object> params) {
        this.params = params;
        outputDir = new File(depCtx.getDeploymentURL().getPath());
        defaultExecutable = depCtx.getArtifacts().get(depCtx.getMainArtifact());
        mainArtifact = depCtx.getMainArtifact();
    }

    public ExeRunner setUndeployCmd(String cmd) {
        this.undeployCmd = cmd;
        return this;
    }

    public ExeRunner setDeployCmd(String cmd) {
        this.deployCmd = cmd;
        return this;
    }

    public ExeRunner setStopExecutableName(String executable) {
        this.stopExecutable = new File(outputDir, executable);
        return this;
    }

    public ExeRunner setUndeployExecutableName(String executable) {
        this.undeployExecutable = new File(outputDir, executable);
        return this;
    }

    @Override
    public String toString() {
        return "ExeRunner{product=" + defaultExecutable.getName() + '}';
    }
}
