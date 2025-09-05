package re.neotamia.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import re.neotamia.config.Serializer;

import java.util.Set;

public class JsonSerializer implements Serializer {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public JsonSerializer() {}

    @Override
    public Object toTree(String data) {
        return gson.fromJson(data, Object.class);
    }

    @Override
    public String fromTree(Object tree) {
        return gson.toJson(tree);
    }

    @Override
    public @NotNull Set<String> getSupportedExtensions() {
        return Set.of("json");
    }
}
