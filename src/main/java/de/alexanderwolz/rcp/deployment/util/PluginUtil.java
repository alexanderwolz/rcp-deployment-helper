package de.alexanderwolz.rcp.deployment.util;

import de.alexanderwolz.rcp.deployment.model.Version;

public class PluginUtil {
    public static String getVersionString(Version version) {
        String s = version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
        if (version.getQualifier() != null) {
            s += "." + version.getQualifier();
        }
        return s;
    }
}
