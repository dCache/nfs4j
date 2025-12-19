#!/bin/bash

# Define paths
PROJECT_ROOT="${PROJECT_ROOT:-$(pwd)}"
JACOCO_VERSION="0.8.14"
MAVEN_REPO="$HOME/.m2/repository"
JACOCO_CLI_JAR="$MAVEN_REPO/org/jacoco/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar"
MERGED_EXEC="$PROJECT_ROOT/target/coverage-reports/merged.exec"
REPORT_DIR="$PROJECT_ROOT/target/coverage-reports/site"

# Ensure the report directory exists
mkdir -p "$REPORT_DIR"

# Debug: Print paths
echo "DEBUG: MAVEN_REPO: $MAVEN_REPO"
echo "DEBUG: JACOCO_DIR: $JACOCO_DIR"
echo "DEBUG: JACOCO_CLI_JAR: $JACOCO_CLI_JAR"
echo "DEBUG: Checking if $JACOCO_CLI_JAR exists..."

# Check if JaCoCo CLI JAR exists in Maven cache
if [ ! -f "$JACOCO_CLI_JAR" ]; then
    echo "Downloading JaCoCo CLI via Maven..."
    mvn dependency:get -Dartifact=org.jacoco:org.jacoco.cli:$JACOCO_VERSION:jar:nodeps -Ddest="$JACOCO_CLI_JAR" -DremoteRepositories=central::default::https://repo.maven.apache.org/maven2
    if [ $? -ne 0 ]; then
        echo "Error: Failed to download JaCoCo CLI JAR via Maven"
        exit 1
    fi
fi

# Check if JaCoCo CLI JAR exists
if [ ! -f "$JACOCO_CLI_JAR" ]; then
    echo "Error: JaCoCo CLI JAR not found at $JACOCO_CLI_JAR"
    exit 1
fi

# Find all jacoco-ut.exec files dynamically
EXEC_FILES=($(find "$PROJECT_ROOT" -name "jacoco-ut.exec" -type f))

# Check if any execution data files were found
if [ ${#EXEC_FILES[@]} -eq 0 ]; then
    echo "Error: No jacoco-ut.exec files found in $PROJECT_ROOT"
    exit 1
fi

# Check if all execution data files exist
for exec_file in "${EXEC_FILES[@]}"; do
    if [ ! -f "$exec_file" ]; then
        echo "Error: Execution data file not found at $exec_file"
        exit 1
    fi
done

# Merge execution data files
echo "Merging execution data files..."
java -jar "$JACOCO_CLI_JAR" merge "${EXEC_FILES[@]}" --destfile "$MERGED_EXEC"

if [ $? -ne 0 ]; then
    echo "Error: Failed to merge execution data files"
    exit 1
fi

# Generate the coverage report
echo "Generating coverage report..."

# Build classfiles and sourcefiles arguments dynamically
CLASSFILES_ARGS=()
SOURCEFILES_ARGS=()

for exec_file in "${EXEC_FILES[@]}"; do
    # Extract module path (e.g., core, dlm, rquota) from the exec file path
    module_path=$(dirname "$(dirname "$(dirname "$exec_file")")")
    module_name=$(basename "$module_path")

    echo "DEBUG: Module path: $module_path"
    # Add classfiles and sourcefiles arguments
    CLASSFILES_ARGS+=("--classfiles" "$module_path/target/classes")
    SOURCEFILES_ARGS+=("--sourcefiles" "$module_path/src/main/java")
done

# Generate the report with dynamic arguments
java -jar "$JACOCO_CLI_JAR" report "$MERGED_EXEC" \
    "${CLASSFILES_ARGS[@]}" \
    "${SOURCEFILES_ARGS[@]}" \
    --html "$REPORT_DIR"

if [ $? -ne 0 ]; then
    echo "Error: Failed to generate coverage report"
    exit 1
fi

echo "Coverage report generated successfully at ${REPORT_DIR}/index.html"