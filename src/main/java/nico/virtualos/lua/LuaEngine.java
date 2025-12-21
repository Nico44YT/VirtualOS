package nico.virtualos.lua;

import nico.virtualos.VirtualOperatingSystem;
import nico.virtualos.file.VFile;
import nico.virtualos.file.VPath;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.*;

public class LuaEngine {
    private final Globals globals;

    private final VirtualOperatingSystem system;

    public LuaEngine(VirtualOperatingSystem system) {
        this.system = system;

        Globals globals = new Globals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new CoroutineLib());
        globals.load(new JseMathLib());
        globals.load(new LuajavaLib());
        LoadState.install(globals);
        LuaC.install(globals);

        globals.get("package").get("searchers").set(2, new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue moduleName) {
                String path = moduleName.tojstring().replace('.', '/') + ".lua";

                return new ZeroArgFunction() {
                    @Override
                    public LuaValue call() {
                        VFile file = system.getFileSystem().getNode(VPath.of("/" + path)).toFile();
                        if (file == null) {
                            return LuaValue.NIL;
                        }

                        try {
                            LuaValue chunk = globals.load(
                                    file.getDataAsString(),
                                    moduleName.tojstring()
                            );
                            return chunk.call();
                        } catch (Exception e) {
                            throw new LuaError(e);
                        }
                    }
                };
            }
        });


        this.globals = globals;
    }

    public void executeLua(VFile luaFile, LuaTable arguments) {
        globals.set("System", CoerceJavaToLua.coerce(system.createLuaAccess()));
        String code = luaFile.getDataAsString();

        globals.load(code).call();
        LuaValue executeFunction = globals.get("execute");

        executeFunction.call(
                LuaString.valueOf(system.getFileSystem().currentDirectory.getFilePath().toString()),
                arguments == null ? LuaValue.NIL : arguments
        );
    }

    public void stop(VirtualOperatingSystem system) {

    }
}
