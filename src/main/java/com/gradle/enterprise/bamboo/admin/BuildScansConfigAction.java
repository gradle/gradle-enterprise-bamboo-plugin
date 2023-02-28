package com.gradle.enterprise.bamboo.admin;

import com.atlassian.bamboo.configuration.GlobalAdminAction;
import com.gradle.enterprise.bamboo.config.PersistentConfiguration;
import com.gradle.enterprise.bamboo.config.PersistentConfigurationManager;
import com.gradle.enterprise.bamboo.config.UsernameAndPasswordCredentialsProvider;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class BuildScansConfigAction extends GlobalAdminAction {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?(-[-\\w]+)?$");

    /* Common parameters for all build systems */
    private String server;
    private boolean allowUntrustedServer;
    private String sharedCredentialName;

    /* Gradle specific parameters */
    private String gePluginVersion;
    private String ccudPluginVersion;
    private String pluginRepository;

    /* Maven specific parameters */
    private boolean injectMavenExtension;
    private boolean injectCcudExtension;

    private final UsernameAndPasswordCredentialsProvider credentialsProvider;
    private final PersistentConfigurationManager configurationManager;

    public BuildScansConfigAction(UsernameAndPasswordCredentialsProvider credentialsProvider,
                                  PersistentConfigurationManager configurationManager) {
        this.credentialsProvider = credentialsProvider;
        this.configurationManager = configurationManager;
    }

    public String input() {
        configurationManager.load()
            .ifPresent(config -> {
                server = config.getServer();
                allowUntrustedServer = config.isAllowUntrustedServer();
                sharedCredentialName = config.getSharedCredentialName();
                gePluginVersion = config.getGePluginVersion();
                ccudPluginVersion = config.getCcudPluginVersion();
                pluginRepository = config.getPluginRepository();
                injectMavenExtension = config.isInjectMavenExtension();
                injectCcudExtension = config.isInjectCcudExtension();
            });

        return INPUT;
    }

    @Override
    public void validate() {
        clearErrorsAndMessages();

        if (!isBlankOrValidUrl(server)) {
            addFieldError("server", "Please specify a valid URL of the Gradle Enterprise server.");
        }

        if (!isBlankOrExistingSharedCredential(sharedCredentialName)) {
            addFieldError("sharedCredentialName", "Please specify the name of the existing shared credential of type 'Username and password'.");
        }

        if (!isBlankOrValidVersion(gePluginVersion)) {
            addFieldError("gePluginVersion", "Please specify a valid version of the Gradle Enterprise Gradle plugin.");
        }

        if (!isBlankOrValidVersion(ccudPluginVersion)) {
            addFieldError("ccudPluginVersion", "Please specify a valid version of the Common Custom User Data Gradle plugin.");
        }

        if (!isBlankOrValidUrl(pluginRepository)) {
            addFieldError("pluginRepository", "Please specify a valid URL of the Gradle plugins repository.");
        }
    }

    private static boolean isBlankOrValidUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return true;
        }
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private static boolean isBlankOrValidVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return true;
        }
        return VERSION_PATTERN.matcher(version).matches();
    }

    private boolean isBlankOrExistingSharedCredential(String name) {
        if (StringUtils.isBlank(name)) {
            return true;
        }
        return credentialsProvider.exists(name);
    }

    public String save() {
        configurationManager.save(
            new PersistentConfiguration()
                .setServer(server)
                .setAllowUntrustedServer(allowUntrustedServer)
                .setSharedCredentialName(sharedCredentialName)
                .setPluginRepository(pluginRepository)
                .setGePluginVersion(gePluginVersion)
                .setCcudPluginVersion(ccudPluginVersion)
                .setInjectMavenExtension(injectMavenExtension)
                .setInjectCcudExtension(injectCcudExtension));

        return SUCCESS;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public boolean isAllowUntrustedServer() {
        return allowUntrustedServer;
    }

    public void setAllowUntrustedServer(boolean allowUntrustedServer) {
        this.allowUntrustedServer = allowUntrustedServer;
    }

    public String getSharedCredentialName() {
        return sharedCredentialName;
    }

    public void setSharedCredentialName(String sharedCredentialName) {
        this.sharedCredentialName = sharedCredentialName;
    }

    public String getGePluginVersion() {
        return gePluginVersion;
    }

    public void setGePluginVersion(String gePluginVersion) {
        this.gePluginVersion = gePluginVersion;
    }

    public String getCcudPluginVersion() {
        return ccudPluginVersion;
    }

    public void setCcudPluginVersion(String ccudPluginVersion) {
        this.ccudPluginVersion = ccudPluginVersion;
    }

    public String getPluginRepository() {
        return pluginRepository;
    }

    public void setPluginRepository(String pluginRepository) {
        this.pluginRepository = pluginRepository;
    }

    public boolean isInjectMavenExtension() {
        return injectMavenExtension;
    }

    public void setInjectMavenExtension(boolean injectMavenExtension) {
        this.injectMavenExtension = injectMavenExtension;
    }

    public boolean isInjectCcudExtension() {
        return injectCcudExtension;
    }

    public void setInjectCcudExtension(boolean injectCcudExtension) {
        this.injectCcudExtension = injectCcudExtension;
    }
}
