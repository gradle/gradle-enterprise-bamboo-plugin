package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.task.runtime.RuntimeTaskDefinition;
import com.atlassian.bamboo.util.BambooIterables;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.variable.VariableDefinitionContext;
import com.gradle.enterprise.bamboo.utils.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class GradleEnterpriseAccessKeyExporter {

    private final List<EnvironmentVariableSetter> environmentVariableSetters;

    @Autowired
    public GradleEnterpriseAccessKeyExporter(List<EnvironmentVariableSetter> environmentVariableSetters) {
        this.environmentVariableSetters = Collections.sortedByOrder(environmentVariableSetters);
    }

    public void exportGradleEnterpriseAccessKey(BuildContext buildContext,
                                                Collection<RuntimeTaskDefinition> tasks) {
        BambooIterables.stream(buildContext.getVariableContext().getPasswordVariables())
            .filter(this::isGradleEnterpriseAccessKey)
            .findFirst()
            .map(VariableDefinitionContext::getValue)
            .ifPresent(accessKey ->
                tasks.forEach(task ->
                    environmentVariableSetters
                        .stream()
                        .filter(setter -> setter.applies(task))
                        .findFirst()
                        .ifPresent(setter -> setter.apply(task, Constants.GRADLE_ENTERPRISE_ACCESS_KEY, accessKey))));
    }

    private boolean isGradleEnterpriseAccessKey(VariableDefinitionContext context) {
        return Constants.ACCESS_KEY.equals(context.getKey());
    }
}
