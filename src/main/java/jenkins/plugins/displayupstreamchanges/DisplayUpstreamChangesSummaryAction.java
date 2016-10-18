/*
 * The MIT License
 *
 * Copyright (c) 2004-2010, Sun Microsystems, Inc., Serban Iordache
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.displayupstreamchanges;

import hudson.model.Action;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jenkins.model.Jenkins;

public class DisplayUpstreamChangesSummaryAction implements Action {
    @SuppressWarnings("rawtypes")
    private AbstractBuild build;

    @SuppressWarnings("rawtypes")
    public DisplayUpstreamChangesSummaryAction(AbstractBuild build) {
        this.build = build;
    }

    /* Action methods */
    public String getUrlName() { return ""; }
    public String getDisplayName() { return ""; }
    public String getIconFileName() { return null; }

    @SuppressWarnings("rawtypes")
    public List<UpstreamChangeLog> getUpstreamChangeLogs() {
        List<UpstreamChangeLog> upstreamChangeLogs = new ArrayList<UpstreamChangeLog>();
        List<ChangeLogSet> changeLogSets = new ArrayList<ChangeLogSet>();
        // Get upstream builds from fingerprinting
        @SuppressWarnings("unchecked")
        Map<AbstractProject<?,?>,Integer> transitiveUpstreamBuilds = build.getTransitiveUpstreamBuilds();
        for (Entry<AbstractProject<?,?>,Integer> e : transitiveUpstreamBuilds.entrySet()) {
            AbstractBuild<?,?> run = e.getKey().getBuildByNumber(e.getValue());
            if (run.hasChangeSetComputed()) {
                ChangeLogSet<?> cls = run.getChangeSet();
                if (cls != null && !cls.isEmptySet()) {
                    changeLogSets.add(cls);
                    upstreamChangeLogs.add(new UpstreamChangeLog(cls, run));
                }
            }
        }

        // Upstream builds via cause
        List<AbstractBuild> upstreamBuilds = new ArrayList<AbstractBuild>();
        getAllUpstreamByCause(this.build, upstreamBuilds);
        for (AbstractBuild build : upstreamBuilds) {
            if (build.hasChangeSetComputed()) {
                ChangeLogSet cls = build.getChangeSet();
                if (!changeLogSets.contains(cls) && !cls.isEmptySet()) {
                    changeLogSets.add(cls);
                    upstreamChangeLogs.add(new UpstreamChangeLog(cls, build));
                }
            }
        }

        return upstreamChangeLogs;
    }

    @SuppressWarnings("rawtypes")
    private static void getAllUpstreamByCause(AbstractBuild build, List<AbstractBuild> list) {
        for (AbstractBuild upstreamBuild : getUpstreamByCause(build)) {
            //Duplication and cycle protection
            if (list.contains(upstreamBuild)) {
                continue;
            } else {
                list.add(upstreamBuild);
                getAllUpstreamByCause(upstreamBuild, list);
            }
        }
        return;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static List<AbstractBuild> getUpstreamByCause(AbstractBuild build) {
        List<AbstractBuild> upstreamBuilds = new ArrayList<AbstractBuild>();
        for (Cause cause: (List<Cause>) build.getCauses()) {
            if (cause instanceof Cause.UpstreamCause) {
                TopLevelItem upstreamProject = null;
                jenkins.model.Jenkins instance = Jenkins.getInstance();
                if (instance != null) {
                    upstreamProject = instance.getItemByFullName(((Cause.UpstreamCause)cause).getUpstreamProject(), TopLevelItem.class);
                }
                if (upstreamProject instanceof AbstractProject) {
                    int buildId = ((Cause.UpstreamCause)cause).getUpstreamBuild();
                    Run run = ((AbstractProject) upstreamProject).getBuildByNumber(buildId);
                    upstreamBuilds.add((AbstractBuild) run);
                }
            }
        }
        return upstreamBuilds;
    }

}
