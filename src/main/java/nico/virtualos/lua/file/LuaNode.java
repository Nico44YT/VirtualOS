package nico.virtualos.lua.file;

import nico.virtualos.lua.stub.LuaAPI;
import nico.virtualos.lua.stub.LuaModule;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

@LuaModule("Node")
public interface LuaNode {

    @LuaAPI(returnType = Boolean.class)
    default boolean isFile() {
        return false;
    }

    @LuaAPI(returnType = Boolean.class)
    default boolean isDirectory() {
        return false;
    }

    @LuaAPI(returnType = String.class)
    LuaString getName();

    @LuaAPI(returnType = LuaDirectory.class)
    LuaDirectory getParent();

    default LuaValue coerce() {
        return CoerceJavaToLua.coerce(this);
    }
}
