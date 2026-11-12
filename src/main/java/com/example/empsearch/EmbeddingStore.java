package com.example.empsearch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EmbeddingStore {
    private static final int DIMENSIONS = 256;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "and", "or", "of", "to", "in", "for", "with", "who", "what",
            "is", "are", "was", "were", "has", "have", "had", "knows", "know", "find",
            "search", "employee", "employees", "person", "people", "me", "any", "someone",
            "show", "list", "get", "give", "looking", "need", "needs", "want", "wants",
            "work", "works", "working", "experience", "experienced", "skill", "skills",
            "expertise", "expert", "good", "best", "top", "candidate", "candidates", "team",
            "role", "job", "position", "can", "could", "should", "would", "there", "their"
    );

    private static final Map<String, List<String>> SYNONYMS = Map.ofEntries(
            Map.entry("ml", List.of("machine", "learning")),
            Map.entry("ai", List.of("artificial", "intelligence")),
            Map.entry("nlp", List.of("natural", "language", "processing")),
            Map.entry("cv", List.of("computer", "vision")),
            Map.entry("dev", List.of("developer")),
            Map.entry("eng", List.of("engineer")),
            Map.entry("swe", List.of("software", "engineer")),
            Map.entry("se", List.of("software", "engineer")),
            Map.entry("sde", List.of("software", "engineer")),
            Map.entry("fe", List.of("frontend")),
            Map.entry("be", List.of("backend")),
            Map.entry("ui", List.of("user", "interface")),
            Map.entry("ux", List.of("user", "experience")),
            Map.entry("pm", List.of("project", "manager")),
            Map.entry("po", List.of("product", "owner")),
            Map.entry("ba", List.of("business", "analyst")),
            Map.entry("da", List.of("data", "analyst")),
            Map.entry("ds", List.of("data", "scientist")),
            Map.entry("db", List.of("database")),
            Map.entry("py", List.of("python")),
            Map.entry("js", List.of("javascript")),
            Map.entry("ts", List.of("typescript")),
            Map.entry("rb", List.of("ruby")),
            Map.entry("go", List.of("golang")),
            Map.entry("k8s", List.of("kubernetes")),
            Map.entry("ci", List.of("continuous", "integration")),
            Map.entry("cd", List.of("continuous", "deployment")),
            Map.entry("etl", List.of("extract", "transform", "load")),
            Map.entry("bi", List.of("business", "intelligence"))
    );

    private final Map<Employee, float[]> vectors = new LinkedHashMap<>();

    public void add(Employee employee) {
        vectors.put(employee, embedEmployee(employee));
    }

    public List<Match> search(String query, int topK) {
        float[] queryVector = embedQuery(query);
        double queryNorm = norm(queryVector);
        if (queryNorm == 0) {
            return List.of();
        }

        List<Match> matches = new ArrayList<>();
        for (Map.Entry<Employee, float[]> entry : vectors.entrySet()) {
            double score = cosine(queryVector, entry.getValue(), queryNorm, norm(entry.getValue()));
            if (score > 0) {
                matches.add(new Match(entry.getKey(), score));
            }
        }

        matches.sort(Comparator.comparingDouble(Match::score).reversed().thenComparing(match -> match.employee().id()));
        return matches.stream().limit(topK).toList();
    }

    private float[] embedEmployee(Employee employee) {
        float[] vector = new float[DIMENSIONS];
        addField(vector, employee.name(), 1.0f);
        addField(vector, employee.department(), 1.5f);
        addField(vector, employee.designation(), 2.0f);
        addField(vector, String.join(" ", employee.skills()), 3.0f);
        return vector;
    }

    private float[] embedQuery(String query) {
        float[] vector = new float[DIMENSIONS];
        addField(vector, query, 1.0f);
        return vector;
    }

    private void addField(float[] vector, String text, float weight) {
        for (String token : tokenize(text)) {
            for (String expanded : expand(token)) {
                vector[index(expanded)] += weight;
            }
        }
    }

    private List<String> tokenize(String text) {
        String normalized = text.toLowerCase()
                .replace("c++", "cpp")
                .replace("c#", "csharp")
                .replace(".net", "dotnet")
                .replace("node.js", "nodejs");
        String[] parts = normalized.replaceAll("[^a-z0-9]+", " ").trim().split("\\s+");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            if (!part.isEmpty() && !STOP_WORDS.contains(part)) {
                tokens.add(part);
            }
        }
        return tokens;
    }

    private List<String> expand(String token) {
        return SYNONYMS.getOrDefault(token, List.of(token));
    }

    private int index(String token) {
        return Math.floorMod(token.hashCode(), DIMENSIONS);
    }

    private double cosine(float[] a, float[] b, double normA, double normB) {
        double dot = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
        }
        return dot / (normA * normB);
    }

    private double norm(float[] vector) {
        double sum = 0;
        for (float value : vector) {
            sum += value * value;
        }
        return Math.sqrt(sum);
    }

    public record Match(Employee employee, double score) {
    }
}
