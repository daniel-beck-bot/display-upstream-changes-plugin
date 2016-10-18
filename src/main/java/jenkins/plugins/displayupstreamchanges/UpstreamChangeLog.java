package jenkins.plugins.displayupstreamchanges;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import jenkins.model.Jenkins;

@SuppressWarnings("rawtypes")
public class UpstreamChangeLog {

    private ChangeLogSet changeLogSet;
    private AbstractBuild build;

    public UpstreamChangeLog(ChangeLogSet changeLogSet, AbstractBuild build) {
        this.changeLogSet = changeLogSet;
        this.build = build;
    }

    public AbstractBuild getBuild() {
        return build;
    }

    public void setBuild(AbstractBuild build) {
        this.build = build;
    }

    public ChangeLogSet getChangeLogSet() {
        return changeLogSet;
    }

    public void setChangeLogSet(ChangeLogSet changeLogSet) {
        this.changeLogSet = changeLogSet;
    }

    public String getDisplayName() {
        return build.getProject().getDisplayName() + " " + build.getDisplayName();
    }

    public String getAbsoluteBuildUrl() {
        Jenkins instance = Jenkins.getInstance();
        if (instance != null) {
            return instance.getRootUrl() + "/" + build.getUrl();
        } else {
            return "/" + build.getUrl(); //should never happen
        }
    }

    public String getSCMDisplayName() {
        return build.getProject().getScm().getDescriptor().getDisplayName();
    }

}
