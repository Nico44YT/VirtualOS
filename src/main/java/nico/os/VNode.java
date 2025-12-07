package nico.os;

public interface VNode {
    String getName();
    VDirectory getParent();
    String toPath();
}
