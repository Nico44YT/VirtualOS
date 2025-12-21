package nico.virtualos.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VDirectory implements VNode {

    private VDirectory parent;
    private VPath path;
    private final List<VNode> children;

    public static VDirectory of(VDirectory parent, String name) {
        return new VDirectory(parent, parent.getFilePath().resolve(name));
    }

    public static VDirectory of(VDirectory parent, VPath path) {
        return new VDirectory(parent, path);
    }

    private VDirectory(VDirectory parent, VPath path) {
        this.parent = parent;
        this.path = path;
        this.children = new ArrayList<>();
    }

    public void addChild(VNode node) {
        this.children.add(node);
    }

    public Optional<VNode> getChild(String name) {
        return this.children.stream()
                .filter(node -> node.getName().equals(name))
                .findFirst();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public String getName() {
        return this.path.fileName();
    }

    @Override
    public void delete() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    public void removeChild(VNode node) {
        children.remove(node);
    }

    @Override
    public void rename(String newName) {
        this.path = this.path.parent().resolve(newName);
    }

    @Override
    public VPath getFilePath() {
        return this.path;
    }

    @Override
    public VDirectory getParent() {
        return this.parent;
    }

    public List<VNode> getChildren() {
        return this.children;
    }
}
