#!/bin/bash

# --- Configuration ---
DEFAULT_INPUT_JAR_NAME="google-adk.jar"
DEFAULT_DECOMPILER_JAR_NAME="cfr.jar"
DEFAULT_OUTPUT_XML_NAME="knowledge_base_full_content.xml"

# --- Determine Paths ---
# This script assumes it's run from the directory containing itself,
# the input JAR, and the decompiler JAR.
SCRIPT_EXEC_DIR=$(pwd) # Directory from which the script is run

# Allow overriding defaults with command-line arguments if desired (optional feature)
# Usage: ./script.sh [input_jar_name] [decompiler_jar_name] [output_xml_name]
INPUT_JAR_NAME="${1:-$DEFAULT_INPUT_JAR_NAME}"
DECOMPILER_JAR_NAME_CONFIG="${2:-$DEFAULT_DECOMPILER_JAR_NAME}"
OUTPUT_XML_NAME_CONFIG="${3:-$DEFAULT_OUTPUT_XML_NAME}"

# Construct full paths based on the script's execution directory
INPUT_JAR_FULL_PATH="$SCRIPT_EXEC_DIR/$INPUT_JAR_NAME"
DECOMPILER_FULL_PATH="$SCRIPT_EXEC_DIR/$DECOMPILER_JAR_NAME_CONFIG"
OUTPUT_XML_FULL_PATH="$SCRIPT_EXEC_DIR/$OUTPUT_XML_NAME_CONFIG"

DECOMPILE=true # Set to false if your JAR_FILE is a sources JAR (controls .class to .java conversion)

# --- Main Script ---
echo "Starting JAR to XML conversion for $INPUT_JAR_NAME..."
echo "Input JAR: $INPUT_JAR_FULL_PATH"
echo "Decompiler JAR: $DECOMPILER_FULL_PATH"
echo "Output XML: $OUTPUT_XML_FULL_PATH"
echo "Binary file content will be omitted."

# 1. Create a working directory (absolute path) and navigate into it
WORK_DIR_NAME="jar_extract_temp_filtered_content" # Changed WORK_DIR name
WORK_DIR_FULL_PATH="$SCRIPT_EXEC_DIR/$WORK_DIR_NAME"

rm -rf "$WORK_DIR_FULL_PATH"
mkdir -p "$WORK_DIR_FULL_PATH"
cd "$WORK_DIR_FULL_PATH" || { echo "Failed to create or enter WORK_DIR: $WORK_DIR_FULL_PATH"; exit 1; }

echo "Working directory: $(pwd)"

# 2. Extract JAR contents
echo "Extracting $INPUT_JAR_FULL_PATH..."
if [ ! -f "$INPUT_JAR_FULL_PATH" ]; then
    echo "Input JAR file not found: $INPUT_JAR_FULL_PATH"
    cd "$SCRIPT_EXEC_DIR"
    exit 1
fi
if ! jar xf "$INPUT_JAR_FULL_PATH"; then
    echo "Failed to extract JAR."
    cd "$SCRIPT_EXEC_DIR"
    exit 1
fi

# 3. Decompile .class files (if DECOMPILE is true)
if [ "$DECOMPILE" = true ]; then
    echo "Attempting to decompile .class files..."
    if [ ! -f "$DECOMPILER_FULL_PATH" ]; then
        echo "Decompiler JAR not found: $DECOMPILER_FULL_PATH"
        echo "If you need to decompile, ensure '$DECOMPILER_JAR_NAME_CONFIG' is in '$SCRIPT_EXEC_DIR'."
        echo "Java .class files will remain binary if decompiler is not found/used."
    else
        echo "Decompiling with $DECOMPILER_FULL_PATH (using $INPUT_JAR_FULL_PATH as input). This might take a while..."
        if java -jar "$DECOMPILER_FULL_PATH" "$INPUT_JAR_FULL_PATH" --outputdir . --silent true --comments false; then
            echo "Decompilation attempt complete."
        else
            echo "Decompilation command failed or had issues. .class files might not be decompiled."
        fi
    fi
else
    echo "Skipping Java .class file decompilation (DECOMPILE set to false)."
fi

# 4. Generate XML file
echo "Generating XML file: $OUTPUT_XML_FULL_PATH..."

echo '<?xml version="1.0" encoding="UTF-8"?>' > "$OUTPUT_XML_FULL_PATH"
echo '<repository_content>' >> "$OUTPUT_XML_FULL_PATH"

echo '  <file_summary>' >> "$OUTPUT_XML_FULL_PATH"
echo "    <purpose>Packed representation of textual file contents from $INPUT_JAR_NAME.</purpose>" >> "$OUTPUT_XML_FULL_PATH"
echo '    <file_format>Repomix-like XML format, including all extracted files (binary content omitted).</file_format>' >> "$OUTPUT_XML_FULL_PATH"
echo '    <usage_guidelines>' >> "$OUTPUT_XML_FULL_PATH"
echo '      - This file is intended for consumption by LLMs for knowledge base creation.' >> "$OUTPUT_XML_FULL_PATH"
echo '      - File paths are relative to the root of the extracted JAR content.' >> "$OUTPUT_XML_FULL_PATH"
echo '    </usage_guidelines>' >> "$OUTPUT_XML_FULL_PATH"
echo '    <notes>' >> "$OUTPUT_XML_FULL_PATH"
if [ "$DECOMPILE" = true ]; then
echo '      - Java .class files were attempted to be decompiled to .java source code.' >> "$OUTPUT_XML_FULL_PATH"
else
echo '      - Java .class file decompilation was skipped.' >> "$OUTPUT_XML_FULL_PATH"
fi
echo '      - All files found within the JAR structure are listed in directory_structure.' >> "$OUTPUT_XML_FULL_PATH"
echo '      - Content for textual files is embedded within CDATA sections.' >> "$OUTPUT_XML_FULL_PATH"
echo '      - Content for binary files is OMITTED; only metadata (path, type, size) is provided.' >> "$OUTPUT_XML_FULL_PATH"
echo '      - The "type" attribute ("textual" or "binary") and "mime_type" provide hints about file nature.' >> "$OUTPUT_XML_FULL_PATH"
echo '    </notes>' >> "$OUTPUT_XML_FULL_PATH"
echo '  </file_summary>' >> "$OUTPUT_XML_FULL_PATH"

echo '  <directory_structure>' >> "$OUTPUT_XML_FULL_PATH"
echo '    <![CDATA[' >> "$OUTPUT_XML_FULL_PATH"
(find . -print | sed 's|^\./||' | sort) >> "$OUTPUT_XML_FULL_PATH"
echo '    ]]>' >> "$OUTPUT_XML_FULL_PATH"
echo '  </directory_structure>' >> "$OUTPUT_XML_FULL_PATH"

echo '  <files>' >> "$OUTPUT_XML_FULL_PATH"

find . -type f -print0 | while IFS= read -r -d $'\0' FILE_PATH_IN_WORK_DIR; do
  RELATIVE_PATH_TO_JAR_ROOT=$(echo "$FILE_PATH_IN_WORK_DIR" | sed 's|^\./||')

  MIME_TYPE="unknown/unknown"
  if command -v file &> /dev/null; then
      MIME_TYPE_DETECTED=$(file -b --mime-type "$FILE_PATH_IN_WORK_DIR")
      if [ -n "$MIME_TYPE_DETECTED" ]; then MIME_TYPE="$MIME_TYPE_DETECTED"; fi
  fi
  
  FILE_SIZE_BYTES="0"
  if command -v wc &> /dev/null; then
      FILE_SIZE_DETECTED=$(wc -c < "$FILE_PATH_IN_WORK_DIR" | awk '{print $1}')
      if [ -n "$FILE_SIZE_DETECTED" ]; then FILE_SIZE_BYTES="$FILE_SIZE_DETECTED"; fi
  fi

  echo "    " >> "$OUTPUT_XML_FULL_PATH"

  IS_TEXT=false
  # Text file detection logic (same as before)
  if [[ "$MIME_TYPE" == text/* ]]; then
      IS_TEXT=true
  elif [[ "$MIME_TYPE" == application/xml || \
          "$MIME_TYPE" == application/json || \
          "$MIME_TYPE" == application/javascript || \
          "$MIME_TYPE" == application/x-sh || \
          "$MIME_TYPE" == application/x-httpd-php || \
          "$MIME_TYPE" == application/rtf || \
          "$MIME_TYPE" == application/csv || \
          "$MIME_TYPE" == "inode/x-empty" ]]; then
      IS_TEXT=true
  elif [[ "$MIME_TYPE" == application/octet-stream || "$MIME_TYPE" == unknown/* || "$MIME_TYPE" == application/zip && "$RELATIVE_PATH_TO_JAR_ROOT" == *.jar ]]; then
      LOWER_RELATIVE_PATH=$(echo "$RELATIVE_PATH_TO_JAR_ROOT" | tr '[:upper:]' '[:lower:]')
      TEXT_EXTENSIONS_REGEX="\.(java|scala|kt|groovy|properties|manifest|mf|md|txt|html|css|js|ts|yaml|yml|xml|sql|py|rb|php|pl|config|cfg|ini|sh|bat|ps1|gitignore|gitattributes|editorconfig|svg|log|csv|tsv|json|text|rst|adoc|asciidoc|conf|ds_store|pem|crt|key|csr|cnf|inf|reg|vbs|wsf|scpt|applescript|bash|zsh|fish|c|cpp|h|hpp|cs|fs|go|rs|dart|swift|s|asm|vb|pas|f|f90|f77|ada|lua|tcl|r|m|erl|hrl|ex|exs|eex|leex|vue|svelte|jsx|tsx|graphql|gql|tf|tfvars|hcl|toml|dockerfile|nginxconf|apacheconf|env|example|sum|mod|work|sbt|gradle|cql|plantuml|puml|dot|lock|babelrc|eslintrc|prettierrc|stylelintrc|nvmrc|bowerrc|yarnrc|npmrc|jshintrc|jsbeautifyrc|dockerignore|gitmodules|gitkeep|csproj|vbproj|fsproj|sln|vcproj|filters|xproj|user|suo|xcconfig|xcscheme|storyboard|xib|entitlements|plist|strings|xcworkspacedata|pch|podspec|gemfile|rakefile|buildkite|circleci|travis|gitlab-ci|jenkinsfile|vagrantfile|tfstate|tfplan|helmignore|helmchart|values\.yaml|requirements\.txt|pipfile|poetry\.lock|pyproject\.toml|package\.json|yarn\.lock|pnpm-lock\.yaml)$"
      if [[ "$LOWER_RELATIVE_PATH" =~ $TEXT_EXTENSIONS_REGEX ]]; then
          IS_TEXT=true
          if [[ "$MIME_TYPE" == application/octet-stream || "$MIME_TYPE" == unknown/* ]]; then
             MIME_TYPE="text/plain (inferred_by_extension)"
          fi
      fi
  fi
  
  if [ "$IS_TEXT" = true ]; then
      echo "    <file path=\"$RELATIVE_PATH_TO_JAR_ROOT\" type=\"textual\" mime_type=\"$MIME_TYPE\" size_bytes=\"$FILE_SIZE_BYTES\">" >> "$OUTPUT_XML_FULL_PATH"
      echo "<![CDATA[" >> "$OUTPUT_XML_FULL_PATH"
      cat "$FILE_PATH_IN_WORK_DIR" >> "$OUTPUT_XML_FULL_PATH"
      if [ "$(tail -c1 "$FILE_PATH_IN_WORK_DIR" | wc -l)" -eq 0 ]; then echo >> "$OUTPUT_XML_FULL_PATH"; fi
      echo "]]>" >> "$OUTPUT_XML_FULL_PATH"
      echo "    </file>" >> "$OUTPUT_XML_FULL_PATH"
  else
      # For binary files, content is OMITTED
      echo "    <file path=\"$RELATIVE_PATH_TO_JAR_ROOT\" type=\"binary\" mime_type=\"$MIME_TYPE\" size_bytes=\"$FILE_SIZE_BYTES\" content_omitted=\"true\"/>" >> "$OUTPUT_XML_FULL_PATH"
  fi
done

echo '  </files>' >> "$OUTPUT_XML_FULL_PATH"

echo '</repository_content>' >> "$OUTPUT_XML_FULL_PATH"

# 6. Clean up and exit
cd "$SCRIPT_EXEC_DIR"
# To retain the extracted/decompiled files for inspection, comment out the next line:
# rm -rf "$WORK_DIR_FULL_PATH"

echo "Conversion complete. Output file: $OUTPUT_XML_FULL_PATH"
if [ -d "$WORK_DIR_FULL_PATH" ]; then
    echo "Temporary work directory '$WORK_DIR_FULL_PATH' was retained for inspection."
fi
