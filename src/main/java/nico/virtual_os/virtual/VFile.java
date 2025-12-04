package nico.virtual_os.virtual;

import nico.virtual_os.lua.LuaFile;

public class VFile implements VNode, LuaFile {

    private String name;
    private VDirectory parentDirectory;
    private String data;


    public VFile(String name, VDirectory parentDirectory, String data) {
        this.name = name;
        this.parentDirectory = parentDirectory;
        this.data = data;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    public VDirectory getParentDirectory() {
        return parentDirectory;
    }

    public String getData() {
        return data;
    }
}
