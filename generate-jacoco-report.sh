#!/bin/bash
# Script to merge JaCoCo execution data files and generate an HTML coverage report.
# Define paths
PROJECT_ROOT="${PROJECT_ROOT:-$(pwd)}"
JACOCO_VERSION="0.8.14"
MAVEN_REPO="$HOME/.m2/repository"
JACOCO_CLI_JAR="$MAVEN_REPO/org/jacoco/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar"
JACOCO_DIR="$HOME/jacoco-$JACOCO_VERSION"
FALLBACK_JACOCO_CLI_JAR="$JACOCO_DIR/lib/jacococli.jar"
MERGED_EXEC="$PROJECT_ROOT/target/coverage-reports/merged.exec"
REPORT_DIR="$PROJECT_ROOT/target/coverage-reports/site"

# Ensure the report directory exists
mkdir -p "$REPORT_DIR"

# Check if JaCoCo CLI JAR exists in Maven cache
if [ ! -f "$JACOCO_CLI_JAR" ]; then
    echo "JaCoCo CLI JAR not found in Maven cache. Trying to download via Maven..."
    # Try to download via Maven
    mvn dependency:get -Dartifact=org.jacoco:org.jacoco.cli:$JACOCO_VERSION:jar:nodeps -Ddest="$JACOCO_CLI_JAR" > /dev/null 2>&1

    # Check if Maven download was successful
    if [ ! -f "$JACOCO_CLI_JAR" ]; then
        echo "Failed to download JaCoCo CLI JAR via Maven. Falling back to direct download..."
        # Fallback: Download only the CLI JAR file directly from GitHub
        mkdir -p "$JACOCO_DIR/lib"
        wget -q "https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/$JACOCO_VERSION/org.jacoco.cli-$JACOCO_VERSION-nodeps.jar" -O "$FALLBACK_JACOCO_CLI_JAR"
        if [ $? -ne 0 ]; then
            echo "Error: Failed to download JaCoCo CLI JAR"
            exit 1
        fi
        JACOCO_CLI_JAR="$FALLBACK_JACOCO_CLI_JAR"
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