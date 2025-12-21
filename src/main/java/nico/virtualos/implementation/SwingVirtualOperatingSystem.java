package nico.virtualos.implementation;

import nico.virtualos.VirtualOperatingSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SwingVirtualOperatingSystem extends VirtualOperatingSystem {

    private final JTextArea textArea;
    private CompletableFuture<String> pendingInput = null;
    private CompletableFuture<Integer> pendingRawInput = null;

    private boolean shiftDown = false;
    private boolean controlDown = false;
    private boolean capsLock = false;
    private boolean altGraphicsDown = false;

    private int inputStartOffset = 0;

    public SwingVirtualOperatingSystem() {
        JFrame frame = new JFrame();
        frame.setMinimumSize(new Dimension(512 + 128, 512 + 128));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea();
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.WHITE);
        textArea.setCaretColor(Color.WHITE);
        textArea.setAutoscrolls(true);
        textArea.setDoubleBuffered(true); // ensure double buffering
        textArea.setLineWrap(true);
        textArea.setFont(new Font("consolas", Font.PLAIN, 16));

        // prevent edits before inputStartOffset
        textArea.addKeyListener(new KeyAdapter() {
            private boolean isCaretBeforeInput() {
                return textArea.getCaretPosition() < inputStartOffset;
            }
            private boolean handleDirectInput(KeyEvent e) {
                if (pendingRawInput != null && !pendingRawInput.isDone()) {
                    e.consume(); pendingRawInput.complete(e.getKeyCode());
                    return true;
                }
                return false;
            }
            @Override
            public void keyReleased(KeyEvent e) {
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();
                altGraphicsDown = e.isAltGraphDown();
                if (asleep || handleDirectInput(e) || isCaretBeforeInput() || rawInput) {
                    e.consume();
                    return;
                }
                super.keyReleased(e);
            }
            @Override
            public void keyTyped(KeyEvent e) {
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();
                altGraphicsDown = e.isAltGraphDown();
                if (asleep || handleDirectInput(e) || isCaretBeforeInput() || rawInput) {
                    e.consume();
                    return;
                }
                super.keyTyped(e);
            }
            @Override
            public void keyPressed(KeyEvent e) {
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();
                altGraphicsDown = e.isAltGraphDown();
                if (controlDown && e.getKeyCode() == KeyEvent.VK_C) {
                    e.consume();
                    interruptRunningProgram();
                    return;
                }

                if (asleep || handleDirectInput(e) || rawInput) {
                    e.consume();
                    return;
                } // Block backspace/delete before inputStartOffset

                if ((e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE)
                        && textArea.getCaretPosition() <= inputStartOffset) {
                    e.consume();
                    return;
                }

                // Enter submits input
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

        frame.setContentPane(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        frame.setVisible(true);
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
    public int getRawInput() {
        pendingRawInput = new CompletableFuture<>();

        try {
            return pendingRawInput.get();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void print(String output) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(output);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });
    }

    @Override
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            textArea.setText("");
            inputStartOffset = 0;
        });
    }

    @Override
    public void resetCaret() {
        SwingUtilities.invokeLater(() -> {
            textArea.setCaretPosition(0);
            inputStartOffset = 0;
        });
    }

    @Override
    public boolean isShiftDown() {
        return this.shiftDown;
    }

    @Override
    public boolean isControlDown() {
        return this.controlDown;
    }

    @Override
    public boolean isCapsLockActive() {
        return this.capsLock;
    }

    @Override
    public boolean isAlternativeGraphicsDown() {
        return this.altGraphicsDown;
    }

    @Override
    public void reboot(boolean skipMockBootSequence) {
        this.pendingRawInput = null;
        this.pendingInput = null;

        super.reboot(skipMockBootSequence);
    }
}
