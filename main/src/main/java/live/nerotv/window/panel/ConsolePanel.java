package live.nerotv.window.panel;

import live.nerotv.window.SerwinFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;

public class ConsolePanel extends SerwinFrame {

    JPanel mainPanel = new JPanel(new BorderLayout());
    JPanel sideMenu = new JPanel(new BorderLayout());

    private final JTextArea textArea = new JTextArea();
    private final JScrollPane scrollPane = new JScrollPane(textArea);
    private final JTextField inputField = new JTextField();

    public ConsolePanel() {
        setTitleBackground(Color.decode("#202224"));
        setTitleForeground(Color.white);
        setLayout(new BorderLayout());

        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        setTitle("SerwiN Paper Console (Beta)");

        sideMenu.setBackground(Color.decode("#202224"));
        JLabel placeholder = new JLabel("SerwiN");
        placeholder.setForeground(Color.decode("#202224"));
        sideMenu.add(placeholder);

        String commandPlaceholder = "Write here...";
        inputField.addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                if(inputField.getText().equals(commandPlaceholder)) {
                    inputField.setText("");
                }
                inputField.setForeground(Color.WHITE);
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if(inputField.getText().isEmpty()) {
                    inputField.setText(commandPlaceholder);
                }
                inputField.setForeground(Color.LIGHT_GRAY);
                super.focusLost(e);
            }
        });
        inputField.setText(commandPlaceholder);
        inputField.setForeground(Color.LIGHT_GRAY);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputField, BorderLayout.SOUTH);
        add(mainPanel, BorderLayout.CENTER);
        add(sideMenu, BorderLayout.WEST);

        setSize(920, 540);
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
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        int value = verticalScrollBar.getValue();
        int extent = verticalScrollBar.getVisibleAmount();
        int max = verticalScrollBar.getMaximum();
        boolean isAtBottom = (value + extent) >= (max - 250);
        for (String line : text) {
            textArea.append(line + "\n");
        }
        if (isAtBottom) {
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }
}