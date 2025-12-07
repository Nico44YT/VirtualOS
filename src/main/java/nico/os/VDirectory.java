package nico.os;

import nico.os.lua.LuaDirectory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class VDirectory implements VNode, LuaDirectory {

    private String name;
    private VDirectory parent;

    private Set<VNode> children;

    public VDirectory(String name, VDirectory parent) {
        this.name = name;
        this.parent = parent;

        this.children = new HashSet<>();
    }

    public Set<VNode> getChildren() {
        return this.children;
    }

    public Set<VDirectory> getDirectories() {
        return this.children.stream().filter($ -> $ instanceof VDirectory).map($ -> (VDirectory)$).collect(Collectors.toSet());
    }

    public Set<VFile> getFiles() {
        return this.children.stream().filter($ -> $ instanceof VFile).map($ -> (VFile)$).collect(Collectors.toSet());
    }

    @Override
    public String toPath() {
        String path = this.getName() + "/";

        VDirectory prevDir = this;
        while(prevDir.getParent() != null) {
            prevDir = prevDir.getParent();
            path = prevDir.getName() + "/" + path;
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

    public Optional<VDirectory> getDirectory(String name) {
        VDirectory current = this;

        if(name.isEmpty()) return Optional.of(this);

        for (String part : name.split("/")) {
            var optional = current.getDirectories().stream()
                    .filter(child -> child.getName().equals(part))
                    .findAny();

            if (optional.isEmpty()) {
                return Optional.empty(); // stop early if one segment fails
            }

            current = optional.get(); // go deeper
        }

        return Optional.of(current); // SUCCESS: return the final directory
    }

    public void addFile(String fileName, String data) {
        this.getChildren().add(new VFile(fileName, this, data));
    }


    public VDirectory getOrCreateDirectory(String name) {
        Optional<VDirectory> optional = this.getDirectory(name);

        return optional.orElseGet(() -> this.addDirectory(name));
    }

    public VDirectory addDirectory(String name) {
        VDirectory prevDir = this;
        for (String string : name.split("/")) {
            var dir = new VDirectory(string, prevDir);
            prevDir.children.add(dir);

            prevDir = dir;
        }

        return prevDir;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VDirectory otherDir && otherDir.getName().equals(this.getName());
    }

    public Optional<VFile> getFile(String fileName) {
        return getFiles().stream().filter(file -> file.getName().equals(fileName)).findFirst();
    }
}
