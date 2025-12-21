package nico.virtualos.lua.file;

import nico.virtualos.file.VDirectory;
import nico.virtualos.lua.stub.LuaAPI;
import nico.virtualos.lua.stub.LuaModule;
import org.luaj.vm2.LuaString;

@LuaModule("Directory")
public record LuaDirectory(VDirectory directory) implements LuaNode {
    @LuaAPI(returnType = Boolean.class)
    @Override
    public boolean isDirectory() {
        return true;
    }

    @LuaAPI(returnType = String.class)
    @Override
    public LuaString getName() {
        return LuaString.valueOf(directory.getName());
    }

    @LuaAPI(returnType = LuaDirectory.class)
    @Override
    public LuaDirectory getParent() {
        return new LuaDirectory(directory.getParent());
    }
}
