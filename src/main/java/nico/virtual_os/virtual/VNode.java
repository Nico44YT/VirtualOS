package nico.virtual_os.virtual;

import nico.virtual_os.lua.LuaNode;

public interface VNode extends LuaNode {
    String getName();

    int getSize();
    default String getFormatedSize() {
        FileSizes fileSizeInfo = FileSizes.getFormatedSize(this);
        return getSize() + fileSizeInfo.getShortLabel();
    }
}
