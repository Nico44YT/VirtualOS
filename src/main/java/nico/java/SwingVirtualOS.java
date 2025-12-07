package nico.java;

import nico.os.VDirectory;
import nico.os.VirtualOperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

public class SwingVirtualOS extends VirtualOperatingSystem {

    private final JTextArea textArea;
    private CompletableFuture<String> pendingInput = null;

    // index in the document where user input starts
    private int inputStartOffset = 0;

    public SwingVirtualOS() {
        JFrame frame = new JFrame();
        frame.setMinimumSize(new Dimension(512, 512));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setFont(new Font("consolas", Font.PLAIN, 16));

        // prevent edits before inputStartOffset
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (textArea.getCaretPosition() < inputStartOffset) {
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
                    e.consume();
                    interruptRunningProgram();
                    return;
                }

                // block backspace + delete before inputStartOffset
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                        e.getKeyCode() == KeyEvent.VK_DELETE) {

                    if (textArea.getCaretPosition() <= inputStartOffset) {
                        e.consume();
                        return;
                    }
                }

                // ENTER submits input
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();

                    if (pendingInput != null && !pendingInput.isDone()) {
                        String full = textArea.getText();
                        String input = full.substring(inputStartOffset).trim();
                        pendingInput.complete(input);
                    }
                }
            }
        });

        frame.setContentPane(new JScrollPane(textArea));
        frame.setVisible(true);
    }

    @Override
    public void print(String output) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(output);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    @Override
    public String getInput() {
        pendingInput = new CompletableFuture<>();

        // mark where user input is allowed to begin
        SwingUtilities.invokeLater(() -> {
            inputStartOffset = textArea.getDocument().getLength();
        });

        try {
            return pendingInput.get(); // block until Enter pressed
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void loop() {
        print(this.getCurrentDirectory().toPath() + "> ");

        String input = getInput();
        print("\n");

        processCommand(input);
    }

    @Override
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            textArea.setText("");
            inputStartOffset = 0;
        });
    }

    @Override
    public void loadDefaultFiles(VDirectory rootDir) {
        String path = "./os/" + rootDir.toPath();
        URL rootFolder = ClassLoader.getSystemClassLoader().getResource(path);

        try {
            assert rootFolder != null;
            File rootFile = new File(rootFolder.getFile());

            for (File subFile : rootFile.listFiles()) {
                if(subFile.isDirectory()) {
                    VDirectory dir = rootDir.getOrCreateDirectory(subFile.getName());
                    loadDefaultFiles(dir);
                } else {
                    String fileName = subFile.getName();
                    this.addFile(rootDir.toPath() + fileName, getDataFromFile(subFile));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDataFromFile(File file) throws Exception {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
}
