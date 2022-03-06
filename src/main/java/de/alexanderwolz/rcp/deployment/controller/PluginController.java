package de.alexanderwolz.rcp.deployment.controller;

import de.alexanderwolz.rcp.deployment.model.Plugin;
import de.alexanderwolz.rcp.deployment.model.Version;
import de.alexanderwolz.rcp.deployment.util.PluginUtil;

import java.io.*;
import java.util.*;

public class PluginController {

    private final String DEFAULT_LOCATION = System.getProperty("user.home");
    private final String MANIFEST = "/META-INF/MANIFEST.MF";
    private final String BUNDLE_VERSION = "Bundle-Version:";

    private String workspace = DEFAULT_LOCATION;
    private Map<String, Plugin> plugins = new TreeMap<>();

    private final ArrayList<IEventListener> listeners = new ArrayList<>();

    public void addListener(IEventListener listener) {
        listeners.add(listener);
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
        plugins.clear();
        plugins = loadPluginsFromWorkspace();
        for (IEventListener listener : listeners) {
            new Thread(() -> listener.onWorkspaceChanged(workspace)).start();
        }
    }

    public void reload() {
        setWorkspace(workspace);
    }

    public Map<String, Plugin> getPlugins() {
        return Collections.unmodifiableMap(plugins);
    }

    /**
     * searches for files within specified location and puts results to the
     * previously cleaned map
     */
    private Map<String, Plugin> loadPluginsFromWorkspace() {
        File directory = new File(workspace);
        Map<String, Plugin> plugins = new TreeMap<>();
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            assert files != null;
            for (File file : files) {
                // check if file-directory contains manifest
                File manifest = new File(file.getAbsoluteFile() + MANIFEST);
                if (manifest.exists()) {
                    try {
                        Plugin plugin = new Plugin(file.getName(), getVersionFromManifest(manifest));
                        plugins.put(plugin.getName(), plugin);
                    } catch (Exception e) {
                        // corrupt manifest file or invalid version
                        for (IEventListener listener : listeners) {
                            new Thread(() -> listener.onManifestException(file)).start();
                        }
                    }
                }
            }
        }
        return plugins;
    }

    /**
     * saves the version in Manifest.MF of given bundle
     */
    public boolean saveVersionToManifest(Plugin plugin) {

        File manifest = new File(workspace + "/" + plugin.getName() + MANIFEST);
        if (manifest.exists()) {
            StringBuilder content = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(manifest));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(BUNDLE_VERSION)) {
                        line = BUNDLE_VERSION + " " + PluginUtil.getVersionString(plugin.getVersion());
                    }
                    content.append(line).append('\n');
                }
                reader.close();

                // write to file
                BufferedWriter writer = new BufferedWriter(new FileWriter(manifest));
                writer.write(content.toString());
                writer.flush();
                writer.close();
                return true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * returns the Version from specified Manifest.MF
     *
     * @param manifest , the MANIFEST.MF
     * @return Version or null if not found
     */
    private Version getVersionFromManifest(File manifest) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(manifest))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(BUNDLE_VERSION)) {
                    return new Version(line.substring(BUNDLE_VERSION.length()).trim());
                }
            }
        }

        return null;
    }

    public void revert() {
        Map<String, Plugin> workspacePlugins = loadPluginsFromWorkspace();
        for (Plugin plugin : plugins.values()) {
            if (plugin.isModified()) {
                Plugin workspacePlugin = workspacePlugins.get(plugin.getName());
                if (workspacePlugin != null) {
                    plugin.setVersion(workspacePlugin.getVersion());
                    plugin.setModified(false);
                }
            }
        }
    }

    public interface IEventListener {
        void onWorkspaceChanged(String workspace);
        void onManifestException(File file);
    }
}
