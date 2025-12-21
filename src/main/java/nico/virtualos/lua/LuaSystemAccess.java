package nico.virtualos.lua;

import nico.virtualos.VirtualOperatingSystem;
import nico.virtualos.file.VDirectory;
import nico.virtualos.file.VFile;
import nico.virtualos.file.VNode;
import nico.virtualos.file.VPath;
import nico.virtualos.lua.file.LuaDirectory;
import nico.virtualos.lua.file.LuaFile;
import nico.virtualos.lua.file.LuaNode;
import nico.virtualos.lua.stub.LuaAPI;
import nico.virtualos.lua.stub.LuaModule;
import org.luaj.vm2.*;

import java.awt.event.KeyEvent;

@LuaModule("System")
public record LuaSystemAccess(VirtualOperatingSystem system) {

    @LuaAPI(returnType = void.class)
    public void sleep(int milliseconds) {
        system.sleep(milliseconds);
    }

    @LuaAPI(returnType = void.class)
    public void print(String output) {
        system.print(output);
    }

    @LuaAPI(returnType = void.class)
    public void clear() {
        system.clear();
    }

    @LuaAPI(returnType = void.class)
    public void resetCaret() {
        system.resetCaret();
    }

    @LuaAPI(returnType = void.class)
    public void setRawInput(boolean value) {
        system.setRawInput(value);
    }

    @LuaAPI(returnType = LuaValue.class)
    public LuaValue awaitInput() {
        if(system().isRawInput()) return LuaInteger.valueOf(system.getRawInput());
        return LuaString.valueOf(system.getInput());
    }

    @LuaAPI(returnType = String.class)
    public LuaString getVersion() {
        return LuaString.valueOf("VirtualOS v3.0");
    }

    @LuaAPI(returnType = LuaDirectory.class)
    public LuaValue createDirectory(String s) {
        VPath path = VPath.of(s);
        path = system.getFileSystem().currentDirectory.getFilePath().resolve(path);

        if(path.fileName() == null || !this.isDirectoryNameValid(path.fileName()).booleanValue()) return LuaValue.FALSE;

        if(system.getFileSystem().getNode(path) != null) return LuaValue.NIL;


        system.getFileSystem().createDirectory(path);
        return new LuaDirectory((VDirectory)system.getFileSystem().getNode(path)).coerce();
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean createFile(String s) {
        VPath path = system.getFileSystem().currentDirectory.getFilePath().resolve(s);
        VNode node = system.getFileSystem().getNode(path);

        if(!isFileNameValid(path.fileName()).booleanValue()) return LuaBoolean.FALSE;

        if(node == null) {
            VDirectory directory = system.getFileSystem().getOrCreateDirectory(path.parent());
            directory.addChild(new VFile(directory, path));
            return LuaBoolean.TRUE;
        }

        return LuaBoolean.FALSE;
    }

    @LuaAPI(returnType = LuaFile.class)
    public LuaValue getFile(String path) {
        VNode node = system.getFileSystem().getNode(VPath.of(path));

        if(node == null || node.isDirectory()) return LuaValue.NIL;

        return new LuaFile(node.toFile()).coerce();
    }

    @LuaAPI(returnType = String.class)
    public LuaString readFile(String path) {
        VNode node = system.getFileSystem().getNode(VPath.of(path));

        if(node == null || node.isDirectory()) return null;

        return LuaString.valueOf(node.toFile().getDataAsString());
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean setCwd(String path) {
        return LuaBoolean.valueOf(system.getFileSystem().setCwd(VPath.of(path)));
    }

    @LuaAPI(returnType = LuaNode[].class)
    public LuaTable listNodes(String path) {
        VNode node = system.getFileSystem().getNode(VPath.of(path));

        if (node.isDirectory()) {
            var array = node.toDirectory().getChildren().stream().map($ -> $.isFile() ? new LuaFile($.toFile()).coerce() : new LuaDirectory($.toDirectory()).coerce()).toArray(LuaValue[]::new);
            return VirtualOperatingSystem.toLuaArray(array);
        }

        return new LuaTable();
    }

    @LuaAPI(returnType = LuaFile[].class)
    public LuaTable listFiles(String path) {
        VNode node = system.getFileSystem().getNode(VPath.of(path));

        if (node.isDirectory()) {
            return VirtualOperatingSystem.toLuaArray(node.toDirectory().getChildren().stream().filter(VNode::isFile).map($ -> new LuaFile($.toFile()).coerce()).toArray(LuaValue[]::new));
        }

        return new LuaTable();
    }

    @LuaAPI(returnType = LuaDirectory[].class)
    public LuaTable listDirectories(String path) {
        VNode node = system.getFileSystem().getNode(VPath.of(path));

        if (node.isDirectory()) {
            return VirtualOperatingSystem.toLuaArray(node.toDirectory().getChildren().stream().filter(VNode::isDirectory).map($ -> new LuaDirectory($.toDirectory()).coerce()).toArray(LuaValue[]::new));
        }

        return new LuaTable();
    }

    @LuaAPI(returnType = String.class)
    public LuaValue getCharacter(int keyCode) {
        if (isShiftDown().booleanValue() && keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            return switch (keyCode) {
                case KeyEvent.VK_0 -> LuaString.valueOf("=");
                case KeyEvent.VK_1 -> LuaString.valueOf("!");
                case KeyEvent.VK_2 -> LuaString.valueOf("\"");
                case KeyEvent.VK_3 -> LuaString.valueOf("§");
                case KeyEvent.VK_4 -> LuaString.valueOf("$");
                case KeyEvent.VK_5 -> LuaString.valueOf("%");
                case KeyEvent.VK_6 -> LuaString.valueOf("&");
                case KeyEvent.VK_7 -> LuaString.valueOf("/");
                case KeyEvent.VK_8 -> LuaString.valueOf("(");
                case KeyEvent.VK_9 -> LuaString.valueOf(")");
                default -> LuaValue.NIL;
            };
        }

        if (isAlternativeGraphicsDown().booleanValue() && keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            return switch (keyCode) {
                case KeyEvent.VK_0 -> LuaString.valueOf("}");
                case KeyEvent.VK_2 -> LuaString.valueOf("²");
                case KeyEvent.VK_3 -> LuaString.valueOf("³");
                case KeyEvent.VK_7 -> LuaString.valueOf("{");
                case KeyEvent.VK_8 -> LuaString.valueOf("[");
                case KeyEvent.VK_9 -> LuaString.valueOf("]");
                default -> LuaValue.NIL;
            };
        }

        if (keyCode == KeyEvent.VK_SPACE) return LuaString.valueOf(" ");

        if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9 ||
                keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return LuaString.valueOf(String.valueOf((char)keyCode));
        }

        return LuaValue.NIL;
        //return LuaString.valueOf(KeyEvent.getKeyText(keyCode));
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isShiftDown() {
        return LuaBoolean.valueOf(system.isShiftDown());
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isControlDown() {
        return LuaBoolean.valueOf(system.isControlDown());
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isAlternativeGraphicsDown() {
        return LuaBoolean.valueOf(system.isAlternativeGraphicsDown());
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isCapsLockActive() {
        return LuaBoolean.valueOf(system.isCapsLockActive());
    }

    @LuaAPI(returnType = void.class)
    public void reboot() {
        system.reboot(false);
    }

    @LuaAPI(returnType = void.class)
    public void tone(int hz, int durationMs) {
        system.tone(hz, durationMs);
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean execute(String command) {
        return LuaBoolean.valueOf(system.processCommand(command));
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean execute(String command, String[] arguments) {
        return LuaBoolean.valueOf(system.processCommand(command, arguments));
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isFileNameValid(String name) {
        return LuaBoolean.valueOf(name.matches("[a-zA-Z_\\-+.]+(/+[a-zA-Z_\\-+.]+)*"));
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isDirectoryNameValid(String name) {
        return LuaBoolean.valueOf(name.matches("[a-zA-Z_\\-.]+(/+[a-zA-Z_\\-.]+)*"));
    }

    @LuaAPI(returnType = Boolean.class)
    public LuaBoolean isModifierKey(int code) {
        return LuaBoolean.valueOf(
                switch(code) {
                    case KeyEvent.VK_SHIFT, KeyEvent.VK_CONTROL, KeyEvent.VK_ALT_GRAPH, KeyEvent.VK_ALT -> true;
                    default -> false;
                }
        );
    }
}
