package live.nerotv.window.forms;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import live.nerotv.Serwin;
import live.nerotv.utils.OnlineConfig;
import live.nerotv.window.SerwinFrame;

import javax.swing.*;
import java.awt.*;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

public class SetupForm extends SerwinFrame {

    private final CardLayout layout = new CardLayout();
    private String panel = "update";

    private JPanel main;
    private JProgressBar processBar;

    private JPanel menu;
    private JPanel autoUpdate;
    private JPanel version;
    private JPanel build;
    private JPanel final_;

    private JButton updateButton;
    private JButton versionButton;
    private JButton buildButton;
    private JButton saveButton;

    private JPanel content;
    private JComboBox<String> comboBox_;

    public SetupForm() {
        setContentPane(main);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("SerwiN v" + Serwin.serwin + " Setup");
    }

    @Override
    public void init() {
        content.setLayout(layout);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        updateButton.addActionListener(e -> {
            initUpdaterSettings();
        });
        versionButton.addActionListener(e -> {
            initVersionSettings();
        });
        buildButton.addActionListener(e -> {
            initBuildSettings();
        });

        initUpdate();

        initVersion();

        initBuild();

        initFinish();

        initUpdaterSettings();
    }

    private void initUpdate() {
        autoUpdate = new JPanel();
        autoUpdate.setLayout(new BorderLayout());
        processBar.setValue(0);
        JTextArea text = new JTextArea();
        text.setText("During startup, should SerwiN check if there are new paper builds and install them if so?");
        text.setEditable(false);
        text.setFont(new Font(text.getFont().getFontName(), Font.PLAIN, 24));
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        JPanel buttons = new JPanel();
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");
        JButton yes_ = new JButton("Yes");
        JButton no_ = new JButton("No");
        yes.addActionListener(e -> {
            Serwin.config.set("settings.paper.autoUpdate", true);
            Serwin.config.set("settings.paper.build", "latest");
            text.setText("Should SerwiN always update to the latest Minecraft version available?");
            buttons.remove(yes);
            buttons.remove(no);
            buttons.add(no_);
            buttons.add(yes_);
            processBar.setValue(17);
            repaint();
        });
        no.addActionListener(e -> {
            Serwin.config.set("settings.paper.autoUpdate", false);
            initVersionSettings();
        });
        yes_.addActionListener(e -> {
            Serwin.config.set("settings.paper.version", "latest");
            initFinal();
        });
        no_.addActionListener(e -> {
            initVersionSettings();
        });
        buttons.setBackground(null);
        buttons.add(no);
        buttons.add(yes);
        JPanel dock = new JPanel();
        dock.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dock.setBackground(Color.decode("#090909"));
        dock.setLayout(new BorderLayout());
        dock.add(buttons, BorderLayout.EAST);
        autoUpdate.add(text, BorderLayout.NORTH);
        autoUpdate.add(dock, BorderLayout.SOUTH);
    }

    private void initVersion() {
        version = new JPanel();
        version.setLayout(new BorderLayout());
        JTextArea text_ = new JTextArea();
        text_.setText("Which Minecraft version do you want to use?");
        text_.setEditable(false);
        text_.setFont(new Font(text_.getFont().getFontName(), Font.PLAIN, 24));
        text_.setLineWrap(true);
        text_.setWrapStyleWord(true);
        String[] versions = getVersions();
        Collections.reverse(Arrays.asList(versions));
        JComboBox<String> comboBox = new JComboBox<>(versions);
        version.add(text_, BorderLayout.NORTH);
        JPanel dock_ = new JPanel();
        dock_.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dock_.setBackground(Color.decode("#090909"));
        dock_.setLayout(new BorderLayout());
        JPanel buttons_ = new JPanel();
        buttons_.add(comboBox);
        JButton proceed = new JButton("Proceed");
        proceed.addActionListener(e -> {
            Serwin.config.set("settings.paper.version", comboBox.getItemAt(comboBox.getSelectedIndex()));
            if (!Serwin.config.getBoolean("settings.paper.autoUpdate")) {
                initBuildSettings();
            } else {
                initFinal();
            }
        });
        buttons_.add(proceed);
        buttons_.setBackground(null);
        dock_.add(buttons_, BorderLayout.EAST);
        version.add(dock_, BorderLayout.SOUTH);
    }

    private void initBuild() {
        build = new JPanel();
        build.setLayout(new BorderLayout());
        JTextArea text_ = new JTextArea();
        text_.setText("Which Paper build do you want to use?");
        text_.setEditable(false);
        text_.setFont(new Font(text_.getFont().getFontName(), Font.PLAIN, 24));
        text_.setLineWrap(true);
        text_.setWrapStyleWord(true);
        String[] versions = getBuilds(getVersions()[0]);
        Collections.reverse(Arrays.asList(versions));
        comboBox_ = new JComboBox<>(versions);
        build.add(text_, BorderLayout.NORTH);
        JPanel dock_ = new JPanel();
        dock_.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dock_.setBackground(Color.decode("#090909"));
        dock_.setLayout(new BorderLayout());
        JPanel buttons_ = new JPanel();
        buttons_.add(comboBox_);
        JButton proceed = new JButton("Proceed");
        proceed.addActionListener(e -> {
            Serwin.config.set("settings.paper.build", comboBox_.getItemAt(comboBox_.getSelectedIndex()));
            initFinal();
        });
        JButton changelog = new JButton("Changelog");
        changelog.addActionListener(e -> openChangelogForm());
        buttons_.add(changelog);
        buttons_.add(proceed);
        buttons_.setBackground(null);
        dock_.add(buttons_, BorderLayout.EAST);
        build.add(dock_, BorderLayout.SOUTH);
    }

    private void openChangelogForm() {
        String version = Serwin.config.getString("settings.paper.version");
        String build = comboBox_.getItemAt(comboBox_.getSelectedIndex());
        try {
            InputStreamReader reader = new InputStreamReader(new URL("https://papermc.io/api/v2/projects/paper/versions/" + version + "/builds/" + build + "/").openStream());
            JsonObject json = new Gson().fromJson(reader, JsonObject.class);
            JsonArray array = json.get("changes").getAsJsonArray();
            JsonObject build_ = array.get(0).getAsJsonObject();
            String commit = build_.get("commit").getAsString();
            String message = build_.get("message").getAsString();
            LinkForm form = (LinkForm) SerwinFrame.get(new LinkForm("https://github.com/PaperMC/paper/commit/" + commit));
            form.setTitle(form.getTitle() + " Paper " + version + " build " + build + " changelog");
            form.setText(message.replace("\n", "<br>"));
            form.setButton("Click here to open GitHub changelog");
            form.setVisible(true);
        } catch (Exception e) {
            Serwin.logger.error("Couldn't open changelog form: " + e.getMessage());
        }
    }

    private void initFinish() {
        final_ = new JPanel();
        final_.setLayout(new BorderLayout());
        JTextArea text_ = new JTextArea();
        text_.setText("Everything set up!\nYou can now click on “Start” to complete the setup wizard.");
        text_.setEditable(false);
        text_.setFont(new Font(text_.getFont().getFontName(), Font.PLAIN, 24));
        text_.setLineWrap(true);
        text_.setWrapStyleWord(true);
        final_.add(text_, BorderLayout.NORTH);
        JPanel dock_ = new JPanel();
        dock_.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dock_.setBackground(Color.decode("#090909"));
        dock_.setLayout(new BorderLayout());
        JPanel buttons_ = new JPanel();
        JButton proceed = new JButton("Start");
        proceed.setBackground(Color.decode("#32a852"));
        proceed.setForeground(Color.white);
        proceed.addActionListener(e -> {
            dispose();
            Serwin.start();
        });
        buttons_.add(proceed);
        buttons_.setBackground(null);
        dock_.add(buttons_, BorderLayout.EAST);
        final_.add(dock_, BorderLayout.SOUTH);
    }

    private void initUpdaterSettings() {
        try {
            content.remove(autoUpdate);
        } catch (Exception ignore) {
        }
        updateButton.setEnabled(true);
        content.add(autoUpdate);
        if (panel.equalsIgnoreCase("version")) {
            versionButton.setEnabled(false);
            content.remove(version);
        } else if (panel.equalsIgnoreCase("build")) {
            buildButton.setEnabled(false);
            content.remove(build);
        } else if (panel.equalsIgnoreCase("final")) {
            saveButton.setEnabled(false);
            content.remove(final_);
        }
        panel = "update";
        processBar.setValue(0);
        repaint();
    }

    private void initVersionSettings() {
        if (!panel.equalsIgnoreCase("version")) {
            versionButton.setEnabled(true);
            content.add(version);
        }
        if (panel.equalsIgnoreCase("update")) {
            updateButton.setEnabled(false);
            content.remove(autoUpdate);
        } else if (panel.equalsIgnoreCase("build")) {
            buildButton.setEnabled(false);
            content.remove(build);
        } else if (panel.equalsIgnoreCase("final")) {
            saveButton.setEnabled(false);
            content.remove(final_);
        }
        panel = "version";
        processBar.setValue(33);
        repaint();
    }

    private void initBuildSettings() {
        if (!panel.equalsIgnoreCase("build")) {
            buildButton.setEnabled(true);
            String[] versions = getBuilds(Serwin.config.getString("settings.paper.version"));
            Collections.reverse(Arrays.asList(versions));
            comboBox_.removeAllItems();
            for (String s : versions) {
                comboBox_.addItem(s.replace(".0", ""));
            }
            content.add(build);
        }
        if (panel.equalsIgnoreCase("update")) {
            updateButton.setEnabled(false);
            content.remove(autoUpdate);
        } else if (panel.equalsIgnoreCase("version")) {
            versionButton.setEnabled(false);
            content.remove(version);
        } else if (panel.equalsIgnoreCase("final")) {
            saveButton.setEnabled(false);
            content.remove(final_);
        }
        panel = "build";
        processBar.setValue(67);
        repaint();
    }

    private void initFinal() {
        if (!panel.equalsIgnoreCase("final")) {
            content.add(final_);
            saveButton.setEnabled(true);
        }
        if (panel.equalsIgnoreCase("update")) {
            updateButton.setEnabled(false);
            content.remove(autoUpdate);
        } else if (panel.equalsIgnoreCase("version")) {
            versionButton.setEnabled(false);
            content.remove(version);
        } else if (panel.equalsIgnoreCase("build")) {
            buildButton.setEnabled(false);
            content.remove(build);
        }
        panel = "final";
        processBar.setValue(100);
        repaint();
    }

    private String[] getVersions() {
        OnlineConfig json = new OnlineConfig("https://papermc.io/api/v2/projects/paper/");
        String versionString = json.get("versions").toString().replace("[", "").replace("]", "").replace(" ", "");
        return versionString.split(",");
    }

    private String[] getBuilds(String version) {
        OnlineConfig json = new OnlineConfig("https://papermc.io/api/v2/projects/paper/versions/" + version);
        String versionString = json.get("builds").toString().replace("[", "").replace("]", "").replace(" ", "");
        return versionString.split(",");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        main = new JPanel();
        main.setLayout(new BorderLayout(0, 0));
        menu = new JPanel();
        menu.setLayout(new GridLayoutManager(5, 1, new Insets(10, 10, 10, 10), -1, -1));
        menu.setBackground(new Color(-16777216));
        menu.setForeground(new Color(-1));
        main.add(menu, BorderLayout.WEST);
        updateButton = new JButton();
        updateButton.setEnabled(true);
        updateButton.setHideActionText(false);
        updateButton.setText("Automatic updater");
        menu.add(updateButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        versionButton = new JButton();
        versionButton.setEnabled(false);
        versionButton.setText("Minecraft version");
        menu.add(versionButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buildButton = new JButton();
        buildButton.setEnabled(false);
        buildButton.setText("Paper build");
        menu.add(buildButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        saveButton = new JButton();
        saveButton.setBackground(new Color(-13858075));
        saveButton.setEnabled(false);
        saveButton.setForeground(new Color(-1));
        saveButton.setText("Save and proceed");
        menu.add(saveButton, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        menu.add(spacer1, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        content = new JPanel();
        content.setLayout(new CardLayout(0, 0));
        main.add(content, BorderLayout.CENTER);
        processBar = new JProgressBar();
        processBar.setValue(0);
        main.add(processBar, BorderLayout.SOUTH);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return main;
    }

}