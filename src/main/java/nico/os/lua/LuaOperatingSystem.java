package nico.os.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public interface LuaOperatingSystem {
    void changeDirectory(String arguments);
    void makeDirectory(String current_path, String arguments);
    void print(String output);
    void clear();
    void sleep(int milliseconds);
    void tone(int hz, int durationMs);
    LuaString awaitInput();
    LuaValue getFile(String path);
    LuaTable getFiles(String path);
    LuaTable getNodes(String path);
    LuaString getVersion();
}
