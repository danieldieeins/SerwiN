package live.nerotv.window.panel;

import live.nerotv.window.SerwinFrame;

import javax.swing.*;
import java.awt.*;

public class ConsolePanel extends SerwinFrame {

    private final JTextArea textArea = new JTextArea();
    private final JScrollPane scrollPane = new JScrollPane(textArea);
    private final JTextField inputField = new JTextField();

    public ConsolePanel() {
        setLayout(new BorderLayout());
        textArea.setEditable(false);
        textArea.setFocusable(false);

        setTitle("SerwiN Paper Console (Beta)");

        add(scrollPane, BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        setSize(800, 600);
        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public JTextField getInputField() {
        return inputField;
    }

    public void append(String... text) {
        for (String line : text) {
            textArea.append(line + "\n");
        }
    }
}