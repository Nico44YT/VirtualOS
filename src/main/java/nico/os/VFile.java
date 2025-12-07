package nico.os;

import nico.os.lua.LuaFile;
import org.luaj.vm2.LuaString;

public class VFile implements VNode, LuaFile {

    private String name;
    private VDirectory parent;
    private String data;

    public VFile(String name, VDirectory parent, String data) {
        this.name = name;
        this.parent = parent;

        this.data = data;
    }

    @Override
    public String toPath() {
        String path = this.getName();

        VDirectory prevDir = parent;
        while(prevDir.getParent() != null) {
            path = prevDir.getName() + "/";
            prevDir = prevDir.getParent();
        }

        return path;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public VDirectory getParent() {
        return this.parent;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VFile otherFile && otherFile.getName().equals(this.getName());
    }

    @Override
    public LuaString getData() {
        return LuaString.valueOf(this.data);
    }
}