package live.nerotv;

import com.formdev.flatlaf.FlatDarkLaf;
import live.nerotv.paper.PaperBuild;
import live.nerotv.paper.PaperInstaller;
import live.nerotv.utils.Config;
import live.nerotv.utils.Logger;
import live.nerotv.window.SerwinFrame;
import live.nerotv.window.forms.EULAForm;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Serwin {

    public static ArrayList<String> args = new ArrayList<>();
    public static Config config;
    public static SerwinFrame frame = null;
    public static Logger logger;
    public static Scanner scanner = new Scanner(System.in);
    public static String serwin = "2024.3-alpha.2";

    private static PaperInstaller installer;
    private static String build;
    private static String path;
    private static String version;

    public static void main(String[] a) throws UnsupportedEncodingException {
        initArguments(a);
        path =  System.getProperty("user.dir");
        config = new Config(URLDecoder.decode(path+"/serwin.json","UTF-8"));
        logger = new Logger("SERWIN");
        config.checkEntry("settings.paper.autoUpdate",true);
        init();
    }

    public static void start() {
        frame = null;
        if(build==null) {
            build = config.getString("settings.paper.build");
        }
        if(version==null) {
            version = config.getString("settings.paper.version");
        }

        preLaunch();
        System.gc();
        launch();
    }

    private static void initDesktop() {
        if (config.getString("settings.paper.version") == null || config.getString("settings.paper.build") == null) {
            try {
                FlatDarkLaf.setup();
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (Exception ignore) {
            }
            SerwinFrame eulaForm = SerwinFrame.get(new EULAForm());
            Dimension size = new Dimension(500,145);
            eulaForm.setMinimumSize(size);
            eulaForm.setSize(size);
            eulaForm.setMaximumSize(size);
            eulaForm.setResizable(false);
            eulaForm.setLocationRelativeTo(null);
            eulaForm.setVisible(true);
        } else {
            start();
        }
    }

    private static void init() {
        if (Desktop.isDesktopSupported()) {
            initDesktop();
        } else {
            if (config.getString("settings.paper.version") == null || config.getString("settings.paper.build") == null) {
                logger.log("[Q] Do you accept the Minecraft EULA? (https://www.minecraft.net/eula) [Y/n]");
                String answer = scanner.nextLine().toLowerCase();
                if (answer.equals("y") || answer.equals("yes") || answer.equals("t") || answer.equals("true")) {
                    acceptEULA();
                    logger.log("[S] Good! Let's move on to creating your server!");
                    logger.log("");
                } else {
                    logger.error("[S] You have to accept the Minecraft EULA (https://www.minecraft.net/eula) to run a server!");
                    logger.log("");
                    return;
                }
                logger.log("[Q] Do you want to use the auto-updater? [Y/n]");
                answer = scanner.nextLine().toLowerCase();
                if (answer.equals("y") || answer.equals("yes") || answer.equals("t") || answer.equals("true")) {
                    config.set("settings.paper.autoUpdate", true);
                    config.set("settings.paper.build", "latest");
                    logger.log("[S] SerwiN automatically updates Paper.");
                    logger.log("");
                    logger.log("[#] Leave blank or type \"latest\" to automatically update the Minecraft version too.");
                    getMinecraft();
                } else if (answer.equals("n") || answer.equals("no") || answer.equals("f") || answer.equals("false")) {
                    config.set("settings.paper.autoUpdate", false);
                    logger.log("[S] SerwiN won't check for Paper or Minecraft updates.");
                    getMinecraft();
                    logger.log("[Q] Which Paper build do you want to use?");
                    answer = scanner.nextLine().toLowerCase();
                    config.set("settings.paper.build", answer);
                    build = answer;
                    logger.log("[S] Set Minecraft version to " + build + ".");
                    logger.log("");
                } else {
                    logger.error("Invalid answer: " + answer);
                    init();
                }
            }
            start();
        }
    }

    private static void getMinecraft() {
        logger.log("[Q] Which Minecraft version do you want to use?");
        String answer = scanner.nextLine().toLowerCase();
        if(answer.isEmpty()||answer.startsWith("latest")||answer.startsWith("auto")||answer.startsWith("y")) {
            version = "latest";
            config.set("settings.paper.version","latest");
            logger.log("[S] SerwiN automatically updates the Minecraft version.");
        } else {
            version = answer;
            config.set("settings.paper.version",answer);
            logger.log("[S] SerwiN won't check for Minecraft updates.");
            logger.log("[S] Set Minecraft version to "+version+".");
        }
        logger.log("");
    }

    private static void preLaunch() {
        logger.log("Preparing launch...");
        boolean autoUpdate = config.getBool("settings.paper.autoUpdate");
        if(autoUpdate) {
            if(version.equalsIgnoreCase("latest")) {
                installer = new PaperInstaller(PaperBuild.getLatest());
            } else {
                installer = new PaperInstaller(PaperBuild.getLatest(version));
            }
        } else {
            installer = new PaperInstaller(PaperBuild.get(version,build));
        }
        PaperBuild paperBuild = installer.getBuild();
        File paper = new File(path+"/cache/"+paperBuild.getFilename());
        if(!paper.exists()) {
            installer.install();
        }

        logger.log("Launching Paper "+paperBuild.getVersion()+" build "+paperBuild.getBuildNumber()+"...");
    }

    private static void launch() {
        try {
            File folder = new File(path + "/server/");
            folder.mkdirs();
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add("-Xmx" + Runtime.getRuntime().maxMemory());
            String server = path + "/server/server.jar";
            Files.copy(Paths.get(path + "/cache/" + installer.getBuild().getFilename()), Paths.get(server), StandardCopyOption.REPLACE_EXISTING);
            command.add(server);
            if (args != null && !args.isEmpty()) {
                command.addAll(args);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(folder);
            processBuilder.redirectErrorStream(true); // Merge error and output streams
            Process process = processBuilder.start();

            CompletableFuture.runAsync(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            int exitCode = process.waitFor();
            logger.log("Server stopped with exit code: " + exitCode);

        } catch (Exception e) {
            logger.error("Couldn't launch " + installer.getBuild().getName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void acceptEULA() {
        String filePath = path+"/server/eula.txt";
        String content = "eula=true";
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(content);
                System.out.println("Text erfolgreich in die Datei geschrieben.");
            }
        } catch (IOException e) {
            logger.error("Couldn't accept the Minecraft EULA: "+e.getMessage());
        }
    }


    private static void initArguments(String[] a) {
        args = new ArrayList<>();
        args.addAll(Arrays.asList(a));
    }
}