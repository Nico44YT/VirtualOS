package nico.virtual_os.lua;

import org.luaj.vm2.LuaTable;

public interface LuaFileSystem {
    void changeDirectory(String path);
    LuaDirectory getCurrentDirectory();
    void createDirectory(LuaDirectory parentDir, String name);
    void removeDirectory(String name);
    void reload();
}
