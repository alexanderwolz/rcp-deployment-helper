package de.alexanderwolz.rcp.deployment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.alexanderwolz.rcp.deployment.model.Plugin;
import de.alexanderwolz.rcp.deployment.model.Version;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class DeploymentHelper {

    private final String DEFAULT_LOCATION = System.getProperty("user.home");
    private final String MANIFEST = "/META-INF/MANIFEST.MF";
    private final String BUNDLE_VERSION = "Bundle-Version:";

    private String workspace = DEFAULT_LOCATION;
    private Map<String, Plugin> plugins = new TreeMap<>();

    private final Display display;
    private final Shell shell;
    private Table table;
    private Button revertButton;
    private Button applyButton;

    public DeploymentHelper() {
        display = new Display();
        shell = new Shell(display);
        shell.setText("RCP Plugin Deployment Helper");
        shell.setSize(525, 500);
    }

    public void run() {
        createMenu();
        createContents();
        shell.open();
        initialize();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    private void createMenu() {
        Menu menuBar = new Menu(shell, SWT.BAR);
        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);

        MenuItem fileItem = new MenuItem(menuBar, SWT.CASCADE);
        fileItem.setText("File");
        fileItem.setMenu(fileMenu);

        MenuItem reloadItem = new MenuItem(fileMenu, SWT.PUSH);
        reloadItem.setText("Reload Workspace");
        reloadItem.addListener(SWT.Selection, event -> initialize());

        MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
        exitItem.setText("Exit");
        exitItem.addListener(SWT.Selection, event -> {
            shell.close();
            display.dispose();
        });

        MenuItem helpItem = new MenuItem(menuBar, SWT.CASCADE);
        helpItem.setText("Help");

        helpItem.setMenu(helpMenu);

        MenuItem aboutItem = new MenuItem(helpMenu, SWT.PUSH);
        aboutItem.setText("About");
        aboutItem.addListener(SWT.Selection, event -> {
            MessageBox msgBox = new MessageBox(shell);
            msgBox.setText("About");
            msgBox.setMessage("(c) 2011 Alexander Wolz mail@alexanderwolz.de");
            msgBox.open();
        });

        shell.setMenuBar(menuBar);
    }

    /**
     * creates content for UI
     */
    private void createContents() {

        shell.setLayout(new GridLayout(6, false));
        new Label(shell, SWT.NONE).setText("Workspace:");

        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;

        final Text text = new Text(shell, SWT.BORDER);
        text.setLayoutData(gridData);
        text.setText(DEFAULT_LOCATION);
        text.addListener(SWT.DefaultSelection, event -> {
            // save workspaceLocation
            workspace = text.getText();
            initialize();
        });

        Button workspaceButton = new Button(shell, SWT.PUSH);
        workspaceButton.setText("Browse...");
        workspaceButton.addListener(SWT.Selection, event -> {
            DirectoryDialog dialog = new DirectoryDialog(shell);
            dialog.setFilterPath(text.getText());
            dialog.setMessage("Choose Workspace");
            String dir = dialog.open();
            if (dir != null) {
                text.setText(dir);
                workspace = dir;
                initialize();
            }
        });

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;

        Group incGroup = new Group(shell, SWT.NONE);
        incGroup.setLayoutData(gridData);
        incGroup.setText("Increment");
        incGroup.setLayout(new FillLayout(SWT.HORIZONTAL));

        Button incrementMajor = new Button(incGroup, SWT.PUSH);
        incrementMajor.setText("Major");
        incrementMajor.addListener(SWT.Selection, event -> {
            for (TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    Plugin plugin = plugins.get(item.getText(1));
                    int major = plugin.getVersion().getMajor() + 1;
                    plugin.getVersion().setMajor(major);
                    plugin.setModified(true);
                    revertButton.setEnabled(true);
                    applyButton.setEnabled(true);
                }
            }
            updateTable();
        });

        Button incrementMinor = new Button(incGroup, SWT.PUSH);
        incrementMinor.setText("Minor");
        incrementMinor.addListener(SWT.Selection, event -> {
            for (TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    Plugin plugin = plugins.get(item.getText(1));
                    int minor = plugin.getVersion().getMinor() + 1;
                    plugin.getVersion().setMinor(minor);
                    plugin.setModified(true);
                    revertButton.setEnabled(true);
                    applyButton.setEnabled(true);
                }
            }
            updateTable();
        });

        Button incrementMicro = new Button(incGroup, SWT.PUSH);
        incrementMicro.setText("Micro");
        incrementMicro.addListener(SWT.Selection, event -> {
            for (TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    Plugin plugin = plugins.get(item.getText(1));
                    int micro = plugin.getVersion().getMicro() + 1;
                    plugin.getVersion().setMicro(micro);
                    plugin.setModified(true);
                    revertButton.setEnabled(true);
                    applyButton.setEnabled(true);
                }
            }
            updateTable();
        });

        new Label(shell, SWT.NONE);

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;

        Group versGroup = new Group(shell, SWT.NONE);
        versGroup.setText("set Version");
        versGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        versGroup.setLayoutData(gridData);

        final Text versionText = new Text(versGroup, SWT.BORDER);
        versionText.setText("1.0.0.qualifier");

        Button versionButton = new Button(versGroup, SWT.PUSH);
        versionButton.setText("set Version");
        versionButton.addListener(SWT.Selection, event -> {
            for (TableItem item : table.getItems()) {
                if (item.getChecked()) {
                    Plugin plugin = plugins.get(item.getText(1));
                    Version version = new Version(versionText.getText().trim());
                    plugin.setVersion(version);
                    plugin.setModified(true);
                    revertButton.setEnabled(true);
                    applyButton.setEnabled(true);
                }
            }
            updateTable();
        });

        gridData = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gridData.horizontalSpan = 6;

        table = new Table(shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayoutData(gridData);

        TableColumn checkColumn = new TableColumn(table, SWT.NONE);
        checkColumn.setWidth(20);
        checkColumn.setText("");
        checkColumn.setResizable(false);

        TableColumn pluginColumn = new TableColumn(table, SWT.NONE);
        pluginColumn.setText("Plugin");

        TableColumn versionColumn = new TableColumn(table, SWT.NONE);
        versionColumn.setText("Version");

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 3;

        final Button selectAllButton = new Button(shell, SWT.CHECK);
        selectAllButton.setText("Select All");
        selectAllButton.setLayoutData(gridData);
        selectAllButton.addListener(SWT.Selection, event -> {
            for (TableItem item : table.getItems()) {
                item.setChecked(selectAllButton.getSelection());
            }
        });

        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;

        revertButton = new Button(shell, SWT.NONE);
        revertButton.setText("Revert");
        revertButton.setEnabled(false);
        revertButton.setLayoutData(gridData);
        revertButton.addListener(SWT.Selection, event -> {

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
            updateTable();
            revertButton.setEnabled(false);
            applyButton.setEnabled(false);
        });

        applyButton = new Button(shell, SWT.NONE);
        applyButton.setEnabled(false);
        applyButton.setText("Apply");
        applyButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        applyButton.addListener(SWT.Selection, event -> {
            List<Plugin> failedPlugins = new ArrayList<>();
            int count = 0;
            for (Plugin plugin : plugins.values()) {
                if (plugin.isModified()) {
                    boolean saved = saveVersionToManifest(plugin);
                    if (!saved) {
                        failedPlugins.add(plugin);
                    }
                    plugin.setModified(false);
                    count++;
                }
            }
            MessageBox msgBox = new MessageBox(shell);
            msgBox.setText("Information");
            StringBuilder msg = new StringBuilder();
            if (failedPlugins.size() > 0) {
                msg.append("Could not save Plugin:");
                for (Plugin plugin : failedPlugins) {
                    msg.append("\n   ").append(plugin.getName());
                }
                msgBox.setText("Error");
            } else if (count == 0) {
                msg.append("No Change");
            } else {
                msg.append("Successfully modified ").append(count).append(" plugins!");
            }

            msgBox.setMessage(msg.toString());
            msgBox.open();

            applyButton.setEnabled(false);
            revertButton.setEnabled(false);
        });

    }

    /**
     * loads all bundles from workspace and fills Table
     */
    private void initialize() {
        plugins = loadPluginsFromWorkspace();

        table.removeAll();

        for (Plugin plugin : plugins.values()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setData(plugin);
            item.setText(1, plugin.getName());
            item.setText(2, getVersionString(plugin.getVersion()));
        }

        // pack all columns except the first one
        for (int i = 1; i < table.getColumns().length; i++) {
            table.getColumn(i).pack();
        }

        revertButton.setEnabled(false);
        applyButton.setEnabled(false);
    }

    private void updateTable() {
        for (TableItem item : table.getItems()) {
            Plugin plugin = (Plugin) item.getData();
            item.setText(1, plugin.getName());
            item.setText(2, getVersionString(plugin.getVersion()));
        }
    }

    private String getVersionString(Version version) {
        String s = version.getMajor() + "." + version.getMinor() + "." + version.getMicro();
        if (version.getQualifier() != null) {
            s += "." + version.getQualifier();
        }
        return s;
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
                        MessageBox box = new MessageBox(shell, SWT.ERROR);
                        box.setMessage("Corrupt Manifest for file '" + file.getName() + "'");
                        box.setText("Error");
                        box.open();
                    }
                }
            }
        }
        return plugins;
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

    /**
     * saves the version in Manifest.MF of given bundle
     */
    private boolean saveVersionToManifest(Plugin plugin) {

        File manifest = new File(workspace + "/" + plugin.getName() + MANIFEST);
        if (manifest.exists()) {
            StringBuilder content = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(manifest));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(BUNDLE_VERSION)) {
                        line = BUNDLE_VERSION + " " + getVersionString(plugin.getVersion());
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
}
