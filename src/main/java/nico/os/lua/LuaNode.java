package nico.os.lua;

public interface LuaNode {
    String getName();
    String toPath();
    boolean isFile();
    boolean isDirectory();
}
