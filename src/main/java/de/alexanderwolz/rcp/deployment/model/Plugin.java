package de.alexanderwolz.rcp.deployment.model;

import java.io.File;

public class Plugin {

    private Version version;
    private String name;

    private boolean isModified;

    public Plugin(File file, Version version) {
	this.name = file.getName();
	this.version = version;
    }

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

    public void setName(String name) {
	this.name = name;
    }

    public boolean isModified() {
	return isModified;
    }

    public void setModified(boolean isModified) {
	this.isModified = isModified;
    }
}
