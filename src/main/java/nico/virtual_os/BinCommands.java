package nico.virtual_os;

import nico.virtual_os.lua.LuaCommand;
import nico.virtual_os.virtual.VDirectory;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class BinCommands {

    public static List<LuaCommand> commands;

    public static void loadDefaultCommands(VDirectory rootDir) {
        String path = "./root/" + rootDir.getName();

        URL rootFolder = ClassLoader.getSystemClassLoader().getResource(path);

        try {
            File rootFile = new File(rootFolder.getFile());

            for (File file : rootFile.listFiles()) {
                if(file.isDirectory()) {

                    VDirectory dir = rootDir.getOrCreateDirectory(file.getName());
                    loadDefaultCommands(dir);
                } else {
                    String fileName = file.getName();
                    rootDir.addFile("/" + fileName, getDataFromFile(file));
                }
            }
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDataFromFile(File file) throws Exception {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }
}
