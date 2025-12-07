package nico.test;

import nico.java.SwingVirtualOS;
import nico.os.VirtualOperatingSystem;

public class Main {
    public static void main(String[] args) {
        VirtualOperatingSystem os = new SwingVirtualOS();

        new Thread(os::init).start();
    }
}
