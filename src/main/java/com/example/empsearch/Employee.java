package com.example.empsearch;

import java.util.List;

public record Employee(String id, String name, String department, String designation, List<String> skills) {
}
