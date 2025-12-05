package nico.virtual_os.virtual;

import nico.virtual_os.lua.LuaDirectory;
import nico.virtual_os.lua.LuaFile;
import nico.virtual_os.lua.LuaNode;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.*;

public class VDirectory implements VNode, LuaDirectory {
    private String name;
    private VDirectory parent;
    private HashMap<String, VNode> children;

    public VDirectory(String name, VDirectory parent) {
        this.name = name;
        this.parent = parent;
        this.children = new HashMap<>();
    }

    public VDirectory getParent() {
        return parent;
    }

    public List<VNode> getChildren() {
        return children.values().stream().toList();
    }

    @Override
    public LuaTable getFiles() {
        LuaTable table = new LuaTable();
        int index = 1;

        for (VNode node : this.getChildren()) {
            if (node instanceof LuaFile) {
                table.set(index++, CoerceJavaToLua.coerce(node));
            }
        }

        return table;
    }
    @Override
    public LuaTable getDirectories() {
        LuaTable table = new LuaTable();
        int index = 1;

        for (VNode node : this.getChildren()) {
            if (node instanceof LuaDirectory) {
                table.set(index++, CoerceJavaToLua.coerce(node));
            }
        }

        return table;
    }

    @Override
    public LuaTable getNodes() {
        LuaTable table = new LuaTable();
        int index = 1;

        for (VNode node : this.getChildren()) {
            if (node != null) {
                table.set(index++, CoerceJavaToLua.coerce((LuaNode)node));
            }
        }

        return table;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean isFile() {
        return false;
    }

    public VDirectory addDirectory(String name) {
        VDirectory prevDir = this;
        for (String string : name.split("/")) {
            var dir = new VDirectory(string, prevDir);
            prevDir.children.put(dir.getName(), dir);

            prevDir = dir;
        }

        return prevDir;
    }

    public Optional<VFile> getFile(String name) {
        // Split into directory path + file name
        int idx = name.lastIndexOf('/');
        String fileName = (idx == -1) ? name : name.substring(idx + 1);
        String dirPath = (idx == -1) ? "" : name.substring(0, idx);

        // Get the directory
        Optional<VDirectory> dir = getDirectory(dirPath);

        // Look for the file inside that directory
        return dir.flatMap(vDirectory ->
                vDirectory.getChildren().stream()
                        .filter(child -> child instanceof VFile && child.getName().equals(fileName))
                        .map(child -> (VFile) child)
                        .findAny()
        );
    }

    public Optional<VDirectory> getDirectory(String name) {
        VDirectory current = this;

        for (String part : name.split("/")) {
            var optional = current.getChildren().stream()
                    .filter(child -> child instanceof VDirectory && child.getName().equals(part))
                    .map(child -> (VDirectory) child)
                    .findAny();

            if (optional.isEmpty()) {
                return Optional.empty(); // stop early if one segment fails
            }

            current = optional.get(); // go deeper
        }

        return Optional.of(current); // SUCCESS: return the final directory
    }

    public void addFile(String path, String data) {
        String[] parts = path.split("/");
        String fileName = parts[parts.length - 1];
        Optional<VDirectory> optional = getDirectory(path.replace(fileName, ""));

        optional.ifPresent(vDirectory -> vDirectory.children.put(fileName, new VFile(fileName, vDirectory, data)));
    }

    public VDirectory getOrCreateDirectory(String name) {
        Optional<VDirectory> optional = this.getDirectory(name);

        return optional.orElseGet(() -> this.addDirectory(name));

    }

    public String toPath() {
        String path = this.getName();

        var dir = this;
        while(dir.getParent() != null) {
            dir = dir.getParent();

            path = dir.getName() + path + "/";
        }

        return path;
    }

    @Override
    public int getSize() {
        int size = 0;

        for(VNode node : getChildren()) {
            size += node.getSize();
        }

        return size;
    }
}
