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
import org.apache.maven.model.Repository;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            // Extracting file contents to new MavenLibraryResolver instance.
            final MavenLibraryResolver iLibraries = gson.fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), PluginLibraries.class)
                    .toMavenLibraryResolver();
            // Adding library(-ies) to the PluginClasspathBuilder.
            classpathBuilder.addLibrary(iLibraries);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
/*
        final File eLibrariesFile = new File(classpathBuilder.getContext().getDataDirectory().toFile(), "libraries.json");

        // Loading external (user-specified) libraries.
        try {
            // Extracting file contents to new MavenLibraryResolver instance.
            final MavenLibraryResolver eDependencies = gson.fromJson(new InputStreamReader(new FileInputStream(eLibrariesFile), StandardCharsets.UTF_8), PluginLibraries.class)
                    .toMavenLibraryResolver();
            // Adding library(-ies) to the PluginClasspathBuilder.
            classpathBuilder.addLibrary(eDependencies);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException(e);
        }

*/
    }

    private record PluginLibraries(Map<String, RepositoryInfo> repositories, List<String> dependencies) {

        public MavenLibraryResolver toMavenLibraryResolver() {
            final MavenLibraryResolver resolver = new MavenLibraryResolver();
            // Adding repositories to library resolver.
            repositories.entrySet().stream()
                    .map(entry -> {
                        final RemoteRepository.Builder builder = new RemoteRepository.Builder(entry.getKey(), "default", entry.getValue().url);
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
    private static final class RepositoryInfoDeserializer implements JsonDeserializer<RepositoryInfo> {
        // Singleton instance.
        public static final RepositoryInfoDeserializer INSTANCE = new RepositoryInfoDeserializer();

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
