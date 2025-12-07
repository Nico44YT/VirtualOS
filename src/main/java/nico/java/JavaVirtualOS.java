package nico.java;

import nico.os.VDirectory;
import nico.os.VirtualOperatingSystem;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

public class JavaVirtualOS extends VirtualOperatingSystem {

    private Scanner scanner;

    public JavaVirtualOS() {
        super();

        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getInput() {
        return this.scanner.nextLine();
    }

    @Override
    public void print(String output) {
        System.out.print(output);
    }

    @Override
    public void clear() {

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
