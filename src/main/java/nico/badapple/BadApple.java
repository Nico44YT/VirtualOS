package nico.badapple;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BadApple {
    public static void main(String[] args) throws Exception {
        URL videoFolder = ClassLoader.getSystemClassLoader().getResource("./video/frames");
        File folder = new File(videoFolder.getFile());

        List<String> frames = new ArrayList<>();

        for (File file : folder.listFiles()) {
            if (!file.isFile()) continue; // skip directories
            BufferedImage image = ImageIO.read(file);

            StringBuilder frame = new StringBuilder();
            for (int y = 0; y < image.getHeight(); y+=10) {
                for (int x = 0; x < image.getWidth(); x+=10) {
                    int rgb = image.getRGB(x, y);
                    int brightness = getBrightness(rgb);
                    frame.append(getCharacter(Math.abs(brightness - 255)));
                }
                frame.append("\\n");
            }
            frames.add(frame.toString());
        }

        File outputFile = new File(folder.getAbsolutePath(), "badapple.lua");

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        writer.write("function execute(os, current_path, arguments)\n");
        writer.write("  local time = 35\n");
        for(String frame : frames) {
            writer.write(String.format("  os:print(\"%s\")\n", frame));
            writer.write("  os:sleep(time)\n");
            writer.write("  os:clear()\n");
        }
        writer.write("end");

        writer.close();
    }

    // Converts RGB to a brightness value 0-255
    public static int getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }

    public static char getCharacter(int brightness) {
        if (brightness < 32) return '#';      // very dark ' '
        if (brightness < 64) return '8';      // dark
        if (brightness < 96) return '&';      // medium-dark
        if (brightness < 128) return '*';     // medium
        if (brightness < 160) return ':';     // medium-light
        if (brightness < 192) return '.';     // light
        return ' ';                            // very light / white
    }
}
