package nico.virtual_os.lua;

import org.luaj.vm2.LuaTable;

public interface LuaFileSystem {
    void changeDirectory(String[] path);
    LuaDirectory getCurrentDirectory();
    LuaTable getCommands();
    void createDirectory(LuaDirectory parentDir, LuaTable name);
    void removeDirectory(String name);
    void reload();
}
