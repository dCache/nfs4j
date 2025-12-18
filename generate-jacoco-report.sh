#!/bin/bash

# Define paths
PROJECT_ROOT="${PROJECT_ROOT:-$(pwd)}"
JACOCO_VERSION="0.8.14"
JACOCO_DIR="$HOME/jacoco-$JACOCO_VERSION"
JACOCO_CLI_JAR="$JACOCO_DIR/lib/jacococli.jar"
MERGED_EXEC="$PROJECT_ROOT/nfs4j-coverage/target/merged.exec"
REPORT_DIR="$PROJECT_ROOT/nfs4j-coverage/target/site"

# Ensure the report directory exists
mkdir -p "$REPORT_DIR"

# Install JaCoCo CLI if not already installed
if [ ! -f "$JACOCO_CLI_JAR" ]; then
    echo "Installing JaCoCo CLI..."
    mkdir -p "$JACOCO_DIR"
    wget -q "https://github.com/jacoco/jacoco/releases/download/v$JACOCO_VERSION/jacoco-$JACOCO_VERSION.zip" -O "/tmp/jacoco-$JACOCO_VERSION.zip"
    unzip -q "/tmp/jacoco-$JACOCO_VERSION.zip" -d "$JACOCO_DIR"
    rm -f "/tmp/jacoco-$JACOCO_VERSION.zip"
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