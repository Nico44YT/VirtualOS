package nico.virtualos.file;


public interface VNode {
    default boolean isFile() {
        return false;
    }
    default boolean isDirectory() {
        return false;
    }

    String getName();

    void delete();
    void rename(String newName);
    VPath getFilePath();
    VDirectory getParent();

    default VDirectory toDirectory() {
        return (VDirectory)this;
    }

    default VFile toFile() {
        return (VFile)this;
    }
}
