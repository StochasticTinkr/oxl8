package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.stream.Stream;

public class Oxl8 {
    public static void main(String[] args) {
        Settings settings = new Settings();
        settings.setClockMultiplier(10);
        settings.setTimerFrequency(60);
        if (args.length != 0) {
            settings.getRomFile().setLocation(args[0]);
        }
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                final Chip8Controller chip8Controller = new Chip8Controller(settings);
                EventQueue.invokeLater(() -> {
                    try {
                        chip8Controller.start();
                    } catch (InterruptedException e) {
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
//    public static void main(String[] args) throws IOException {
//        if (args.length != 1) {
//            System.out.println("Please specify the rom to load");
//        }
//        String path = args[0];
//        final byte[] rom = loadRom(path);
//        final byte[] font = loadFontFromHexData(Oxl8.class.getClassLoader().getResourceAsStream("font.hex"));
//        if (font == null) {
//            System.out.println("Unable to load font");
//        }
//        EventQueue.invokeLater(() -> {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ignore) {
//            }
//            new App(font, rom, false, path).start();
//
//        });
//    }
//
//    private static byte[] loadRom(String path) throws IOException {
//        try (FileInputStream fileInputStream = new FileInputStream(path)) {
//            return fileInputStream.readAllBytes();
//        }
//    }
//
//
//    public static byte[] loadFontFromHexData(InputStream fontStream) {
//        if (fontStream == null) {
//            return null;
//        }
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fontStream))) {
//            ByteArrayOutputStream font = new ByteArrayOutputStream();
//
//            reader
//                    .lines()
//                    .map(line -> line.replaceFirst("//.*", ""))
//                    .map(line -> line.split("\\s+"))
//                    .flatMap(Stream::of)
//                    .mapToInt(s -> Integer.parseInt(s, 16))
//                    .forEachOrdered(font::write);
//
//            return font.toByteArray();
//        } catch (IOException | NumberFormatException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//    }
}
