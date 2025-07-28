package live.nerotv.serwin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zyneonstudios.nexus.utilities.file.FileGetter;
import com.zyneonstudios.nexus.utilities.json.GsonUtility;
import com.zyneonstudios.nexus.utilities.storage.JsonStorage;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {

    private static File serwin = new File("cache/serwin-executable.jar");
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws FileNotFoundException {
        if (new File("cache/").mkdirs()) {
            System.out.println("Created cache folder.");
        }

        try {
            JsonObject data = GsonUtility.getObject("https://raw.githubusercontent.com/danieldieeins/SerwiN/refs/heads/master/data.json");
            JsonStorage config = new JsonStorage("./serwin.json");
            config.ensure("settings.updater.targetVersion", "latest");
            config.ensure("settings.updater.usedVersion", data.get("recommendedVersion").getAsString());

            String target = config.getString("settings.updater.targetVersion");
            if (config.getString("settings.updater.targetVersion").equalsIgnoreCase("latest")) {
                target = data.get("recommendedVersion").getAsString();
            }

            if (!target.startsWith("v")) {
                target = "v" + target;
            }

            if (target.equals(config.getString("settings.updater.usedVersion")) && serwin.exists()) {
                launch(args);
                return;
            } else {
                if (serwin.exists()) {
                    serwin.delete();
                }
                JsonArray versions = data.getAsJsonArray("versions");
                for (int i = 0; i < versions.size(); i++) {
                    JsonObject version = versions.get(i).getAsJsonObject();
                    if (version.get("tag").getAsString().equals(target)) {
                        String url = version.get("download").getAsString();
                        FileGetter.downloadFile(url, serwin.getAbsolutePath());
                        config.set("settings.updater.usedVersion", version.get("tag"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Couldn't check for latest version or update SerwiN, launching last used version if exists.");
        }


        if (serwin.exists()) {
            launch(args);
        } else {
            throw new FileNotFoundException("Couldn't find or download SerwiN (path: './cache/serwin-executable.jar'). Check your internet connection!");
        }
    }

    private static void launch(String[] args) {
        try {
            List<String> command = new ArrayList<>();
            command.add("java");
            command.add("-jar");
            command.add("-Xms" + Runtime.getRuntime().maxMemory());
            command.add("-Xmx" + Runtime.getRuntime().maxMemory());
            command.add(serwin.getAbsolutePath());
            if (args != null) {
                command.addAll(Arrays.asList(args));
            }

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
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

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {                System.out.println("Shutting down serwin and paper (if running)");
                System.out.println("Shutting down serwin and paper (if running)");
                process.destroy();
                System.out.println("bye :3");
            }));

            CompletableFuture.runAsync(() -> {
                while (true) {
                    try {
                        String line = scanner.nextLine();
                        BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                        processInput.write(line + "\n");
                        processInput.flush();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            process.waitFor();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}