package de.alexanderwolz.rcp.deployment.model;

public class Plugin {

    private final String name;
    private Version version;

    private boolean isModified;

    public Plugin(String name, Version version) {
        this.name = name;
        this.version = version;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }
}
