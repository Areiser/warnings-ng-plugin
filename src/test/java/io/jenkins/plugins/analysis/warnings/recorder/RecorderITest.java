package io.jenkins.plugins.analysis.warnings.recorder;

import java.io.IOException;

import org.junit.Test;

import hudson.model.FreeStyleProject;
import hudson.model.Result;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.steps.IssuesRecorder;
import io.jenkins.plugins.analysis.core.testutil.IntegrationTestWithJenkinsPerSuite;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateResult;
import io.jenkins.plugins.analysis.core.util.QualityGate.QualityGateType;
import io.jenkins.plugins.analysis.warnings.Java;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.ConfigurationView;
import io.jenkins.plugins.analysis.warnings.recorder.pageobj.InfoView;

import static org.assertj.core.api.Assertions.*;

public class RecorderITest extends IntegrationTestWithJenkinsPerSuite {

    private static final int HEALTHY_VALUE = 1;
    private static final int UNHEALTHY_VALUE = 9;

    /**
     * Normal test with API usagee only
     */
    @Test
    public void shouldCreateFreestyleJobWithJavaWarnings() {
        FreeStyleProject project = createFreeStyleProject();

        Java javaAnalysis = new Java();
        copySingleFileToWorkspace(project, "../javac.txt", "javac.log");
        javaAnalysis.setPattern("**/*.log");

        IssuesRecorder recorder = enableWarnings(project, javaAnalysis);
        recorder.addQualityGate(5, QualityGateType.TOTAL, QualityGateResult.UNSTABLE);
        recorder.addQualityGate(10, QualityGateType.TOTAL, QualityGateResult.FAILURE);

        recorder.setUnhealthy(UNHEALTHY_VALUE);
        recorder.setHealthy(HEALTHY_VALUE);

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        assertThat(result.getTotalSize()).isEqualTo(2);
    }

    @Test
    public void tenWarningsWithHealthZero() throws IOException {
        verifyWarningsAndHealth("javac_10_warnings.txt", true, 0, 10);
    }

    @Test
    public void tenWarningsWithoutHealth() throws IOException {
        verifyWarningsAndHealth("javac_10_warnings.txt", false, 0, 10);
    }

    @Test
    public void nineWarningsWithHealthTen() throws IOException {
        verifyWarningsAndHealth("javac_9_warnings.txt", true, 10, 9);
    }

    @Test
    public void nineWarningsWithoutHealth() throws IOException {
        verifyWarningsAndHealth("javac_9_warnings.txt", false, 10, 9);
    }

    @Test
    public void oneWarningWithHealthNinety() throws IOException {
        verifyWarningsAndHealth("javac_1_warning.txt", true, 90, 1);
    }

    @Test
    public void oneWarningWithoutHealth() throws IOException {
        verifyWarningsAndHealth("javac_1_warning.txt", false, 90, 1);
    }

    @Test
    public void noWarningsWithoutHealth() throws IOException {
        verifyWarningsAndHealth("javac_0_warnings.txt", false, 100, 0);
    }

    @Test
    public void noWarningsWithHealthFull() throws IOException {
        verifyWarningsAndHealth("javac_0_warnings.txt", true, 100, 0);
    }

    private void verifyWarningsAndHealth(final String filePath, final boolean healthReport, final int healthScore,
            final int warnings) throws IOException {
        FreeStyleProject project = createFreeStyleProject();

        Java javaAnalysis = new Java();
        copySingleFileToWorkspace(project, filePath, "javac.log");
        javaAnalysis.setPattern("**/*.log");
        enableWarnings(project, javaAnalysis);

        if (healthReport) {
            ConfigurationView config = new ConfigurationView(getWebPage(project, "configure"));

            config.setHealthy(HEALTHY_VALUE);
            config.setUnhealthy(UNHEALTHY_VALUE);
            config.submit();
        }

        AnalysisResult result = scheduleBuildAndAssertStatus(project, Result.SUCCESS);

        InfoView infoView = new InfoView(getWebPage(project, "1/java/info"));

        assertThat(result.getTotalSize()).isEqualTo(warnings);

        assertThat(infoView.getInfoMessages()).isEqualTo(result.getInfoMessages());
        assertThat(infoView.getErrorMessages()).isEqualTo(result.getErrorMessages());

        if (healthReport) {

            assertThat(infoView.getInfoMessages()).contains(
                    "Enabling health report (Healthy=1, Unhealthy=9, Minimum Severity=LOW)");
            assertThat(project.getBuildHealth().getScore()).isEqualTo(healthScore);
        }
        else {
            assertThat(infoView.getInfoMessages()).contains(
                    "Health report is disabled - skipping");
        }
    }

}