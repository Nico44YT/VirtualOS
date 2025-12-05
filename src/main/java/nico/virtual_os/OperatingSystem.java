package nico.virtual_os;

import nico.virtual_os.lua.LuaCommand;
import nico.virtual_os.lua.LuaDirectory;
import nico.virtual_os.lua.LuaFileSystem;
import nico.virtual_os.virtual.VDirectory;
import nico.virtual_os.virtual.VFile;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.Optional;
import java.util.Scanner;

public class OperatingSystem implements LuaFileSystem {
    private static final VDirectory rootDir = new VDirectory("/", null);
    private static VDirectory currentDir = rootDir;

    private static final Globals globals = JsePlatform.standardGlobals();

    public void init() {

        rootDir.addDirectory("home");
        rootDir.addDirectory("bin");

        BinCommands.loadDefaultCommands(rootDir);

        Scanner scanner = new Scanner(System.in);

        while(true) {
            System.out.printf("%s> ", currentDir.toPath());
            String input = scanner.nextLine();

            Optional<VFile> possibleCommand = rootDir.getFile(String.format("bin/%s.lua", input.split(" ")[0]));

            if(possibleCommand.isPresent()) {
                String data = possibleCommand.get().getData();

                globals.load(data).call();
                LuaValue executeFunction = globals.get("execute");

                LuaValue fileSystem = CoerceJavaToLua.coerce((LuaFileSystem)this);
                LuaValue parentFolder = CoerceJavaToLua.coerce((LuaDirectory)currentDir);

                String[] argumentsArray = input.split(" ");
                String arguments = "";

                for(int i = 1;i<argumentsArray.length;i++) {
                    arguments += argumentsArray[i];
                }

                try{
                    executeFunction.call(fileSystem, parentFolder, arguments);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.printf("Command '%s' not found, try 'help'\n", input);
            }
        }
    }

    @Override
    public void changeDirectory(String path) {
        String[] parts = path.split("/");

        for(int i = 0;i<parts.length;i++) {
            var part = parts[i];

            if(part.equals("..") && currentDir.getParent() != null) currentDir = currentDir.getParent();
            else currentDir.getDirectory(part).ifPresentOrElse($ -> currentDir = $, () -> System.out.println("Error occurred"));
        }
    }

    @Override
    public LuaDirectory getCurrentDirectory() {
        return currentDir;
    }

    @Override
    public LuaTable getCommands() {
        LuaTable table = new LuaTable();
        Globals locals = JsePlatform.standardGlobals();

        int[] i = {1};

        rootDir.getDirectory("bin").ifPresent(bin -> {
            bin.getChildren().stream()
                    .filter(node -> node instanceof VFile && node.getName().endsWith(".lua"))
                    .map(node -> (VFile) node)
                    .forEach(file -> {

                        // Load the script so description() exists
                        locals.load(file.getData()).call();

                        // Retrieve function
                        LuaValue descFn = locals.get("description");
                        String desc = "(no description)";

                        if (!descFn.isnil()) {
                            desc = descFn.call().tojstring();
                        }

                        // Store command
                        table.set(i[0]++, CoerceJavaToLua.coerce(
                                new LuaCommand(file.getName().replace(".lua", ""), desc)
                        ));
                    });
        });

        return table;
    }

    @Override
    public void createDirectory(LuaDirectory parentDir, LuaTable name) {
        ((VDirectory)parentDir).getOrCreateDirectory(name.get(1).toString());
    }

    @Override
    public void removeDirectory(String name) {

    }

    @Override
    public void reload() {
        BinCommands.loadDefaultCommands(rootDir);
    }

    public static enum FileSizes {
        BYTES(0, "byte", "byte")
        KILO_BYTE(1_000, "kilobyte", "kB"),
        MEGA_BYTE(1_000_000, "megabyte", "mB");

        private int size;
        private String label;
        private String shortLabel;
        
        FileSizes(int size, String label, String shortLabel) {
            this.size = size;
            this.label = label;
            this.shortLabel = shortLabel;
        }

        public int getSize() {
            return this.size;
        }

        public String getFullLabel() {
            return this.label;
        }

        public String getShortLabel() {
            return this.shortLabel;
        }

        public static FileSizes getSize(VNode node) {
            int nodeSize = node.getSize();
            FileSizes[] values = FileSizes.values();

            for(int i = 0;i<values.length;i++) {
                if(i == values.length-1) return values[i];

                if(values[i].getSize() < nodeSize && nodeSize < values[i+1].getSize()) return values[i];
            }

            return FileSizes.BYTES;
        }
    }
}
