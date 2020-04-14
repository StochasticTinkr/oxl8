package com.stochastictinkr.oxl8;

import com.stochastictinkr.oxl8.settings.FileFormat;
import com.stochastictinkr.oxl8.settings.FileSelection;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FileLoader {
    public static final int AUTODETECT_PEEK_SIZE = 16;

    public byte[] load(FileSelection fileSelection) throws IOException {
        return load(fileSelection, Function.identity());
    }

    public <T> T load(FileSelection fileSelection, Function<byte[], T> create) throws IOException {
        return load(fileSelection, create, null);
    }

    public <T> T load(FileSelection fileSelection, Function<byte[], T> create, Supplier<? extends InputStream> defaultFile) throws IOException {
        byte[] data = null;
        if (!fileSelection.isDefault()) {
            try (InputStream stream = new FileInputStream(fileSelection.getLocation())) {
                FileFormat format = fileSelection.getFormat();
                switch (format) {
                    case HEX:
                        data = hexToBytes(stream);
                        break;
                    case BINARY:
                        data = stream.readAllBytes();
                        break;
                    case AUTODETECT:
                        data = readAutoDetected(stream);
                        break;
                }
            }
        }

        if (data == null && defaultFile != null) {
            data = readAutoDetected(defaultFile.get());
        }
        return create.apply(data);
    }

    private byte[] readAutoDetected(InputStream stream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);
        final byte[] prefix = peek(bufferedInputStream, AUTODETECT_PEEK_SIZE);
        String prefixString = new String(prefix, StandardCharsets.US_ASCII);
        if (looksLikeHex(prefixString)) {
            return hexToBytes(bufferedInputStream);
        } else {
            return bufferedInputStream.readAllBytes();
        }
    }

    private boolean looksLikeHex(String prefixString) {
        return prefixString.matches("^([a-fA-F0-9 ]|//.*)+$");
    }

    private byte[] peek(InputStream stream, int size) throws IOException {
        stream.mark(size);
        final byte[] prefix = stream.readNBytes(size);
        stream.reset();
        return prefix;
    }


    private static byte[] hexToBytes(InputStream stream) throws IOException {
        if (stream == null) {
            return null;
        }
        try (final InputStreamReader reader = new InputStreamReader(stream)) {
            return hexToBytes(reader);
        }
    }

    private static byte[] hexToBytes(Reader reader) throws IOException {
        if (reader instanceof BufferedReader) {
            return hexToBytes((BufferedReader) reader);
        }
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            return hexToBytes(bufferedReader);
        }
    }

    private static byte[] hexToBytes(BufferedReader reader) {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        reader
                .lines()
                .map(line -> line.replaceFirst("//.*", ""))
                .map(line -> line.split("\\s+"))
                .flatMap(Stream::of)
                .mapToInt(s -> Integer.parseInt(s, 16))
                .forEachOrdered(baos::write);

        return baos.toByteArray();
    }

}
