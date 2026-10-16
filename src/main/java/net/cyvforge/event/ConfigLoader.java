package net.cyvforge.event;

import net.cyvforge.CyvForge;
import net.cyvforge.config.ColorTheme;
import net.cyvforge.config.CyvClientColorHelper;
import net.cyvforge.config.CyvClientConfig;
import net.cyvforge.hud.HUDManager;
import net.cyvforge.hud.structure.DraggableHUDElement;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

public class ConfigLoader {
    public static final String NAME = "config.txt";
    public static final String PATH = "config/cyvforge/";
    public static final String FILEPATH = PATH + NAME;

    public static File configFile;

    private static volatile boolean loadedOk = false;

    public static void init(CyvClientConfig cfg) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> save(CyvForge.config, true)));

        File dir = new File(Minecraft.getMinecraft().mcDataDir, PATH);
        if (!dir.exists()) dir.mkdirs();
        configFile = new File(Minecraft.getMinecraft().mcDataDir, FILEPATH);

        try {
            File oldFile = new File(FILEPATH);
            if (!configFile.exists() && oldFile.isFile()) {
                java.nio.file.Files.copy(oldFile.toPath(), configFile.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            configFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        read(cfg);

        if (loadedOk) {
            LogManager.getLogger().info("Config file loaded! (" + configFile.getAbsolutePath() + ")");
            backup();
        } else {
            LogManager.getLogger().error("Config file " + configFile.getAbsolutePath()
                    + " could not be read, saving is disabled so the existing file is not overwritten with defaults.");
        }

    }

    private static void backup() {
        try {
            if (configFile.length() == 0) return;
            java.nio.file.Files.copy(configFile.toPath(), new File(configFile.getParentFile(), NAME + ".bak").toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LogManager.getLogger().warn("Could not back up config.txt", e);
        }
    }

    private static String readText() throws IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(configFile.toPath());
        try {
            return java.nio.charset.StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (java.nio.charset.CharacterCodingException e) {
            return new String(bytes);
        }
    }

    public static synchronized void read(CyvClientConfig cfg) {
        loadedOk = false;
        try (BufferedReader bufferedreader = new BufferedReader(new StringReader(readText()))) {
            String s;
            while ((s = bufferedreader.readLine()) != null) {
                String[] parts = s.split("=", 2);
                try {
                    if (parts.length == 2 && cfg.configFields.containsKey(parts[0])) {
                        cfg.configFields.get(parts[0]).set(parts[1]);
                    }
                } catch (Exception e) {
                    LogManager.getLogger().info("Config option \"" + Arrays.toString(parts) + "\" failed to load.");
                }

            }
            loadedOk = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set colors
        CyvClientColorHelper.checkColor(CyvClientConfig.getString("color1", "aqua"), CyvClientConfig.getString("color2", "white"));

        //set theme
        try {
            CyvForge.theme = ColorTheme.valueOf(CyvClientConfig.getString("theme", "CYVISPIRIA"));
        } catch (Exception e) {
            CyvForge.theme = ColorTheme.CYVISPIRIA;
        }

        for (DraggableHUDElement mod : HUDManager.registeredRenderers) {
            try {
                mod.readConfigFields();
            } catch (Exception e) {
                LogManager.getLogger().warn("HUD element " + mod.getClass().getSimpleName()
                        + " threw while reading its fields, using defaults for it", e);
            }
        }

        //decimal precision
        CyvForge.df.setMinimumIntegerDigits(1);
        if (CyvClientConfig.getBoolean("trimZeroes", true)) CyvForge.df.setMinimumFractionDigits(0);
        else CyvForge.df.setMinimumFractionDigits(CyvClientConfig.getInt("df",5));
        CyvForge.df.setMaximumFractionDigits(CyvClientConfig.getInt("df",5));
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setDecimalSeparator('.');
        CyvForge.df.setDecimalFormatSymbols(s);

    }

    public static synchronized void save(CyvClientConfig cfg, boolean isFinal) {
        if (configFile == null) return;
        if (!loadedOk) {
            LogManager.getLogger().warn("Config was not loaded successfully, skipping save to protect the existing config.txt");
            return;
        }

        for (DraggableHUDElement mod : HUDManager.registeredRenderers) {
            try {
                mod.saveConfigFields();
            } catch (Exception e) {
                LogManager.getLogger().warn("HUD element " + mod.getClass().getSimpleName()
                        + " threw while saving its fields, skipping it", e);
            }
        }

        StringBuilder sb = new StringBuilder();
        cfg.configFields.forEach((name, data) -> {
            String value = (data.value != null) ? data.value.toString() : "";
            sb.append(name).append('=').append(value).append('\n');
        });

        File tmp = new File(configFile.getParentFile(), NAME + ".tmp");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(tmp, false), java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            LogManager.getLogger().error("Failed to save the configuration to the temporary file – the old config.txt remains unchanged.", e);
            return;
        }

        try {
            try {
                java.nio.file.Files.move(tmp.toPath(), configFile.toPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                java.nio.file.Files.move(tmp.toPath(), configFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            LogManager.getLogger().info("CyvForge config saved!");
        } catch (IOException e) {
            LogManager.getLogger().error("Failed to replace config.txt", e);
        }
    }
}
