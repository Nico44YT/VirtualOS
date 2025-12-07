package nico.os;

import nico.os.lua.LuaOperatingSystem;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public record LuaSystemAccess(VirtualOperatingSystem system) implements LuaOperatingSystem {

    @Override
    public void changeDirectory(String arguments) {
        String[] parts = arguments.split("/");

        for(String part : parts) {
            if(part.equals("..")) {
                if(system.getCurrentDirectory().getParent() != null) {
                    system.setCurrentDirectory(system.getCurrentDirectory());
                }
                continue;
            }

            system.getCurrentDirectory().getDirectory(part).ifPresent(system::setCurrentDirectory);
        }
    }

    @Override
    public void makeDirectory(String current_path, String arguments) {
        String[] parts = current_path.split("/");

        current_path = "";
        for(int i = 1;i<parts.length;i++) current_path += parts[i] + "/";

        system.getRootDirectory().getDirectory(current_path).ifPresent(dir -> system.makeDirectory(dir, arguments));
    }

    @Override
    public void print(String output) {
        system.print(output);
    }

    @Override
    public void clear() {
        system.clear();
    }

    @Override
    public void sleep(int milliseconds) {
        system.sleep(milliseconds);
    }

    @Override
    public LuaString awaitInput() {
        return LuaString.valueOf(system.getInput());
    }

    @Override
    public LuaValue getFile(String path) {
        String[] parts = path.split("/");

        path = "";
        for(int i = 1;i<parts.length - 1;i++) path += parts[i] + "/";

        var optionalDir = system.getRootDirectory().getDirectory(path);
        if(optionalDir.isPresent()) {
            var optionalFile = optionalDir.get().getFile(parts[parts.length - 1]);
            if(optionalFile.isPresent()) return CoerceJavaToLua.coerce(optionalFile.get());
        }

        return null;
    }

    @Override
    public LuaTable getFiles(String path) {
        String[] parts = path.split("/");

        path = "";
        for(int i = 1;i<parts.length;i++) path += parts[i] + "/";

        var dir = system.getRootDirectory().getDirectory(path);
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

        var dir = system.getRootDirectory().getDirectory(path);
        var files = dir.map(vDirectory -> vDirectory.getChildren().toArray(VNode[]::new)).orElse(new VNode[0]);

        LuaTable table = new LuaTable();

        int counter = 0;
        for(var file : files) {
            table.set(++counter, CoerceJavaToLua.coerce(file));
        }

        return table;
    }

    @Override
    public LuaString getVersion() {
        return LuaString.valueOf("v2.0");
    }

    @Override
    public void tone(int hz, int durationMs) {
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
}
