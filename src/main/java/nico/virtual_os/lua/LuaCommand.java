package nico.virtual_os.lua;

public record LuaCommand(String name, String description) {
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
