package nico.os;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

public abstract class VirtualOperatingSystem {
    private final VDirectory ROOT;
    private VDirectory currentDir;
    private final LuaSystemAccess LUA_SYSTEM_ACCESS;
    private static final Globals LUA_GLOBALS = JsePlatform.standardGlobals();

    public VirtualOperatingSystem() {
        this.ROOT = new VDirectory("root", null);

        this.currentDir = ROOT;
        this.LUA_SYSTEM_ACCESS = new LuaSystemAccess(this);
    }

    public abstract String getInput();
    public abstract void print(String output);
    public abstract void clear();
    public abstract void loadDefaultFiles(VDirectory rootDir);

    public void addFile(String path, String data) {
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];

        path = "";
        for(int i = 1;i<parts.length - 1;i++) path += parts[i] + "/";

        ROOT.getOrCreateDirectory(path.replace(fileName, "")).addFile(fileName, data);
    }

    public void loop() {
        print(this.currentDir.toPath() + "> ");

        String input = getInput();

        processCommand(input);
    }

    public void init() {
        loadDefaultFiles(ROOT);

        ROOT.getDirectory("startup").flatMap(startupFolder -> startupFolder.getFile("startup.lua")).ifPresent(luaFile -> executeLua(luaFile, ""));

        while(true) {
            loop();
        }
    }

    public void interruptRunningProgram() {
        //TODO stop the lua thread form running
        clear();
    }

    protected void processCommand(String input) {
        String[] parts = input.split(" ", 2);

        getRootDirectory().getDirectory("bin/").ifPresentOrElse(binDir -> {

            if (!parts[0].endsWith(".lua"))
                parts[0] += ".lua";

            binDir.getFile(parts[0]).ifPresentOrElse(luaFile -> {
                String arguments = parts.length == 2 ? parts[1] : "";
                executeLua(luaFile, arguments);
            }, () -> {
                if(parts[0].replace(".lua", "").isEmpty()) return;
                print(String.format("The command \"%s\" couldn't be found.\n", parts[0].replace(".lua", "")));
            });
        }, () -> {

        });
    }

    protected void executeLua(VFile luaFile, String arguments) {
        LUA_GLOBALS.load(luaFile.getData().toString()).call();

        LuaValue executeFunction = LUA_GLOBALS.get("execute");

        executeFunction.call(
                CoerceJavaToLua.coerce(LUA_SYSTEM_ACCESS),                      // VirtualOperatingSystem
                CoerceJavaToLua.coerce(currentDir.toPath()),          // String
                CoerceJavaToLua.coerce(arguments)
        );
    }

    public VDirectory getRootDirectory() {
        return this.ROOT;
    }

    public VDirectory getCurrentDirectory() {
        return this.currentDir;
    }

    public void setCurrentDirectory(VDirectory currentDir) {
        this.currentDir = currentDir;
    }

    public void makeDirectory(VDirectory directory, String arguments) {
        directory.addDirectory(arguments);
    }

    public void sleep(int number) {
        try {
            Thread.sleep(number);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
