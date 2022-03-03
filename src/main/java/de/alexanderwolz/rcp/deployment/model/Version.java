package de.alexanderwolz.rcp.deployment.model;

public class Version {

    private int major = 0;
    private int minor = 0;
    private int micro = 0;
    private String qualifier = "qualifier";

    public Version(String version) {

	String[] s = version.split("\\.");
	if (s.length > 4) {
	    throw new RuntimeException("Unsupported Version");
	}

	try {
	    major = Integer.parseInt(s[0]);
	    minor = Integer.parseInt(s[1]);
	    micro = Integer.parseInt(s[2]);
	    qualifier = s[3];
	} catch (Exception e) {
	    throw new RuntimeException(e);
	}
    }

    public int getMajor() {
	return major;
    }

    public void setMajor(int major) {
	this.major = major;
    }

    public int getMinor() {
	return minor;
    }

    public void setMinor(int minor) {
	this.minor = minor;
    }

    public int getMicro() {
	return micro;
    }

    public void setMicro(int micro) {
	this.micro = micro;
    }

    public String getQualifier() {
	return qualifier;
    }

    public void setQualifier(String qualifier) {
	this.qualifier = qualifier;
    }
}
