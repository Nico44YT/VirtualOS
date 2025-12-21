package nico.virtualos.lua.stub;

import nico.virtualos.lua.LuaSystemAccess;
import nico.virtualos.lua.file.LuaDirectory;
import nico.virtualos.lua.file.LuaFile;
import nico.virtualos.lua.file.LuaNode;
import org.luaj.vm2.LuaBoolean;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaString;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public final class LuaStubGenerator {

    private static final Path OUT_DIR = Paths.get("ide-stubs");
    private static final Map<Class<?>, String> modules = new HashMap<>();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUT_DIR);

        List<Class<?>> classes = List.of(LuaSystemAccess.class, LuaDirectory.class, LuaFile.class, LuaNode.class);

        classes.forEach(clazz -> {
            modules.put(clazz, clazz.getAnnotation(LuaModule.class).value());
        });

        for (Class<?> aClass : classes) {
            generateModule(aClass);
        }

        generateGlobals();
    }

    private static void generateModule(Class<?> clazz) throws IOException {
        LuaModule module = clazz.getAnnotation(LuaModule.class);
        if (module == null) return;

        String moduleName = module.value();
        StringBuilder lua = new StringBuilder();

        lua.append("---@class ").append(moduleName).append("\n");
        lua.append("local ").append(moduleName).append(" = {}\n\n");

        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(LuaAPI.class)) continue;

            LuaAPI api = m.getAnnotation(LuaAPI.class);
            String name = api.name().isEmpty() ? m.getName() : api.name();

            for (Parameter p : m.getParameters()) {
                lua.append("---@param ")
                   .append(p.getName())
                   .append(" ")
                   .append(mapType(p.getType()))
                   .append("\n");
            }

            if (m.getReturnType() != void.class) {
                Class<?> returnType = m.getReturnType();

                if(m.getAnnotation(LuaAPI.class).returnType() != null) {
                    returnType = m.getAnnotation(LuaAPI.class).returnType();
                }

                lua.append("---@return ")
                   .append(mapType(returnType))
                   .append("\n");
            }

            lua.append("function ")
               .append(moduleName)
               .append(":")
               .append(name)
               .append("(");

            lua.append(Arrays.stream(m.getParameters())
                    .map(Parameter::getName)
                    .collect(Collectors.joining(", ")));

            lua.append(") end\n\n");
        }

        lua.append("return ").append(moduleName).append("\n");

        Files.writeString(
                OUT_DIR.resolve(moduleName.toLowerCase() + ".lua"),
                lua.toString()
        );
    }

    private static void generateGlobals() throws IOException {
        StringBuilder globals = new StringBuilder();

        modules.values().forEach(module -> {
            globals.append("---@type ").append(module).append("\n");
        });

        Files.writeString(OUT_DIR.resolve("globals.lua"), globals.toString());
    }

    private static String mapType(Class<?> type) {
        if (type.isArray()) return mapType(type.getComponentType()) + "[]";

        if (type == String.class || type == LuaString.class) return "string";
        if (type == int.class || type == Integer.class || type == LuaInteger.class) return "integer";
        if (type == boolean.class || type == Boolean.class || type == LuaBoolean.class) return "boolean";
        if (type == void.class) return "nil";

        return modules.getOrDefault(type, "any");
    }
}