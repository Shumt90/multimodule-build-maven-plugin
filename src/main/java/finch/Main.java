package finch;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "check", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class Main extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(defaultValue = "${project.artifactId}")
    private String artifactId;

    @Parameter(property = "rootArtifact", required = true)
    private String rootArtifact;

    @Parameter(property = "nextGoals", required = true)
    private String nextGoals;

    @Parameter(defaultValue = "${basedir}")
    protected File baseDir;

    public void execute() throws MojoFailureException {

        getLog().info("hash dir: " + outputDirectory.getAbsolutePath());
        getLog().info("base dir: " + baseDir.getAbsolutePath());

        Path oldHash = Path.of(outputDirectory.getAbsolutePath(), artifactId + "-" + nextGoals + ".txt");
        Path newHash = Path.of(outputDirectory.getAbsolutePath(), artifactId + "-" + nextGoals + "-new.txt");

        try {
            if (!Files.exists(outputDirectory.toPath()))
                Files.createDirectories(outputDirectory.toPath());
        } catch (IOException e) {
            throw new MojoFailureException(String.format("Can't create directory %s", outputDirectory), e);
        }
        if (rootArtifact.equals(artifactId)) {
            runCommand();
        } else {
            getLog().info("Calculating hash sum ...");
            Hash hash = new Hash(baseDir, newHash.toFile());
            hash.scanDirectory();
            try {
                if (Files.exists(oldHash) && FileUtils.contentEquals(oldHash.toFile(), newHash.toFile())) {
                    getLog().info(String.format("Module %s is not modified", artifactId));
                } else {
                    runCommand();
                    FileUtils.copyFile(newHash.toFile(), oldHash.toFile());
                }
            } catch (IOException e) {
                throw new MojoFailureException(String.format("Can't compare or copy files: %s, %s", oldHash, newHash), e);
            }
        }

    }

    private void runCommand() throws MojoFailureException {
        try {
            final StringBufferStreamConsumer out = new StringBufferStreamConsumer(true);

            final CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            Commandline commandline = new Commandline("cd " + baseDir.getAbsolutePath() + " && mvn -N " + nextGoals);

            getLog().info(String.format("try run: %s", commandline));

            final int exitCode = CommandLineUtils.executeCommandLine(commandline, out, err);
            String errorStr = err.getOutput();
            String outStr = out.getOutput();

            if (exitCode != 0) {
                // not all commands print errors to error stream
                if (StringUtils.isBlank(errorStr) && StringUtils.isNotBlank(outStr)) {
                    errorStr = outStr;
                }

                throw new MojoFailureException(errorStr);
            }


        } catch (CommandLineException e) {
            throw new MojoFailureException(String.format("Can't run: %s", nextGoals), e);
        }
    }
}
