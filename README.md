# Employee Information Search Using Embeddings

A small Java 21 / Maven application that searches employee records with natural-language queries. Employee profiles are converted into local embedding vectors and ranked by cosine similarity, so queries can match on meaning and related terms instead of only exact keywords.

## Requirements

- Java 21 or newer
- Maven 3.8 or newer

No external API keys or services are required.

## Project layout

```text
pom.xml
src/main/java/com/example/empsearch/
  Main.java            # Loads employees, runs the interactive search loop
  Employee.java        # Employee record: id, name, department, designation, skills
  EmbeddingStore.java  # Converts profiles to vectors and ranks matches
src/main/resources/
  employees.csv        # Sample employee dataset
```

## How it works

1. `employees.csv` is loaded into `Employee` records.
2. Each record is converted into a hashed embedding vector by `EmbeddingStore`.
   - Skills are weighted highest, followed by designation, department, and name.
   - Common query words are ignored and common abbreviations are expanded, for example `ML` -> `machine learning`.
3. User queries are converted into vectors using the same process.
4. The store ranks employees by cosine similarity and displays the top matches.

This is a lightweight local embedding implementation intended for hands-on validation. For production semantic search, replace the vector generation inside `EmbeddingStore` with a pretrained embedding model or embedding API.

## Build

```bash
mvn clean package
```

## Run

Interactive mode:

```bash
mvn exec:java
```

or

```bash
java -jar target/emp-search-1.0.0.jar
```

Type a natural-language query at the `Search>` prompt. Use `exit` or `quit` to stop.

Run non-interactively with piped queries:

```bash
printf 'java developer\nmachine learning\nexit\n' | java -jar target/emp-search-1.0.0.jar
```

## Configure

### Employee dataset

Edit `src/main/resources/employees.csv` and rebuild:

```text
id|name|department|designation|skills
E001|Alice Johnson|Engineering|Senior Java Developer|Java;Spring Boot;Microservices;AWS
```

Format rules:

- Fields are separated by `|`.
- Skills are separated by `;` inside the last field.
- Lines starting with `#` are ignored.

### Search behavior

Tuning constants are in `src/main/java/com/example/empsearch/EmbeddingStore.java`:

- `DIMENSIONS` - size of the hashed embedding vectors.
- `STOP_WORDS` - words ignored during tokenization.
- `SYNONYMS` - abbreviation/alias expansion.
- Field weights in `embedEmployee` - currently name `1.0`, department `1.5`, designation `2.0`, skills `3.0`.

## Example queries

```text
java developer
machine learning
who knows kubernetes
financial analyst
ux designer
python
```
