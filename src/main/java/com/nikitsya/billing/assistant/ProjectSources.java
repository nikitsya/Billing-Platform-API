package com.nikitsya.billing.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProjectSources {
    private static final List<String> REPOSITORIES = List.of("Billing-Platform-API", "Billing-Platform-Frontend");
    private final ObjectMapper mapper;
    private final HttpClient client;
    private Snapshot cached;
    private Instant retryAfter = Instant.EPOCH;

    @Autowired
    public ProjectSources(ObjectMapper mapper) {
        this(mapper, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
    }

    ProjectSources(ObjectMapper mapper, HttpClient client) {
        this.mapper = mapper;
        this.client = client;
    }

    static boolean allowedPath(String path) {
        if (!path.matches("[A-Za-z0-9_/-]+(?:\\.[A-Za-z0-9_-]+)?") || path.contains("..")) return false;
        if (List.of("README.md", "pom.xml", "package.json", "Dockerfile").contains(path)) return true;
        if (path.startsWith("src/main/java/") || path.startsWith("src/test/java/")) return path.endsWith(".java");
        if (path.startsWith("src/main/resources/db/migration/")) return path.endsWith(".sql");
        if (path.startsWith("js/") || path.startsWith("test/")) return path.endsWith(".js");
        if (path.startsWith("css/")) return path.endsWith(".css");
        return path.equals("index.js") || path.equals("index.html")
                || path.matches("(overview|customers|products-and-prices|subscriptions|payments|ask-ai)/index\\.html");
    }

    public synchronized Snapshot snapshot() {
        Instant now = Instant.now();
        if (cached != null && now.isBefore(cached.expiresAt())) return cached;
        if (now.isBefore(retryAfter)) throw ChatException.unavailable();
        retryAfter = now.plusSeconds(60);
        Map<String, SourceFile> files = new LinkedHashMap<>();
        try {
            for (String repository : REPOSITORIES) {
                String api = "https://api.github.com/repos/nikitsya/" + repository;
                JsonNode commits = mapper.readTree(get(api + "/commits?per_page=1"));
                String commit = commits.path(0).path("sha").asText();
                if (!commit.matches("[a-f0-9]{40}")) throw ChatException.unavailable();
                JsonNode tree = mapper.readTree(get(api + "/git/trees/" + commit + "?recursive=1"));
                if (tree.path("truncated").asBoolean() || !tree.path("tree").isArray()) {
                    throw ChatException.unavailable();
                }
                int count = 0;
                for (JsonNode entry : tree.path("tree")) {
                    String path = entry.path("path").asText();
                    if (!"blob".equals(entry.path("type").asText())
                            || !"100644".equals(entry.path("mode").asText())
                            || entry.path("size").asLong(100001) > 100000 || !allowedPath(path)) continue;
                    String id = repository + "/" + path;
                    files.put(id, new SourceFile(id, repository, commit, path));
                    if (++count >= 200) break;
                }
            }
        } catch (IOException exception) {
            throw ChatException.unavailable();
        }
        if (files.isEmpty()) throw ChatException.unavailable();
        cached = new Snapshot(Map.copyOf(files), now.plusSeconds(3600), new ConcurrentHashMap<>());
        return cached;
    }

    public String read(Snapshot snapshot, String id) {
        SourceFile file = snapshot.files().get(id);
        if (file == null) throw ChatException.unavailable();
        return snapshot.contents().computeIfAbsent(id, ignored -> {
            String text = get("https://raw.githubusercontent.com/nikitsya/" + file.repository()
                    + "/" + file.commit() + "/" + file.path());
            if (text.length() > 12000)
                text = text.substring(0, 12000) + "\n[File excerpt truncated at 12,000 characters]";
            return text;
        });
    }

    private String get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Billing-Platform-Project-Assistant").GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body().length() > 8000000) throw ChatException.unavailable();
            return response.body();
        } catch (IOException exception) {
            throw ChatException.unavailable();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw ChatException.unavailable();
        }
    }

    public record Snapshot(Map<String, SourceFile> files, Instant expiresAt, Map<String, String> contents) {
        public String catalogue() {
            return String.join("\n", files.keySet().stream().sorted().toList());
        }
    }

    public record SourceFile(String id, String repository, String commit, String path) {
        public String url() {
            return "https://github.com/nikitsya/" + repository + "/blob/" + commit + "/" + path;
        }
    }
}
