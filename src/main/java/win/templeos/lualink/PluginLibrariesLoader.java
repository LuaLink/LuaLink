package win.templeos.lualink;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for downloading and loading internal and user-specified libraries.
 */
@SuppressWarnings("UnstableApiUsage")
public final class PluginLibrariesLoader implements PluginLoader {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(RepositoryInfo.class, RepositoryInfoDeserializer.INSTANCE)
            .setLenient()
            .create();

    @Override
    public void classloader(final @NotNull PluginClasspathBuilder classpathBuilder) {
        // Loading internal libraries.
        try (final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("paper-libraries.json")) {
            if (stream == null)
                throw new IOException("File 'paper-libraries.json' not found in the classpath.");
            // Extracting file contents to new MavenLibraryResolver instance.
            final MavenLibraryResolver iLibraries = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PluginLibraries.class)
                    .toMavenLibraryResolver(classpathBuilder.getContext().getLogger(), RepositoryContext.INTERNAL);
            // Adding library(-ies) to the PluginClasspathBuilder.
            classpathBuilder.addLibrary(iLibraries);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        // Saving the default 'libraries.json' file to the plugin's directory.
        final File eLibrariesFile = new File(classpathBuilder.getContext().getDataDirectory().toFile(), "libraries.json");
        if (eLibrariesFile.exists() == false) {
            try (final InputStream stream = this.getClass().getClassLoader().getResourceAsStream("libraries.json")) {
                if (stream == null)
                    throw new IOException("File 'libraries.json' not found in the classpath.");
                // Creating directories...
                classpathBuilder.getContext().getDataDirectory().toFile().mkdirs();
                // Copying the file to the plugin's directory. Existing file should not be overridden
                Files.copy(stream, eLibrariesFile.toPath());
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Loading external (user-specified) libraries from the 'libraries.json' file.
        if (eLibrariesFile.exists() == true) {
            try {
                // Extracting file contents to new MavenLibraryResolver instance.
                final MavenLibraryResolver eDependencies = gson.fromJson(new InputStreamReader(new FileInputStream(eLibrariesFile), StandardCharsets.UTF_8), PluginLibraries.class)
                        .toMavenLibraryResolver(classpathBuilder.getContext().getLogger(), RepositoryContext.USER_SPECIFIED);
                // Adding library(-ies) to the PluginClasspathBuilder.
                classpathBuilder.addLibrary(eDependencies);
            } catch (final FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record PluginLibraries(Map<String, RepositoryInfo> repositories, List<String> dependencies) {

        public MavenLibraryResolver toMavenLibraryResolver(final @NotNull Logger logger, final @NotNull RepositoryContext context) {
            final MavenLibraryResolver resolver = new MavenLibraryResolver();
            // Adding repositories to library resolver.
            repositories.entrySet().stream()
                    .map(entry -> {
                        // Preparing the RemoteRepository builder.
                        // This is filled in the next step, and re-used later to apply authentication credentials if specified.
                        RemoteRepository.Builder builder;
                        // Field 'MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR' was added in one of the recent Paper versions and may not be available on < 1.21.6.
                        try {
                            // Replacing Maven Central repository with a pre-configured mirror.
                            // See: https://docs.papermc.io/paper/dev/getting-started/paper-plugins/#loaders
                            if (entry.getValue().url.contains("maven.org") == true || entry.getValue().url.contains("maven.apache.org") == true) {
                                if (context == RepositoryContext.INTERNAL) {
                                    builder = new RemoteRepository.Builder(entry.getKey(), "default", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR);
                                } else {
                                    logger.warn("Found at least one Maven Central repository in use that was not automatically replaced. Adjust contents of 'libraries.json' to get rid of this warning.");
                                    logger.warn("While the plugin should work as usual, please keep in mind that downloading from Maven Central may be against their TOS and can be subject to a rate-limit.");
                                    logger.warn("- " + entry.getValue().url + " (" + context + ")");
                                    builder = new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue().url);
                                }
                            } else {
                                builder = new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue().url);
                            }
                        } catch (final NoSuchFieldError e) {
                            if (context == RepositoryContext.INTERNAL) {
                                builder = new RemoteRepository.Builder(entry.getKey(), "default", "https://maven-central.storage-download.googleapis.com/maven2");
                            } else {
                                logger.warn("Replacing Maven Central repository with pre-configured mirror failed. As of now, this feature is available only on Paper 1.21.6 #11 and higher.");
                                logger.warn("While the plugin should work as usual, please keep in mind that downloading from Maven Central may be against their TOS and can be subject to a rate-limit.");
                                logger.warn("- " + entry.getValue().url + " (" + context + ")");
                                builder = new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue().url);
                            }
                        }
                        // Building and adding Authentication if specified.
                        if (entry.getValue().username != null && entry.getValue().password != null)
                            builder.setAuthentication(new AuthenticationBuilder().addUsername(entry.getValue().username).addPassword(entry.getValue().password).build());
                        // Building and returning RemoteRepository object.
                        return builder.build();
                    })
                    .forEach(resolver::addRepository);
            // Adding dependencies to library resolver.
            dependencies.stream()
                    .map(value -> new Dependency(new DefaultArtifact(value), null))
                    .forEach(resolver::addDependency);
            // Returning library resolver instance.
            return resolver;
        }
    }

    /**
     * Holds information about maven repository.
     */
    private record RepositoryInfo(@NotNull String url, @Nullable String username, @Nullable String password) { /* DATA ONLY */ }

    /**
     * Represents the context in which the library is being resolved.
     */
    private enum RepositoryContext {
        INTERNAL, USER_SPECIFIED
    }

    /**
     * Converts JSON repository definition to {@link RepositoryInfo} object.
     *
     * <pre>{@code
     * {
     *     // Supports simple repository definition.
     *     "SomeRepository": "https://repo.example.com/public",
     *     // Supports username-password authentication.
     *     "SomePrivateRepository": {
     *         "url": "https://repo.example.com/private",
     *         "username": "RepositoryUsername",
     *         "password": "SecretRepositoryPassword123"
     *     }
     * }
     * }</pre>
     */
    private enum RepositoryInfoDeserializer implements JsonDeserializer<RepositoryInfo> {
        INSTANCE; // SINGLETON

        @Override
        public RepositoryInfo deserialize(final @NotNull JsonElement element, final @NotNull Type type, final @NotNull JsonDeserializationContext context) throws JsonParseException {
            // Handling simple definitions, where value is url.
            if (element instanceof JsonPrimitive)
                return new RepositoryInfo(element.getAsString(), null, null);
            // Handling more advanced definitions, where value is object containing url and (optionally) credentials.
            if (element instanceof JsonObject object)
                return new RepositoryInfo(
                        object.get("url").getAsString(),
                        object.has("username") ? object.get("username").getAsString() : null,
                        object.has("password") ? object.get("password").getAsString() : null
                );
            // Throwing exception if unexpected/unsupported/invalid format has been provided.
            throw new JsonParseException("Invalid repository format.");
        }
    }
}
