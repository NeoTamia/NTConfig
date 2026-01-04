# Changelog

## [0.1.0](https://github.com/NeoTamia/NTConfig/compare/v0.0.1...v0.1.0) (2026-01-04)


### ✨ Features

* Add comment support for YAML and TOML serializers ([42bdafd](https://github.com/NeoTamia/NTConfig/commit/42bdafd6c540097133b6cb8709bc5798054df6cf))
* Add GitHub Actions workflows for CI/CD ([669ba34](https://github.com/NeoTamia/NTConfig/commit/669ba347327fdd1f5a645539f112f45d38c92902))
* Add GitHub issue and pull request templates ([68e8fd1](https://github.com/NeoTamia/NTConfig/commit/68e8fd16859dc255d14480c93d9e2983ccd47c85))
* **build:** Initialize Gradle build system with Kotlin JVM, plugins, and dependencies ([70d2d11](https://github.com/NeoTamia/NTConfig/commit/70d2d11104aa60f2991a31d60299ac514c81163a))
* **core:** Add `NamingStrategy` and support for custom naming conventions ([ee2066d](https://github.com/NeoTamia/NTConfig/commit/ee2066d0da2b66439f3e39348277b0e5bddf59ae))
* **core:** Add `save` and `load` methods with String path support and register `TypeAdapter` in `TypeAdapterRegistry` ([f127eae](https://github.com/NeoTamia/NTConfig/commit/f127eae840c90e15d6ab1c8212c10ed5bb461c95))
* **core:** Enhance (de)serialization with adapter context and serializer refactor ([620e9da](https://github.com/NeoTamia/NTConfig/commit/620e9da3fc6e04a63a512007bab6b9e2df812cba))
* **core:** Implement serializer framework with modular support for JSON, TOML, and YAML ([5ba8d51](https://github.com/NeoTamia/NTConfig/commit/5ba8d511b26f649632954f24c9fe01f05ffeddf5))
* **core:** Introduce unified configuration management system ([773511d](https://github.com/NeoTamia/NTConfig/commit/773511db14e74c1550f94a29007792ab8be6d292))
* **core:** Simplify TypeAdapter methods by removing AdapterContext parameter ([27c5094](https://github.com/NeoTamia/NTConfig/commit/27c5094b6877880598ce11474bcbf698866b9a06))
* **migration:** Implement configuration versioning and migration management ([80f1a92](https://github.com/NeoTamia/NTConfig/commit/80f1a9263fe4822fadb2ca53a7b87d9c417bb631))
* **project:** Modularize codebase and add JSON, TOML, and YAML support ([57936b9](https://github.com/NeoTamia/NTConfig/commit/57936b9a2f7b5aeb7e29b1cc059dbab13c2a38f3))


### 🐛 Bug Fixes

* Annotation issues ([03c64a2](https://github.com/NeoTamia/NTConfig/commit/03c64a2e137a91c56858efe2efc60aaf0ab959a8))
* Ensure file handles are closed after saving configuration ([5764b3c](https://github.com/NeoTamia/NTConfig/commit/5764b3c6eca67d5777c782d6bca7f05db17960ea))
* Float deserialisation ([b44077e](https://github.com/NeoTamia/NTConfig/commit/b44077e8d2d8261839eaefe62fe881676ea3cfdc))


### 📚 Documentation

* Add `CODE_OF_CONDUCT.md` and `CONTRIBUTING.md` ([5179058](https://github.com/NeoTamia/NTConfig/commit/51790588feab8c98b7662cb2817425eea5e98663))


### ♻️ Code Refactoring

* **build:** Convert implementation dependencies to api in build scripts ([939e7a3](https://github.com/NeoTamia/NTConfig/commit/939e7a3c1657181fb4d9bef6ba4f3eb6dcfda51a))
* Convert classes to data classes and remove redundant methods ([3c685f6](https://github.com/NeoTamia/NTConfig/commit/3c685f65d528a45ad828f06e5a4e82ef2d274b29))
* **core:** Remove obsolete config annotations and serializers ([cf9f9f0](https://github.com/NeoTamia/NTConfig/commit/cf9f9f02047a2a5fd5d8658b9da9c4fdb1e9e89c))
* **core:** Remove unused enums, data classes, and TypeAdapters in `Main.kt` ([08127a5](https://github.com/NeoTamia/NTConfig/commit/08127a5d6c55dbb75fa3252d02ba0180ea0fb474))
* Relocate Main.kt to re.neotamia.config package ([9d8c862](https://github.com/NeoTamia/NTConfig/commit/9d8c862c03e1948445051eee828ad21141431f44))
* Remove unused merge strategies and add migration tests ([6c1231d](https://github.com/NeoTamia/NTConfig/commit/6c1231d28e7831c32ab27a03a198fccdce0b500f))
* Simplify version comparison logic in ConfigMigrationManager ([535a2f2](https://github.com/NeoTamia/NTConfig/commit/535a2f2c81082db517213291ac89087bf2b8acc6))
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
* **deps:** Update NightConfig to version 3.10.0 ([5056d31](https://github.com/NeoTamia/NTConfig/commit/5056d31f79c5032ced8fa2cb8abc567b63349134))
* **deps:** Update NightConfig to version 3.10.1 ([b44077e](https://github.com/NeoTamia/NTConfig/commit/b44077e8d2d8261839eaefe62fe881676ea3cfdc))
* **deps:** Update NightConfig to version 3.9.0 and add .editorconfig ([47a17b3](https://github.com/NeoTamia/NTConfig/commit/47a17b332d784b71bafc23889cc41644593c322c))
* **deps:** Update NightConfig to version 3.9.1 and change module paths ([22dce53](https://github.com/NeoTamia/NTConfig/commit/22dce53215f4f94bae165aa08aa1b0f75c76d06d))
* **editorconfig:** Update file pattern rules and format renovate.json5 file ([0426f09](https://github.com/NeoTamia/NTConfig/commit/0426f093e371b6884dd030ace9c8903d9e7c4932))
* **gradle:** Add Spotless plugin for code formatting ([c20f25b](https://github.com/NeoTamia/NTConfig/commit/c20f25be613d65af2904d7ae6593602e74b89306))
* **gradle:** Exclude `src/test` directory from code cleanup tasks ([3234f37](https://github.com/NeoTamia/NTConfig/commit/3234f37a0137a8220308791e092523ce82b8331e))
* **gradle:** Remove `kotlin.code.style` property from `gradle.properties` ([bfea842](https://github.com/NeoTamia/NTConfig/commit/bfea842fe5e1ada02bc99aa809854b338e8ab492))
* **gradle:** Remove `mavenJava` publication from publishing configuration ([8169845](https://github.com/NeoTamia/NTConfig/commit/8169845e307eb53ca6df8c7891c904490af9266c))
* **gradle:** Unify module group names and set artifactId using kebab-case ([342ed87](https://github.com/NeoTamia/NTConfig/commit/342ed8710c0e1c009e9822818caafa6863ed1f72))
* **gradle:** Update `nightConfig` to version 3.10.2 ([31315b8](https://github.com/NeoTamia/NTConfig/commit/31315b82effacfe8a607dfec5e55da01d7e72205))
* Migrate to new project structure and update Gradle configuration ([d3f638a](https://github.com/NeoTamia/NTConfig/commit/d3f638a82a642ec6b6dca7c857c565a3290cd32d))
* Update Kotlin stdlib usage and add runtime dependency ([faa235f](https://github.com/NeoTamia/NTConfig/commit/faa235f9c2a030ba128f483456e1ccb5228072b0))


### 👷 Continuous Integration

* **project-setup:** Remove workflow ([303be84](https://github.com/NeoTamia/NTConfig/commit/303be84e2a541108f57cf89396c4d5a699a4bf09))
