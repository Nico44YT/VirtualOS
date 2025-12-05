package nico.virtual_os.lua;

public interface LuaOperatingSystem {
    LuaFileSystem getFileSystem();
    LuaUser getUser();
    String getUserInput();

    LuaTable getCommands();
    void reloadCommands();
}