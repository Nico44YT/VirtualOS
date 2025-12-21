package nico.virtualos.file;

import java.nio.charset.StandardCharsets;

public class VFile implements VNode {

    private VDirectory parent;
    private VPath path;
    private byte[] data;

    public VFile(VDirectory parent, VPath path) {
        this.parent = parent;
        this.path = path;
        this.data = new byte[0];
    }

    @Override
    public boolean isFile() {
        return true;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getDataAsString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public String getName() {
        return this.path.fileName();
    }

    @Override
    public void delete() {
        getParent().removeChild(this);
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
}
