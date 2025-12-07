package nico.os.lua;

import org.luaj.vm2.LuaString;

public interface LuaFile extends LuaNode {
    LuaString getData();

    @Override
    default boolean isFile() {
        return true;
    }

    @Override
    default boolean isDirectory() {
        return false;
    }
}
