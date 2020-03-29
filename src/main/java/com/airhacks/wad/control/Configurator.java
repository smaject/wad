
package com.airhacks.wad.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author airhacks.com
 */
public interface Configurator {

    String WAD_CONFIGURATION_FILE = ".wadrc";
    String WAD_KEY_DEPLOYMENT_DIR = "deploymentDir=";
    String WAD_KEY_DEPENDENT_MODULE = "depModule=";
    String WAD_KEY_WAR_FILE_NAME = "warFile=";

    static Set<Path> getConfiguredDeploymentFolders(List<Path> commandLineArguments) {
        Set<Path> deploymentFolders = getConfigurationFromUserDirectory(WAD_KEY_DEPLOYMENT_DIR);
        deploymentFolders.addAll(getPathConfigurationFromModuleDirectory(WAD_KEY_DEPLOYMENT_DIR));
        deploymentFolders.forEach(f -> System.out
                .printf("%s \'%s\' %s from .wadrc\n", TerminalColors.FILE.value(), f, TerminalColors.RESET.value()));
        commandLineArguments.forEach(f -> System.out
                .printf("command line argument %s \'%s\' %s\n", TerminalColors.FILE.value(), f,
                        TerminalColors.RESET.value()));
        deploymentFolders.addAll(commandLineArguments);
        System.out.println("resulting deployment folders are:");
        deploymentFolders.forEach(
                f -> System.out.printf("%s \'%s\' %s\n", TerminalColors.FILE.value(), f, TerminalColors.RESET.value()));
        return deploymentFolders;

    }

    static Set<String> getStringConfigurationFromDirectory(Path pathToConfiguration, String configKey) {
        try {
            return Files.readAllLines(pathToConfiguration).
                    stream().
                    filter(l -> l.startsWith(configKey)).
                    map(String::trim).
                    map(l -> l.substring(configKey.length())).
                    map(Substitutor::substitute).
                    collect(Collectors.toSet());
        } catch (IOException ex) {
            return new HashSet<>();
        }
    }

    static Set<Path> getPathConfigurationFromDirectory(Path pathToConfiguration, String configKey) {
        return getStringConfigurationFromDirectory(pathToConfiguration, configKey).
                stream().
                map(Paths::get).
                collect(Collectors.toSet());
    }

    static Set<Path> getConfigurationFromUserDirectory(String configKey) {
        return getPathConfigurationFromDirectory(getUserHomeValue(), configKey);
    }

    static boolean userConfigurationExists() {
        return userConfigurationExists(getUserHomeValue());
    }

    static Path getUserHomeValue() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, WAD_CONFIGURATION_FILE);

    }

    static Path getModuleDirectory() {
        return Paths.get("").toAbsolutePath();
    }

    static Path getModuleDirectoryValue() {
        return Paths.get(getModuleDirectory().toString(), WAD_CONFIGURATION_FILE);
    }

    static boolean userConfigurationExists(Path pathToConfiguration) {
        return Files.exists(pathToConfiguration);
    }

    static boolean moduleConfigurationExists() {
        return Files.exists(getModuleDirectoryValue());
    }

    static Set<Path> getPathConfigurationFromModuleDirectory(String configKey) {
        return getPathConfigurationFromDirectory(getModuleDirectoryValue(), configKey);
    }

    static Set<Path> getDependentModules() {
        return getPathConfigurationFromModuleDirectory(WAD_KEY_DEPENDENT_MODULE)
                .stream()
                .map(Configurator::getAbsolutePath)
                .collect(Collectors.toSet());
    }

    static Path getAbsolutePath(Path path) {
        if (path.startsWith(".") || path.startsWith("..")) {
            return Paths.get(getModuleDirectory().toString(), path.toString());
        }
        return path;
    }

    static String getWarFileName() {
        final Set<String> configWarFileName =
                getStringConfigurationFromDirectory(getModuleDirectoryValue(), WAD_KEY_WAR_FILE_NAME);
        if (!configWarFileName.isEmpty()) {
            return configWarFileName.iterator().next();
        }
        final Path moduleDirectory = getModuleDirectory();
        return moduleDirectory.getFileName() + ".war";
    }
}
