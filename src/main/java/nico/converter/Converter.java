package nico.converter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class Converter {
    public static void main(String[] args) throws Exception {
        File folder = new File("G:\\Projects\\VirtualOS2\\video\\frames");

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

        File outputFile = new File(folder.getAbsolutePath(), "..\\badapple.lua");

        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

        writer.write("function drawFrame(os, frame, time)\n");
        writer.write("  os:print(frame)\n");
        writer.write("  os:sleep(time)\n");
        writer.write("  os:clear()\n");
        writer.write("end\n");

        writer.write("function execute(os, current_path, arguments)\n");
        writer.write("  local time = 35\n");
        for(String frame : frames) {
            writer.write(String.format("  drawFrame(os, \"%s\", time)\n", frame));
        }
        writer.write("end\n");

        writer.close();
    }

    // Converts RGB to a brightness value 0-255
    public static int getBrightness(int rgb) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        return (r + g + b) / 3;
    }

    public static String getCharacter(int brightness) {
        if (brightness > 200) return " ";
        if (brightness > 150) return "░";
        if (brightness > 100) return "▒";
        if (brightness > 50) return "▓";
        return "█";
    }
}
