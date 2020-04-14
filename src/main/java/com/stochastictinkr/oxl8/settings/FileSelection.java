package com.stochastictinkr.oxl8.settings;

import lombok.Data;

@Data
public class FileSelection {
    private String location;
    private FileFormat format = FileFormat.AUTODETECT;

    public boolean isDefault() {
        return location == null || "".equals(location);
    }
}
