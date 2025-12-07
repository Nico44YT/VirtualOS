package nico.os;

import nico.os.lua.LuaOperatingSystem;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public abstract class VirtualOperatingSystem implements LuaOperatingSystem {
    private final VDirectory ROOT;
    private VDirectory currentDir;

    public VirtualOperatingSystem() {
        this.ROOT = new VDirectory("root", null);

        this.currentDir = ROOT;
    }

    public abstract String getInput();
    public abstract void print(String output);
    public abstract void clear();
    public abstract void loadDefaultFiles(VDirectory rootDir);

    public void addFile(String path, String data) {
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];

        path = "";
        for(int i = 1;i<parts.length - 1;i++) path += parts[i] + "/";

        ROOT.getOrCreateDirectory(path.replace(fileName, "")).addFile(fileName, data);
    }

    public void init() {
        loadDefaultFiles(ROOT);

        while(true) {
            print(this.currentDir.toPath() + "> ");

            String input = getInput();

            ROOT.getDirectory("bin/").ifPresent(binDir -> {
                String[] parts = input.split(" ", 2);

                if(!parts[0].endsWith(".lua")) parts[0] += ".lua";

                binDir.getFile(parts[0]).ifPresent(luaFile -> {
                    String arguments = parts.length == 1 ? "" : parts[1];

                    executeLua(luaFile, arguments);
                });
            });
        }
    }

    private static final Globals globals = JsePlatform.standardGlobals();
    protected void executeLua(VFile luaFile, String arguments) {
        globals.load(luaFile.getData().toString()).call();

        LuaValue executeFunction = globals.get("execute");

        executeFunction.call(
                CoerceJavaToLua.coerce(this),                      // VirtualOperatingSystem
                CoerceJavaToLua.coerce(currentDir.toPath()),          // String
                CoerceJavaToLua.coerce(arguments)                     // String
        );
    }

    @Override
    public void changeDirectory(String arguments) {
        String[] parts = arguments.split("/");

        for(String part : parts) {
            if(part.equals("..")) {
                if(currentDir.getParent() != null) {
                    currentDir = currentDir.getParent();
                }
                continue;
            }

            currentDir.getDirectory(part).ifPresent(dir -> {
                currentDir = dir;
            });
        }
    }

    @Override
    public LuaTable getFiles(String path) {
        String[] parts = path.split("/");

        path = "";
        for(int i = 1;i<parts.length;i++) path += parts[i] + "/";

        var dir = ROOT.getDirectory(path);
        var files = dir.map(vDirectory -> vDirectory.getFiles().toArray(VFile[]::new)).orElse(new VFile[0]);

        LuaTable table = new LuaTable();

        int counter = 0;
        for(var file : files) {
            table.set(++counter, CoerceJavaToLua.coerce(file));
        }

        return table;
    }

    @Override
    public LuaTable getNodes(String path) {
        String[] parts = path.split("/");

        path = "";
        for(int i = 1;i<parts.length;i++) path += parts[i] + "/";

        var dir = ROOT.getDirectory(path);
        var files = dir.map(vDirectory -> vDirectory.getChildren().toArray(VNode[]::new)).orElse(new VNode[0]);

        LuaTable table = new LuaTable();

        int counter = 0;
        for(var file : files) {
            table.set(++counter, CoerceJavaToLua.coerce(file));
        }

        return table;
    }

    @Override
    public LuaString awaitInput() {
        return LuaString.valueOf(getInput());
    }

    public VDirectory getRootDirectory() {
        return this.ROOT;
    }

    public VDirectory getCurrentDirectory() {
        return this.currentDir;
    }

    public LuaValue getFile(String path) {
        String[] parts = path.split("/");

        path = "";
        for(int i = 1;i<parts.length - 1;i++) path += parts[i] + "/";

        var optionalDir = ROOT.getDirectory(path);
        if(optionalDir.isPresent()) {
            var optionalFile = optionalDir.get().getFile(parts[parts.length - 1]);
            if(optionalFile.isPresent()) return CoerceJavaToLua.coerce(optionalFile.get());
        }

        return null;
    }

    @Override
    public void makeDirectory(String current_path, String arguments) {
        String[] parts = current_path.split("/");

        current_path = "";
        for(int i = 1;i<parts.length;i++) current_path += parts[i] + "/";

        ROOT.getDirectory(current_path).ifPresent(dir -> makeDirectory(dir, arguments));
    }

    public void makeDirectory(VDirectory directory, String arguments) {
        directory.addDirectory(arguments);
    }

    public static void tone(int hz, int durationMs) {
        float sampleRate = 44100f;
        int samples = (int)((durationMs / 1000.0) * sampleRate);

        byte[] buffer = new byte[samples];

        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * i * hz / sampleRate;
            buffer[i] = (byte)(Math.sin(angle) * 127);
        }

        AudioFormat format = new AudioFormat(
                sampleRate,
                8,    // sample size in bits
                1,    // mono
                true, // signed
                false // little endian
        );

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, samples);
            line.drain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sleep(int number) {
        try {
            Thread.sleep(number);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
