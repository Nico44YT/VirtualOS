package nico.virtualos.file;

import java.util.*;

public final class VPath {

    public static final VPath ROOT = VPath.of("/");

    private final boolean absolute;
    private final List<String> parts;

    private VPath(boolean absolute, List<String> parts) {
        this.absolute = absolute;
        this.parts = List.copyOf(parts);
    }

    /* =========================
       Factory methods
       ========================= */

    public static VPath of(String path) {
        Objects.requireNonNull(path, "path");

        boolean absolute = path.startsWith("/");
        String[] rawParts = path.split("/");

        Deque<String> normalized = new ArrayDeque<>();

        for (String part : rawParts) {
            if (part.isEmpty() || part.equals(".")) {
                continue;
            }
            if (part.equals("..")) {
                if (!normalized.isEmpty() && !normalized.peekLast().equals("..")) {
                    normalized.removeLast();
                } else if (!absolute) {
                    normalized.addLast("..");
                }
            } else {
                normalized.addLast(part);
            }
        }

        return new VPath(absolute, new ArrayList<>(normalized));
    }

    public static VPath root() {
        return new VPath(true, List.of());
    }

    /* =========================
       Accessors
       ========================= */

    public boolean isAbsolute() {
        return absolute;
    }

    public boolean isRoot() {
        return absolute && parts.isEmpty();
    }

    public List<String> parts() {
        return parts;
    }

    public String fileName() {
        return parts.isEmpty() ? null : parts.get(parts.size() - 1);
    }

    public VPath parent() {
        if (parts.isEmpty()) {
            return this;
        }
        return new VPath(absolute, parts.subList(0, parts.size() - 1));
    }

    /* =========================
       Path operations
       ========================= */

    public VPath resolve(String other) {
        return resolve(VPath.of(other));
    }

    public VPath resolve(VPath other) {
        if (other.absolute) {
            return other;
        }

        List<String> merged = new ArrayList<>(this.parts);
        merged.addAll(other.parts);
        return new VPath(this.absolute, merged);
    }

    /* =========================
       Filesystem traversal
       ========================= */

    public VNode resolveFrom(VDirectory root, VDirectory cwd) {
        VNode current = absolute ? root : cwd;

        for (String part : parts) {
            if (!(current instanceof VDirectory dir)) {
                return null;
            }
            current = dir.getChild(part).get();
            if (current == null) {
                return null;
            }
        }

        return current;
    }

    /* =========================
       Object overrides
       ========================= */

    @Override
    public String toString() {
        if (parts.isEmpty()) {
            return absolute ? "/" : ".";
        }
        return (absolute ? "/" : "") + String.join("/", parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VPath that)) return false;
        return absolute == that.absolute && parts.equals(that.parts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolute, parts);
    }
}