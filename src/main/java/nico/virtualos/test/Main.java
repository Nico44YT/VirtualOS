package nico.virtualos.test;

import nico.virtualos.VirtualOperatingSystem;
import nico.virtualos.implementation.SwingVirtualOperatingSystem;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        VirtualOperatingSystem operatingSystem = new SwingVirtualOperatingSystem();

        List<String> arguments = Arrays.stream(args).toList();

        final boolean skipMockBootSequence = arguments.contains("-skipboot");

        new Thread(() -> {
            operatingSystem.init(skipMockBootSequence);
        }).start();
    }
}
