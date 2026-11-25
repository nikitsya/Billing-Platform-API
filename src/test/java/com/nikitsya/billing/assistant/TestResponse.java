package com.nikitsya.billing.assistant;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

record TestResponse(int statusCode, String body) implements HttpResponse<String> {
    public HttpRequest request() {
        return null;
    }

    public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
    }

    public HttpHeaders headers() {
        return HttpHeaders.of(Map.of(), (name, value) -> true);
    }

    public Optional<SSLSession> sslSession() {
        return Optional.empty();
    }

    public URI uri() {
        return URI.create("https://example.test");
    }

    public HttpClient.Version version() {
        return HttpClient.Version.HTTP_1_1;
    }
}
