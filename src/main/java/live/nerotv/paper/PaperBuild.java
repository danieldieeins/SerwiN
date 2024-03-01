package live.nerotv.paper;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import live.nerotv.Serwin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class PaperBuild {

    public static PaperBuild getLatest() {
        try {
            URL projectsUrl = new URL("https://papermc.io/api/v2/projects/paper");
            InputStream projectsInputStream = projectsUrl.openStream();
            JsonElement projectsJsonElement = JsonParser.parseReader(new InputStreamReader(projectsInputStream));
            JsonObject projectsJsonObject = projectsJsonElement.getAsJsonObject();
            JsonArray versions = projectsJsonObject.get("versions").getAsJsonArray();
            String latestVersion = versions.get(versions.size()-1).getAsString();
            URL versionUrl = new URL("https://papermc.io/api/v2/projects/paper/versions/" + latestVersion);
            InputStream versionInputStream = versionUrl.openStream();
            JsonElement versionJsonElement = JsonParser.parseReader(new InputStreamReader(versionInputStream));
            JsonObject versionJsonObject = versionJsonElement.getAsJsonObject();
            JsonArray builds = versionJsonObject.get("builds").getAsJsonArray();
            if(!builds.isEmpty()) {
                String latestBuild = builds.get(builds.size()-1).getAsString();
                return new PaperBuild(latestVersion,latestBuild);
            }
        } catch (Exception e) {
            Serwin.logger.error("Couldn't get latest download url for paper: "+e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public static PaperBuild getLatest(String version) {
        try {
            URL versionUrl = new URL("https://papermc.io/api/v2/projects/paper/versions/" + version);
            InputStream versionInputStream = versionUrl.openStream();
            JsonElement versionJsonElement = JsonParser.parseReader(new InputStreamReader(versionInputStream));
            JsonObject versionJsonObject = versionJsonElement.getAsJsonObject();
            JsonArray builds = versionJsonObject.get("builds").getAsJsonArray();
            if(!builds.isEmpty()) {
                String latestBuild = builds.get(builds.size()-1).getAsString();
                return new PaperBuild(version,latestBuild);
            }
        } catch (Exception e) {
            Serwin.logger.error("Couldn't get latest download url for paper: "+e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public static PaperBuild get(String version, String buildNumber) {
        return new PaperBuild(version,buildNumber);
    }



    private String buildNumber;
    private String version;
    private String name;
    private String url;

    public PaperBuild(String version, String buildNumber) {
        this.buildNumber = buildNumber;
        this.version = version;
        this.name = "paper-"+version+"-"+buildNumber;
        this.url = "https://api.papermc.io/v2/projects/paper/versions/"+version+"/builds/"+buildNumber+"/downloads/"+name+".jar";
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return name+".jar";
    }

    public String getURL() {
        return url;
    }

    public void destroyObject() {
        buildNumber = null;
        version = null;
        name = null;
        url = null;
        System.gc();
    }
}