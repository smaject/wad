
package com.airhacks.wad.control;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author airhacks.com
 */
public class Builder {

    private final DefaultInvoker invoker;
    private final DefaultInvocationRequest request;
    private final Path builderPath;
    private boolean initialBuild;

    public Builder(Path path) {
        this.builderPath = path;
        this.invoker = new DefaultInvoker();
        this.invoker.setLogger(new SilentLogger());
        this.invoker.setOutputHandler((line) -> {
        });
        List<String> goals = Arrays.asList("clean", "install");
        Properties properties = new Properties();
        properties.put("maven.test.skip", String.valueOf(true));
        this.request = new DefaultInvocationRequest();
        this.request.setBaseDirectory(path.toFile());
        this.request.setPomFile(Paths.get(path.toString(), "pom.xml").toFile());
        this.request.setGoals(goals);
        this.request.setBatchMode(true);
        this.request.setProperties(properties);
        this.request.setThreads(System.getProperty("threads", "1"));
        this.request.setShowErrors(true);
        this.initialBuild = true;
    }

    public InvocationResult build() throws MavenInvocationException {
        System.out.println("Build '" + builderPath + "' ...");
        final InvocationResult result = this.invoker.execute(request);
        this.initialBuild = false;
        return result;
    }

    public boolean isInitialBuild() {
        return initialBuild;
    }
}
