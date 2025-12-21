package nico.virtualos.lua.file;

import nico.virtualos.VirtualOperatingSystem;
import nico.virtualos.file.VFile;
import nico.virtualos.lua.stub.LuaAPI;
import nico.virtualos.lua.stub.LuaModule;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Arrays;

@LuaModule("File")
public record LuaFile(VFile file) implements LuaNode {
    @LuaAPI(returnType = Integer[].class)
    public LuaTable getData() {
        byte[] bytes = file.getData();
        int[] unsignedBytes = new int[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            unsignedBytes[i] = Byte.toUnsignedInt(bytes[i]);
        }

        return VirtualOperatingSystem.toLuaArray(Arrays.stream(unsignedBytes).mapToObj(LuaInteger::valueOf).toArray(LuaValue[]::new));
    }

    @LuaAPI(returnType = String.class)
    public LuaString getDataAsString() {
        return LuaString.valueOf(file.getDataAsString());
    }

    @LuaAPI(returnType = Boolean.class)
    @Override
    public boolean isFile() {
        return true;
    }

    @LuaAPI(returnType = String.class)
    @Override
    public LuaString getName() {
        return LuaString.valueOf(file.getName());
    }

    @LuaAPI(returnType = LuaDirectory.class)
    @Override
    public LuaDirectory getParent() {
        return new LuaDirectory(file.getParent());
    }
}
