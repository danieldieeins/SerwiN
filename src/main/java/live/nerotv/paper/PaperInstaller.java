package live.nerotv.paper;

import live.nerotv.Serwin;
import live.nerotv.window.forms.LoadingForm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PaperInstaller {

    private PaperBuild build;
    private String path = "";
    private LoadingForm loadingForm;

    public PaperInstaller(PaperBuild build) {
        this.build = build;
    }

    public PaperInstaller(PaperBuild build, String path) {
        this.build = build;
        this.path = path+"/";
    }

    public PaperBuild getBuild() {
        return build;
    }

    public void setBuild(PaperBuild build) {
        this.build = build;
    }

    public boolean install() {
        boolean r = false;
        if(Serwin.desktop) {
            loadingForm = Serwin.openLoadingForm("Paper","Please wait - We're checking for updates...");
        }
        Serwin.logger.log("Downloading "+build.getName()+"...");
        File cache = new File(path+"cache/");
        if(cache.exists()) {
            deleteFolder(cache);
        }
        cache.mkdirs();
        if(downloadFile(build.getURL(),path+"cache/"+build.getFilename()).exists()) {
            r = true;
        } else {
            Serwin.logger.error("Couldn't install "+build.getName()+": Not found");
            throw new RuntimeException("Couldn't install "+build.getName()+": Not found");
        }
        if(Serwin.desktop) {
            loadingForm.dispose();
        }
        return r;
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) {
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private File downloadFile(String urlString, String path) {
        if(Serwin.desktop) {
            loadingForm.setText("Please wait - Downloading Paper "+build.getVersion()+"-"+build.getBuildNumber()+"...");
        }
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                File outputFile = new File(path);
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
                if(Serwin.desktop) {
                    loadingForm.setText("Please wait - Installing Paper "+build.getVersion()+"-"+build.getBuildNumber()+"...");
                }
                return outputFile;
            }
        } catch (Exception ignore) {}
        return null;
    }

    public void destroyObject() {
        build.destroyObject();
        build = null;
        System.gc();
    }
}