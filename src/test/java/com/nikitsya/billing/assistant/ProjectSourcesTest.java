package com.nikitsya.billing.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectSourcesTest {
    @Test
    void excludesSecretsHiddenFilesAndArbitraryPaths() {
        for (String path : new String[]{".env", "src/main/resources/application.properties", "../README.md",
                ".git/config", "https://example.com/file", "js/../../secrets.js", "key.pem", "docs/private.txt"}) {
            assertFalse(ProjectSources.allowedPath(path), path);
        }
        for (String path : new String[]{"README.md", "js/api.js", "css/style.css", "ask-ai/index.html",
                "src/main/java/com/nikitsya/billing/customer/api/CustomerController.java"}) {
            assertTrue(ProjectSources.allowedPath(path), path);
        }
    }

    @Test
    void cachesBothRepositoryTreesAndPinsReadFilesToTheirCommits() throws Exception {
        HttpClient client = mock(HttpClient.class);
        String sha = "a".repeat(40);
        String commits = "[{\"sha\":\"" + sha + "\"}]";
        String tree = """
                {"tree":[{"path":"README.md","type":"blob","mode":"100644","size":42},
                {"path":".env","type":"blob","mode":"100644","size":12}]}
                """;
        when(client.send(any(), any(HttpResponse.BodyHandler.class))).thenReturn(
                response(commits), response(tree), response(commits), response(tree), response("Project source"));
        ProjectSources sources = new ProjectSources(new ObjectMapper(), client);
        var snapshot = sources.snapshot();
        assertSame(snapshot, sources.snapshot());
        assertEquals(2, snapshot.files().size());
        String id = "Billing-Platform-Frontend/README.md";
        assertEquals("Project source", sources.read(snapshot, id));
        assertEquals("Project source", sources.read(snapshot, id));
        assertThrows(ChatException.class, () -> sources.read(snapshot, "https://attacker.example"));
        var captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(client, times(5)).send(captor.capture(), any(HttpResponse.BodyHandler.class));
        assertEquals("https://raw.githubusercontent.com/nikitsya/Billing-Platform-Frontend/" + sha + "/README.md",
                captor.getAllValues().get(4).uri().toString());
    }

    private HttpResponse<String> response(String body) {
        return new TestResponse(200, body);
    }
}
