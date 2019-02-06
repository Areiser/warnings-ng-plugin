package io.jenkins.plugins.analysis.core.portlets;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import edu.hm.hafner.util.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.view.dashboard.DashboardPortlet;

import io.jenkins.plugins.analysis.core.charts.SeverityChart;
import io.jenkins.plugins.analysis.core.model.History;
import io.jenkins.plugins.analysis.core.model.NullAnalysisHistory;
import io.jenkins.plugins.analysis.core.model.ToolSelection;
import io.jenkins.plugins.analysis.core.util.JenkinsFacade;

/**
 * A dashboard view portlet that renders a two-dimensional table of issues per type and job.
 *
 * @author Ullrich Hafner
 */
public class IssuesChartPortlet extends DashboardPortlet {
    private boolean hideCleanJobs;
    private boolean selectTools = false;
    private List<ToolSelection> tools = new ArrayList<>();

    private JenkinsFacade jenkinsFacade = new JenkinsFacade();

    /**
     * Creates a new instance of {@link IssuesChartPortlet}.
     *
     * @param name
     *         the name of the portlet
     */
    @DataBoundConstructor
    public IssuesChartPortlet(final String name) {
        super(name);
    }

    @VisibleForTesting
    void setJenkinsFacade(final JenkinsFacade jenkinsFacade) {
        this.jenkinsFacade = jenkinsFacade;
    }

    private JenkinsFacade getJenkinsFacade() {
        return ObjectUtils.defaultIfNull(jenkinsFacade, new JenkinsFacade());
    }

    @SuppressWarnings("unused") // called by Stapler
    public boolean getHideCleanJobs() {
        return hideCleanJobs;
    }

    /**
     * Determines if all jobs that have no issues from the selected static analysis tools should be hidden.
     *
     * @param hideCleanJobs
     *         if {@code true} then all jobs with no issues will be hidden, {@code false} otherwise
     */
    @SuppressWarnings("WeakerAccess")
    @DataBoundSetter
    public void setHideCleanJobs(final boolean hideCleanJobs) {
        this.hideCleanJobs = hideCleanJobs;
    }

    @SuppressWarnings("unused") // called by Stapler
    public boolean getSelectTools() {
        return selectTools;
    }

    /**
     * Determines whether all available tools should be selected or if the selection should be done individually.
     *
     * @param selectTools
     *         if {@code true} the selection of tools can be done manually by selecting the corresponding ID, otherwise
     *         all available tools in a job are automatically selected
     */
    @SuppressWarnings("WeakerAccess") // called by Stapler
    @DataBoundSetter
    public void setSelectTools(final boolean selectTools) {
        this.selectTools = selectTools;
    }

    public List<ToolSelection> getTools() {
        return tools;
    }

    /**
     * Returns the tools that should be taken into account when summing up the totals of a job.
     *
     * @param tools
     *         the tools to select
     *
     * @see #setSelectTools(boolean)
     */
    @DataBoundSetter
    public void setTools(final List<ToolSelection> tools) {
        this.tools = tools;
    }

    /**
     * Returns the UI model for an ECharts line chart that shows the issues stacked by severity.
     *
     * @return the UI model as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // Called by jelly view
    public JSONObject getTrend() {
        SeverityChart severityChart = new SeverityChart();

        History history = new NullAnalysisHistory();
        return JSONObject.fromObject(severityChart.create(history));
    }

    /**
     * Extension point registration.
     *
     * @author Ulli Hafner
     */
    @Extension(optional = true)
    public static class IssuesTablePortletDescriptor extends Descriptor<DashboardPortlet> {
        @NonNull
        @Override
        public String getDisplayName() {
            return "TODO";
        }
    }
}