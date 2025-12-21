package nico.virtualos.file;

import java.util.Optional;

public class VFileSystem {
    public final VDirectory ROOT_DIR;

    public VDirectory currentDirectory;

    public VFileSystem() {
        this.ROOT_DIR = VDirectory.of(null, VPath.ROOT);
        this.currentDirectory = this.ROOT_DIR;
    }

    public void createDirectory(VDirectory parent, String name) {
        VPath path = parent.getFilePath().resolve(name);
        createDirectory(path);
    }

    public void createDirectory(VPath path) {
        VDirectory prevDir = ROOT_DIR;
        for (String part : path.parts()) {
            VNode node = prevDir.getChild(part).orElseGet(() -> null);
            if(node == null) {
                var dir = VDirectory.of(prevDir, part);
                prevDir.addChild(dir);

                prevDir = dir;
                continue;
            }

            if(node.isDirectory()) {
                prevDir = node.toDirectory();
            }

            if(node.isFile()) throw new RuntimeException();
        }
    }

    public VNode getNode(VPath path) {
        VNode current;
        if(path.isAbsolute()) {
           current = ROOT_DIR;
        } else {
            current = currentDirectory;
        }

        for (String part : path.parts()) {
            if (!(current instanceof VDirectory dir)) {
                return null;
            }

            Optional<VNode> child = dir.getChild(part);
            if (child.isEmpty()) {
                return null;
            }

            current = child.get();
        }

        return current;
    }

    public VDirectory getOrCreateDirectory(VPath path) {
        if (!path.isAbsolute()) {
            throw new IllegalArgumentException("Path must be absolute");
        }

        VDirectory current = ROOT_DIR;

        for (String part : path.parts()) {
            Optional<VNode> child = current.getChild(part);

            if (child.isPresent()) {
                VNode node = child.get();
                if (!node.isDirectory()) {
                    throw new IllegalStateException(
                            "Path component is not a directory: " + part
                    );
                }
                current = (VDirectory) node;
            } else {
                VDirectory newDir = VDirectory.of(
                        current,
                        current.getFilePath().resolve(part)
                );
                current.addChild(newDir);
                current = newDir;
            }
        }

        return current;
    }

    public void addFile(String pathString, byte[] rawDataFromFile) {
        VPath path = VPath.of(pathString);

        if (!path.isAbsolute()) {
            throw new IllegalArgumentException("File path must be absolute");
        }

        VPath parentPath = path.parent();
        String fileName = path.fileName();

        VDirectory parentDir = getOrCreateDirectory(parentPath);

        Optional<VNode> existing = parentDir.getChild(fileName);
        if (existing.isPresent()) {
            VNode node = existing.get();
            if (node.isDirectory()) {
                throw new IllegalStateException(
                        "Cannot overwrite directory with file: " + path
                );
            }
            node.delete();
        }

        VFile file = new VFile(parentDir, path);
        file.setData(rawDataFromFile);
        parentDir.addChild(file);
    }

    public boolean setCwd(VPath path) {
        VNode node = getNode(
                path.isAbsolute()
                        ? path
                        : currentDirectory.getFilePath().resolve(path)
        );

        if (node instanceof VDirectory dir) {
            currentDirectory = dir;
            return true;
        }

        return false;
    }
}
