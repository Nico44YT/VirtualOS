package nico.virtualos;

import nico.virtualos.file.*;
import nico.virtualos.lua.LuaEngine;
import nico.virtualos.lua.LuaSystemAccess;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

public abstract class VirtualOperatingSystem {

    protected boolean asleep;
    protected boolean rawInput;

    protected final VFileSystem fileSystem;
    protected final LuaEngine luaEngine;

    protected String[] bootupText;

    protected Thread executionThread;

    protected VirtualOperatingSystem() {
        this.fileSystem = new VFileSystem();
        this.luaEngine = new LuaEngine(this);
    }

    /* =========================
       Abstract IO / Platform
       ========================= */

    public abstract String getInput();
    public abstract int getRawInput();

    public boolean isRawInput() {
        return this.rawInput;
    }

    public void setRawInput(boolean value) {
        this.rawInput = value;
    }

    public abstract void print(String output);
    public abstract void resetCaret();
    public abstract void clear();

    public abstract boolean isShiftDown();
    public abstract boolean isControlDown();
    public abstract boolean isCapsLockActive();
    public abstract boolean isAlternativeGraphicsDown();

    public LuaEngine getLuaEngine() {
        return this.luaEngine;
    }

    /* =========================
       Boot / Lifecycle
       ========================= */

    public void init(boolean skipMockBootSequence) {
        loadDefaultFiles(fileSystem.ROOT_DIR);
        loadBootupText();
        reboot(skipMockBootSequence);
    }

    public void reboot(boolean skipMockBootSequence) {
        if (executionThread != null) {
            clear();
            executionThread.interrupt();
        }

        executionThread = new Thread(() -> boot(skipMockBootSequence), "VirtualOS-Thread");
        executionThread.start();
    }

    protected void printBootSequence() {
        Random random = new Random();
        // Power-on delay
        sleep(200 + random.nextInt(200));

        // POST activity (dots with irregular pauses)
        int postDots = 40 + random.nextInt(25);
        for (int i = 0; i < postDots; i++) {
            print(".");
            sleep(random.nextInt(40, 120));

            // occasional longer pause like hardware probing
            if (random.nextInt(12) == 0) {
                sleep(random.nextInt(150, 350));
            }
        }

        // POST success beep
        tone(880, 120);
        sleep(100);

        print("\nPOST Successful\n");
        sleep(300 + random.nextInt(300));
        clear();

        // Boot log output
        for (String line : bootupText) {
            print(line + "\n");

            // Fast scrolling logs most of the time
            int delay = random.nextInt(20, 80);

            // Simulate module load / filesystem stall
            if (line.contains("mount")
                    || line.contains("Loading")
                    || line.contains("Starting")
                    || random.nextInt(18) == 0) {
                delay += random.nextInt(200, 500);
            }

            sleep(delay);
        }

        // Final handoff pause
        sleep(1000 + random.nextInt(800));
        clear();
    }

    protected void boot(boolean skipBootSequence) {
        if(!skipBootSequence) {
            printBootSequence();
        }
        runStartupScript();

        while (!Thread.currentThread().isInterrupted()) {
            loop();
        }
    }

    protected void loop() {
        print(this.fileSystem.currentDirectory.getFilePath().toString() + "> ");

        String input = getInput();

        print("\n");

        processCommand(input);
    }

    /* =========================
       Command Processing
       ========================= */

    public boolean processCommand(String input, String[] arguments) {
        if (input == null || input.isBlank()) return false;
        String command = input.endsWith(".lua") ? input : input + ".lua";

        var f = fileSystem.ROOT_DIR
                .getChild("bin")
                .flatMap(bin -> bin.toDirectory().getChild(command));

        if(f.isPresent()) {

            luaEngine.executeLua(
                    f.get().toFile(),
                    toLuaArray(Arrays.stream(arguments).map(LuaString::valueOf).toArray(LuaValue[]::new))
            );

            return true;
        }

        print("The command \"" + command.replace(".lua", "") + "\" couldn't be found.\n");
        return false;
    }

    public boolean processCommand(String input) {
        if(input.isEmpty()) return false;

        String[] parts = input.split(" ", 2);
        String arguments = parts.length == 2 ? parts[1] : "";
        String command = parts[0].endsWith(".lua") ? parts[0] : parts[0] + ".lua";

        if(parts[0].endsWith(".lua")) {
            var node = this.fileSystem.currentDirectory.getChild(parts[0]);

            if(node.isPresent()) {
                this.getLuaEngine().executeLua(
                        node.get().toFile(),
                        toLuaArray(Arrays.stream(arguments.split(" ")).map(LuaString::valueOf).toArray(LuaValue[]::new))
                );

                return true;
            }
        }


        return processCommand(command, arguments.split(" "));
    }

    /* =========================
       Startup
       ========================= */

    private void runStartupScript() {
        fileSystem.ROOT_DIR.getChild("startup").ifPresent(node -> {
            if (node.isFile()) {
                print("CRITICAL ERROR!\nstartup should be a directory, not a file\n");
                return;
            }

            node.toDirectory()
                    .getChild("startup.lua")
                    .ifPresent(file -> luaEngine.executeLua(file.toFile(), null));
        });
    }

    /* =========================
       Filesystem Bootstrap
       ========================= */

    protected void loadDefaultFiles(VDirectory rootDir) {
        String path = "./os/root" + rootDir.getFilePath();
        URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        if (resource == null) return;

        File rootFile = new File(resource.getFile());
        File[] files = rootFile.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                VDirectory dir = fileSystem.getOrCreateDirectory(
                        rootDir.getFilePath().resolve(file.getName())
                );
                loadDefaultFiles(dir);
            } else {
                try {
                    fileSystem.addFile(
                            rootDir.getFilePath() + "/" + file.getName(),
                            Files.readAllBytes(file.toPath())
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void loadBootupText() {
        String path = "./os/bootup_text.txt";
        URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        if (resource == null) return;

        File bootupTextFile = new File(resource.getFile());
        try {
            bootupText = Files.readAllLines(bootupTextFile.toPath(), StandardCharsets.UTF_8).toArray(String[]::new);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* =========================
       Utilities
       ========================= */

    public void sleep(int ms) {
        try {
            asleep = true;
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            asleep = false;
        }
    }

    public void interruptRunningProgram() {
        luaEngine.stop(this);
    }

    public LuaSystemAccess createLuaAccess() {
        return new LuaSystemAccess(this);
    }

    public static LuaTable toLuaArray(LuaValue[] args) {
        LuaTable table = new LuaTable();
        for (int i = 0; i < args.length; i++) {
            table.set(i + 1, args[i]);
        }
        return table;
    }

    public VFileSystem getFileSystem() {
        return this.fileSystem;
    }

    /* =========================
       Audio
       ========================= */

    public void tone(int hz, int durationMs) {
        float sampleRate = 44100f;
        int samples = (int) ((durationMs / 1000.0) * sampleRate);
        byte[] buffer = new byte[samples];

        for (int i = 0; i < samples; i++) {
            buffer[i] = (byte) (Math.sin(2 * Math.PI * i * hz / sampleRate) * 127);
        }

        AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format);
            line.start();
            line.write(buffer, 0, samples);
            line.drain();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}