package nico.virtual_os.lua;

import org.luaj.vm2.LuaTable;

public interface LuaDirectory {
    LuaTable getFiles();
    LuaTable getDirectories();
    LuaTable getNodes();
}
