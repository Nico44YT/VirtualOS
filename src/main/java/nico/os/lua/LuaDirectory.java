package nico.os.lua;

public interface LuaDirectory extends LuaNode {
    @Override
    default boolean isFile() {
        return false;
    }

    @Override
    default boolean isDirectory() {
        return true;
    }
}
