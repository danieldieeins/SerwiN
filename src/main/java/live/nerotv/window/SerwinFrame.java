package live.nerotv.window;

import live.nerotv.Serwin;

import javax.swing.*;
import java.awt.*;

public class SerwinFrame extends JFrame {

    public static SerwinFrame get(SerwinFrame frame) {
        String title = frame.getTitle();
        if(title.isEmpty()) {
            title = "SerwiN v"+ Serwin.serwin;
        }
        frame.setTitlebar(title,Color.black,Color.white);
        frame.init();
        frame.pack();
        return frame;
    }

    public void init() {

    }

    public void setTitlebar(String title, Color background, Color foreground) {
        setTitle(title);
        setTitleBackground(background);
        setTitleForeground(foreground);
    }

    public void setTitleBackground(Color color) {
        setBackground(color);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", color);
    }

    public void setTitleForeground(Color color) {
        getRootPane().putClientProperty("JRootPane.titleBarForeground", color);
    }
}