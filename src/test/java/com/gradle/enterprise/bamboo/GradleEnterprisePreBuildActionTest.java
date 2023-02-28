package com.gradle.enterprise.bamboo;

import com.atlassian.bamboo.ResultKey;
import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.v2.build.BuildContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GradleEnterprisePreBuildActionTest {

    private static final RuntimeException INJECTION_EXCEPTION = new RuntimeException();

    @Test
    void preBuildActionDoesntFailOnException() {
        BuildScanInjector mockBuildScanInjector = mock(BuildScanInjector.class);
        BuildLoggerManager mockBuildLoggerManager = mock(BuildLoggerManager.class);

        doThrow(INJECTION_EXCEPTION).when(mockBuildScanInjector).inject(any());
        when(mockBuildScanInjector.buildTool()).thenReturn(BuildTool.GRADLE);

        BuildLogger mockBuildLogger = mock(BuildLogger.class);
        when(mockBuildLoggerManager.getLogger(any(ResultKey.class))).thenReturn(mockBuildLogger);

        GradleEnterprisePreBuildAction gradleEnterprisePreBuildAction = new GradleEnterprisePreBuildAction(
                Arrays.asList(mockBuildScanInjector),
                mockBuildLoggerManager
        );

        gradleEnterprisePreBuildAction.init(TestFixtures.getBuildContext());

        assertDoesNotThrow(gradleEnterprisePreBuildAction::call);
        verify(mockBuildLoggerManager, times(1)).getLogger(any(ResultKey.class));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockBuildLogger).addErrorLogEntry(argumentCaptor.capture(), eq(INJECTION_EXCEPTION));
        assertEquals("Gradle Enterprise Gradle auto-injection failed", argumentCaptor.getValue());
    }

    @Test
    void injectionSucceedsForOneAndFailsForAnother() {
        BuildScanInjector mockSuccessfulBuildScanInjector = mock(BuildScanInjector.class);
        BuildScanInjector mockFailedBuildScanInjector = mock(BuildScanInjector.class);
        BuildLoggerManager mockBuildLoggerManager = mock(BuildLoggerManager.class);

        doThrow(INJECTION_EXCEPTION).when(mockFailedBuildScanInjector).inject(any());
        when(mockFailedBuildScanInjector.buildTool()).thenReturn(BuildTool.GRADLE);

        BuildLogger mockBuildLogger = mock(BuildLogger.class);
        when(mockBuildLoggerManager.getLogger(any(ResultKey.class))).thenReturn(mockBuildLogger);

        GradleEnterprisePreBuildAction gradleEnterprisePreBuildAction = new GradleEnterprisePreBuildAction(
                Arrays.asList(mockSuccessfulBuildScanInjector, mockFailedBuildScanInjector),
                mockBuildLoggerManager
        );

        gradleEnterprisePreBuildAction.init(TestFixtures.getBuildContext());

        assertDoesNotThrow(gradleEnterprisePreBuildAction::call);

        verify(mockSuccessfulBuildScanInjector, times(1)).inject(any(BuildContext.class));
        verify(mockFailedBuildScanInjector, times(1)).inject(any(BuildContext.class));

        verify(mockBuildLoggerManager, times(1)).getLogger(any(ResultKey.class));

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockBuildLogger).addErrorLogEntry(argumentCaptor.capture(), eq(INJECTION_EXCEPTION));
        assertEquals("Gradle Enterprise Gradle auto-injection failed", argumentCaptor.getValue());
    }

}