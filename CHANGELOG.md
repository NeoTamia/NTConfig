# Changelog

## [0.1.1](https://github.com/NeoTamia/NTConfig/compare/v0.1.0...v0.1.1) (2026-02-08)


### ♻️ Code Refactoring

* Cleanup imports ([304e504](https://github.com/NeoTamia/NTConfig/commit/304e504e2fb27545357cbf5ce5d6e5f2bc5a5b83))
* **core:** Optimize version handling and format registration ([f2cb68c](https://github.com/NeoTamia/NTConfig/commit/f2cb68cff7830985435abbfe41dc5acce0eb2bda))
* **core:** Simplify migration manager initialization and remove redundant methods ([7b80d4f](https://github.com/NeoTamia/NTConfig/commit/7b80d4f6b68aea605d50d4c4fc8911e944a437c0))


### 🔧 Build System

* **deps:** Update dependency com.diffplug.spotless:spotless-plugin-gradle to v8.2.1 ([#20](https://github.com/NeoTamia/NTConfig/issues/20)) ([907252d](https://github.com/NeoTamia/NTConfig/commit/907252dba6bffddeba4f104fde09f6ad75b1593a))
* **deps:** Update dependency org.jetbrains.kotlin.jvm to v2.3.10 ([#24](https://github.com/NeoTamia/NTConfig/issues/24)) ([f974859](https://github.com/NeoTamia/NTConfig/commit/f974859f55f09f55daaa455804707bc40f4d2677))
* **deps:** Update gradle to v9.3.1 ([#21](https://github.com/NeoTamia/NTConfig/issues/21)) ([0330efa](https://github.com/NeoTamia/NTConfig/commit/0330efaf52c12f1283a8de70f1e0dae22f5b9b2c))

## [0.1.0](https://github.com/NeoTamia/NTConfig/compare/v0.0.1...v0.1.0) (2026-02-04)


### ✨ Features

* Add comment support for YAML and TOML serializers ([42bdafd](https://github.com/NeoTamia/NTConfig/commit/42bdafd6c540097133b6cb8709bc5798054df6cf))
* Add GitHub Actions workflows for CI/CD ([669ba34](https://github.com/NeoTamia/NTConfig/commit/669ba347327fdd1f5a645539f112f45d38c92902))
* Add GitHub issue and pull request templates ([68e8fd1](https://github.com/NeoTamia/NTConfig/commit/68e8fd16859dc255d14480c93d9e2983ccd47c85))
* **build:** Initialize Gradle build system with Kotlin JVM, plugins, and dependencies ([70d2d11](https://github.com/NeoTamia/NTConfig/commit/70d2d11104aa60f2991a31d60299ac514c81163a))
* **core:** Add `ConfigMigrationHelpers` for raw config migration utilities ([a2bd952](https://github.com/NeoTamia/NTConfig/commit/a2bd952cda343afeec5b59515a2f009d9d62b98e))
* **core:** Add `ConfigMigrationRegistry` for managing config migration steps ([152b2e6](https://github.com/NeoTamia/NTConfig/commit/152b2e66ec3fa5b11061cbeddb9906fff02a025e))
* **core:** Add `ConfigTreeMerger` utility for merging NightConfig trees ([54a1f6b](https://github.com/NeoTamia/NTConfig/commit/54a1f6bfcc4a5d66ee63697b023bfb2df8c76529))
* **core:** Add `FormatModule` and `FormatModules` for extensible config format registration ([db5c6f2](https://github.com/NeoTamia/NTConfig/commit/db5c6f27da2011562e20358606237d1fa076826d))
* **core:** Add `NamingStrategy` and support for custom naming conventions ([ee2066d](https://github.com/NeoTamia/NTConfig/commit/ee2066d0da2b66439f3e39348277b0e5bddf59ae))
* **core:** Add `save` and `load` methods with String path support and register `TypeAdapter` in `TypeAdapterRegistry` ([f127eae](https://github.com/NeoTamia/NTConfig/commit/f127eae840c90e15d6ab1c8212c10ed5bb461c95))
* **core:** Add `Saveable` and `SaveableCommented` interfaces with support in `NTConfig` ([545593f](https://github.com/NeoTamia/NTConfig/commit/545593f5c31a47f0f63ff979eb202b4bc706f286))
* **core:** Add migration methods to NTConfig ([9fdebbe](https://github.com/NeoTamia/NTConfig/commit/9fdebbe5d90692ea5af7bd8a92326c1ebf6a2333))
* **core:** Add YAML, JSON, and TOML format modules and registration utilities ([4f30b77](https://github.com/NeoTamia/NTConfig/commit/4f30b775db3818da229c8f502a6bdec73dfb8ae4))
* **core:** Enhance (de)serialization with adapter context and serializer refactor ([620e9da](https://github.com/NeoTamia/NTConfig/commit/620e9da3fc6e04a63a512007bab6b9e2df812cba))
* **core:** Implement serializer framework with modular support for JSON, TOML, and YAML ([5ba8d51](https://github.com/NeoTamia/NTConfig/commit/5ba8d511b26f649632954f24c9fe01f05ffeddf5))
* **core:** Improve error handling for serialization and deserialization in `NTConfig` ([a39b1ef](https://github.com/NeoTamia/NTConfig/commit/a39b1efa636e1c77c069d6c4dd0be46fab26ea71))
* **core:** Introduce unified configuration management system ([773511d](https://github.com/NeoTamia/NTConfig/commit/773511db14e74c1550f94a29007792ab8be6d292))
* **core:** Simplify TypeAdapter methods by removing AdapterContext parameter ([27c5094](https://github.com/NeoTamia/NTConfig/commit/27c5094b6877880598ce11474bcbf698866b9a06))
* **migration:** Implement configuration versioning and migration management ([80f1a92](https://github.com/NeoTamia/NTConfig/commit/80f1a9263fe4822fadb2ca53a7b87d9c417bb631))
* **project:** Modularize codebase and add JSON, TOML, and YAML support ([57936b9](https://github.com/NeoTamia/NTConfig/commit/57936b9a2f7b5aeb7e29b1cc059dbab13c2a38f3))


### 🐛 Bug Fixes

* Annotation issues ([03c64a2](https://github.com/NeoTamia/NTConfig/commit/03c64a2e137a91c56858efe2efc60aaf0ab959a8))
* **core:** Support for header comments in `CommentedFileConfig` ([27e9516](https://github.com/NeoTamia/NTConfig/commit/27e9516bfd2b7663eed41aa9fe0f7e8e161a7261))
* Ensure file handles are closed after saving configuration ([5764b3c](https://github.com/NeoTamia/NTConfig/commit/5764b3c6eca67d5777c782d6bca7f05db17960ea))
* Float deserialisation ([b44077e](https://github.com/NeoTamia/NTConfig/commit/b44077e8d2d8261839eaefe62fe881676ea3cfdc))


### 📚 Documentation

* Add `CODE_OF_CONDUCT.md` and `CONTRIBUTING.md` ([5179058](https://github.com/NeoTamia/NTConfig/commit/51790588feab8c98b7662cb2817425eea5e98663))
* **core:** Add javadoc for NTConfigException.java ([e51e603](https://github.com/NeoTamia/NTConfig/commit/e51e603af7e69402e18f42058af4dd46f24ba272))


### ♻️ Code Refactoring

* **build:** Convert implementation dependencies to api in build scripts ([939e7a3](https://github.com/NeoTamia/NTConfig/commit/939e7a3c1657181fb4d9bef6ba4f3eb6dcfda51a))
* **build:** Streamline module dependencies and publishing config (dev) ([a54b485](https://github.com/NeoTamia/NTConfig/commit/a54b48572196d20056c7608ae5388c405b358dec))
* Convert classes to data classes and remove redundant methods ([3c685f6](https://github.com/NeoTamia/NTConfig/commit/3c685f65d528a45ad828f06e5a4e82ef2d274b29))
* **core:** Extract serde error formatting logic into `SerdeErrorFormatter` ([150ea9a](https://github.com/NeoTamia/NTConfig/commit/150ea9a63426101a70c112e724aa2ffd750acca0))
* **core:** Integrate `SerdeContext` into `NTConfig` ([839a4bf](https://github.com/NeoTamia/NTConfig/commit/839a4bf5a6c32339ee5296d63faf9bea46a0c521))
* **core:** Remove `ConfigMerger` and old `ConfigMigrationManager` classes ([b4a005c](https://github.com/NeoTamia/NTConfig/commit/b4a005c4025beb8ae1501994e1c7ac279ddcc2da))
* **core:** Remove obsolete config annotations and serializers ([cf9f9f0](https://github.com/NeoTamia/NTConfig/commit/cf9f9f02047a2a5fd5d8658b9da9c4fdb1e9e89c))
* **core:** Remove unused enums, data classes, and TypeAdapters in `Main.kt` ([08127a5](https://github.com/NeoTamia/NTConfig/commit/08127a5d6c55dbb75fa3252d02ba0180ea0fb474))
* **core:** Rename ConfigVersion to MigrationVersion and update references ([0fab121](https://github.com/NeoTamia/NTConfig/commit/0fab121bd8a26e0f5a0f7185af9d2831f4ea609d))
* **core:** Reorganize migration packages and add javadocs ([af4f8a2](https://github.com/NeoTamia/NTConfig/commit/af4f8a262191fec6b13e0c9b7a0016e2d178a81f))
* **core:** Replace `ConfigMigrationTest` with `RawConfigMigrationTest` ([d950e01](https://github.com/NeoTamia/NTConfig/commit/d950e01be15af84c37bec832648da169789feea0))
* **core:** Simplify backup file name generation in `BackupManager` ([573ad04](https://github.com/NeoTamia/NTConfig/commit/573ad04ae8b76edf8d9d655dd304806038c00a98))
* **main:** Fix ktlint imports ([1deb543](https://github.com/NeoTamia/NTConfig/commit/1deb5438705bd6bd4d2e58fdad6602b685f9c5b8))
* Relocate Main.kt to re.neotamia.config package ([9d8c862](https://github.com/NeoTamia/NTConfig/commit/9d8c862c03e1948445051eee828ad21141431f44))
* Remove unused merge strategies and add migration tests ([6c1231d](https://github.com/NeoTamia/NTConfig/commit/6c1231d28e7831c32ab27a03a198fccdce0b500f))
* Simplify version comparison logic in ConfigMigrationManager ([535a2f2](https://github.com/NeoTamia/NTConfig/commit/535a2f2c81082db517213291ac89087bf2b8acc6))
* **spotless:** Fix imports ([ecf9bc5](https://github.com/NeoTamia/NTConfig/commit/ecf9bc524ae2fef9c2c20496a94444c3f97bfd47))
* **tests:** Apply code formatting improvements in `ConfigMigrationTest` ([b55b55d](https://github.com/NeoTamia/NTConfig/commit/b55b55d3e62feb6bdaded6f44758cb98aa8da859))
* **tests:** Replace `[@formatter](https://github.com/formatter):off` with `spotless:off` in `ConfigMigrationTest` ([93ae33b](https://github.com/NeoTamia/NTConfig/commit/93ae33b8babb62b5cb70d23d6731480cd9329cbf))
* Update TypeAdapter interface to support generic serialization/deserialization ([8291bbe](https://github.com/NeoTamia/NTConfig/commit/8291bbe557e8507fd9b94528e33bcfd8517d82d6))


### 🧪 Tests

* Add tests directories ([008a140](https://github.com/NeoTamia/NTConfig/commit/008a140e332e3bac16a88fe655d45ca4fadb9507))


### 🔧 Build System

* Add Release Please configuration ([9e8ad80](https://github.com/NeoTamia/NTConfig/commit/9e8ad80ed39cfb680fdfdca3077fe4fbe897fd63))
* Add Renovate configuration file for dependency management ([18d96f1](https://github.com/NeoTamia/NTConfig/commit/18d96f165a479684ab882b492888bb7c41c9907e))
* **deps:** Add BoostedYaml support ([a51d90d](https://github.com/NeoTamia/NTConfig/commit/a51d90d60b3f3f7f3a8bffb7019269b1c9ecdaca))
* **deps:** Add kotlinx.serialization.json and kaml for YAML support ([d0978f8](https://github.com/NeoTamia/NTConfig/commit/d0978f8cb4f6b79c72c07dd0448b9e2be9466942))
* **deps:** Add NightConfig library and update build configuration ([07513ce](https://github.com/NeoTamia/NTConfig/commit/07513ce5542c2852c3efa2d7987a945c4013ee1a))
* **deps:** Cleanup unused dependencies ([87690a4](https://github.com/NeoTamia/NTConfig/commit/87690a4dd80a798909adaeec296dd318919a92dd))
* **deps:** Update dependency com.diffplug.spotless:spotless-plugin-gradle to v8.2.0 ([#19](https://github.com/NeoTamia/NTConfig/issues/19)) ([57afbad](https://github.com/NeoTamia/NTConfig/commit/57afbad8d16330de38aaa6777c5cee000354abae))
* **deps:** Update dependency com.gradleup.shadow:shadow-gradle-plugin to v9.3.1 ([#14](https://github.com/NeoTamia/NTConfig/issues/14)) ([f1ff246](https://github.com/NeoTamia/NTConfig/commit/f1ff24611eae2e71bdbe01fcd9394e8d1bde4b0b))
* **deps:** Update gradle to v9.3.0 ([#16](https://github.com/NeoTamia/NTConfig/issues/16)) ([5f33853](https://github.com/NeoTamia/NTConfig/commit/5f338539a5785517f5f459337d2ffe8ff71d6cde))
* **deps:** Update NightConfig to version 3.10.0 ([5056d31](https://github.com/NeoTamia/NTConfig/commit/5056d31f79c5032ced8fa2cb8abc567b63349134))
* **deps:** Update NightConfig to version 3.10.1 ([b44077e](https://github.com/NeoTamia/NTConfig/commit/b44077e8d2d8261839eaefe62fe881676ea3cfdc))
* **deps:** Update NightConfig to version 3.9.0 and add .editorconfig ([47a17b3](https://github.com/NeoTamia/NTConfig/commit/47a17b332d784b71bafc23889cc41644593c322c))
* **deps:** Update NightConfig to version 3.9.1 and change module paths ([22dce53](https://github.com/NeoTamia/NTConfig/commit/22dce53215f4f94bae165aa08aa1b0f75c76d06d))
* **deps:** Upgrade `nightConfig` to 3.10.5 ([c184b8b](https://github.com/NeoTamia/NTConfig/commit/c184b8b110b8eae5427dc1060767b255aa0a2bb4))
* **deps:** Upgraded `nightConfig` to version 3.10.4. ([839a4bf](https://github.com/NeoTamia/NTConfig/commit/839a4bf5a6c32339ee5296d63faf9bea46a0c521))
* **editorconfig:** Update file pattern rules and format renovate.json5 file ([0426f09](https://github.com/NeoTamia/NTConfig/commit/0426f093e371b6884dd030ace9c8903d9e7c4932))
* **gradle:** Add Spotless plugin for code formatting ([c20f25b](https://github.com/NeoTamia/NTConfig/commit/c20f25be613d65af2904d7ae6593602e74b89306))
* **gradle:** Enable conditional publishing for modules ([242691a](https://github.com/NeoTamia/NTConfig/commit/242691aba4286e26f2612f190b98c25839cd7e6d))
* **gradle:** Exclude `src/test` directory from code cleanup tasks ([3234f37](https://github.com/NeoTamia/NTConfig/commit/3234f37a0137a8220308791e092523ce82b8331e))
* **gradle:** Remove `kotlin.code.style` property from `gradle.properties` ([bfea842](https://github.com/NeoTamia/NTConfig/commit/bfea842fe5e1ada02bc99aa809854b338e8ab492))
* **gradle:** Remove `mavenJava` publication from publishing configuration ([8169845](https://github.com/NeoTamia/NTConfig/commit/8169845e307eb53ca6df8c7891c904490af9266c))
* **gradle:** Remove stripped prefix for main jar ([1503cbb](https://github.com/NeoTamia/NTConfig/commit/1503cbbe12cd1124ac26411205119a745f903ef3))
* **gradle:** Unify module group names and set artifactId using kebab-case ([342ed87](https://github.com/NeoTamia/NTConfig/commit/342ed8710c0e1c009e9822818caafa6863ed1f72))
* **gradle:** Update `nightConfig` to version 3.10.2 ([31315b8](https://github.com/NeoTamia/NTConfig/commit/31315b82effacfe8a607dfec5e55da01d7e72205))
* Migrate to new project structure and update Gradle configuration ([d3f638a](https://github.com/NeoTamia/NTConfig/commit/d3f638a82a642ec6b6dca7c857c565a3290cd32d))
* Update Kotlin stdlib usage and add runtime dependency ([faa235f](https://github.com/NeoTamia/NTConfig/commit/faa235f9c2a030ba128f483456e1ccb5228072b0))


### 👷 Continuous Integration

* **project-setup:** Remove workflow ([303be84](https://github.com/NeoTamia/NTConfig/commit/303be84e2a541108f57cf89396c4d5a699a4bf09))
