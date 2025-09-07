package re.neotamia.config.main;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import org.snakeyaml.engine.v2.api.*;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.Tag;

import java.util.List;
import java.util.Map;

public class Main {
    public static class Config {
        private String name = "Config";
        //        @ConfigVersion
        private int version = 2;
        @SerdeKey("isEnabled")
//        @ConfigProperty(name = "isEnabled", value = "Whether the configuration is enabled")
        @SerdeComment("Whether the configuration is enabled")
        private boolean enabled = true;
        //        @ConfigProperty(exclude = true)
        private float decimals = 0.5f;
        private double doubleValue = 0.123456789;
        private List<String> items = List.of("item1", "item2", "item3");
        private Map<String, String> settings = Map.of("key1", "value1", "key2", "value2");
        private NestedConfig nested = new NestedConfig();
        @SerdeComment("""
                Multiline ?
                oui ?""")
        private String multiline =
                """
                        This is a multiline
                        string example.
                        It preserves line breaks.
                        """.trim();
        private Test test = Test.OWO;
        private ResourceLocation resource = new ResourceLocation("example:resource_path");
        private boolean uneVariableOuLeNomPeutEtreTresLong = true;
        private int toto = 42;
        private int titi = 24;
    }

//    public static void main(String[] args) throws IOException, InterruptedException {
//        var config = CommentedFileConfig.builder("config-night.toml").sync().onSave(() -> System.out.println("oazeoazpieopazkeopaze")).build();

    /// /        var config =  CommentedFileConfig.of(Path.of("config-night.toml"));
    /// /        System.out.println(config);
    /// /        ObjectSerializer.standard().serializeFields(new Config(), config);
    /// /        System.out.println(config);
//        config.load();
//        System.out.println(config);
//        config.save();
//    }
    public static void main(String[] args) {
        Map<Tag, ConstructNode> tags = Map.of(
            new Tag("re.neotamia.config.main.Test"), (node) -> {
                if (node instanceof ScalarNode scalar)
                    return Test.valueOf(scalar.getValue());
                return null;
            }
        );

        Load load = new Load(LoadSettings.builder()
                .setParseComments(true)
                .setTagConstructors(tags)
                .build());
        Dump dump = new Dump(DumpSettings.builder()
                .setIndent(2)
                .setIndicatorIndent(2)
                .setIndentWithIndicator(true)
                .setDumpComments(true)
                .build());

        System.out.println(dump.dumpToString(Test.OWO));
        System.out.println(load.loadFromString("!!re.neotamia.config.main.Test 'OWO'"));
    }
}
