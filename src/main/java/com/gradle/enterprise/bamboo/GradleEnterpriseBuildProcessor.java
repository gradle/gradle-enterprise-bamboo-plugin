package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.build.CustomBuildProcessor;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.CurrentResult;
import com.atlassian.bamboo.v2.build.task.AbstractBuildTask;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class GradleEnterpriseBuildProcessor extends AbstractBuildTask implements CustomBuildProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(GradleEnterpriseBuildProcessor.class);

    @NotNull
    @Override
    public BuildContext call() {
        registeredGradleEnterpriseResources()
            .stream()
            .map(File::new)
            .forEach(this::delete);

        return buildContext;
    }

    private Collection<String> registeredGradleEnterpriseResources() {
        CurrentResult currentResult = buildContext.getCurrentResult();
        if (currentResult == null) {
            return Collections.emptyList();
        }

        String resources = currentResult.getCustomBuildData().get(Constants.GE_RESOURCES_KEY);
        if (StringUtils.isBlank(resources)) {
            LOGGER.debug(
                "No registered Gradle Enterprise resources for the build: {}",
                buildContext.getPlanResultKey().getKey()
            );

            return Collections.emptyList();
        }

        return Arrays.asList(resources.split(Constants.GE_RESOURCES_SEPARATOR));
    }

    private void delete(File file) {
        if (!file.exists()) {
            // If it happens then something somewhere went wrong, and we need to fix it
            LOGGER.error("Registered Gradle Enterprise resource not found: {}", file.getAbsolutePath());

            return;
        }

        boolean deleted = FileUtils.deleteQuietly(file);
        if (!deleted) {
            LOGGER.warn("Unable to delete registered Gradle Enterprise resource: {}", file.getAbsolutePath());
        }
    }
}
