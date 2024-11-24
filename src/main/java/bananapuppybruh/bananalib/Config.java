package bananapuppybruh.bananalib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class Config {
    private final Path configFilePath;
    private final Map<String, ConfigOption<?>> options = new HashMap<>();

    public Config(String configFileName) {
        this.configFilePath = Paths.get(FabricLoader.getInstance().getConfigDir().toString() + "/" + configFileName + ".json");
        loadConfig();
    }

    public <T> void addOption(String key, T value) {
        this.addOption(new ConfigOption<>(key, value));
    }
    public void addOption(ConfigOption<?> option) {
        options.putIfAbsent(option.getKey(), option);
        serializeJson();
    }

    @SuppressWarnings("unchecked")
    public <T> T getOption(String key) {
        ConfigOption<?> option = options.get(key);
        if(option != null) {
            return (T) option.getValue();
        }
        return null;
    }

    public <T> void updateOption(ConfigOption<T> option, T newValue) {
        option.setValue(newValue);
        serializeJson();
    }

    private void serializeJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Object> serializedOptions = new HashMap<>();
        for(ConfigOption<?> option : options.values()) {
            serializedOptions.put(option.getKey(), option.getValue());
        }
        try(Writer writer = Files.newBufferedWriter(configFilePath)) {
            gson.toJson(serializedOptions, writer);
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private void deserializeJson() {
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(configFilePath)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> loadedOptions = gson.fromJson(reader, HashMap.class);
            if(loadedOptions != null) {
                for(Map.Entry<String, Object> entry : loadedOptions.entrySet()) {
                    options.put(entry.getKey(), new ConfigOption<>(entry.getKey(), entry.getValue()));
                }
            }
        } catch (FileNotFoundException e) {
            // Create a new config file.
            serializeJson();
        } catch (IOException e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
        }
    }

    private void loadConfig() {
        if(Files.exists(configFilePath)) {
            deserializeJson();
        } else {
            serializeJson();
        }
    }
}
