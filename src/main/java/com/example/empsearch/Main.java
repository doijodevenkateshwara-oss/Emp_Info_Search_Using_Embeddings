package com.example.empsearch;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        List<Employee> employees = loadEmployees();
        EmbeddingStore store = new EmbeddingStore();
        employees.forEach(store::add);

        System.out.println("Loaded " + employees.size() + " employees. Type a query, or 'exit' to quit.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Search> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String query = scanner.nextLine().trim();
            if (query.isEmpty()) {
                continue;
            }
            if (query.equalsIgnoreCase("exit") || query.equalsIgnoreCase("quit")) {
                break;
            }

            List<EmbeddingStore.Match> matches = store.search(query, 5);
            if (matches.isEmpty()) {
                System.out.println("No matching employees found.");
                continue;
            }

            System.out.println("Top matches:");
            int rank = 1;
            for (EmbeddingStore.Match match : matches) {
                Employee employee = match.employee();
                System.out.printf("%d. %s (%s) - %s | %s | score %.3f%n",
                        rank++, employee.name(), employee.id(), employee.department(), employee.designation(), match.score());
                System.out.println("   Skills: " + String.join(", ", employee.skills()));
            }
        }
    }

    private static List<Employee> loadEmployees() throws IOException {
        try (InputStream input = Main.class.getResourceAsStream("/employees.csv")) {
            if (input == null) {
                throw new FileNotFoundException("employees.csv was not found on the classpath");
            }
            List<Employee> employees = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                boolean header = true;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    if (header) {
                        header = false;
                        continue;
                    }
                    String[] parts = line.split("\\|", -1);
                    if (parts.length < 5) {
                        continue;
                    }
                    List<String> skills = Arrays.stream(parts[4].split(";"))
                            .map(String::trim)
                            .filter(skill -> !skill.isEmpty())
                            .toList();
                    employees.add(new Employee(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            skills
                    ));
                }
            }
            return employees;
        }
    }
}
