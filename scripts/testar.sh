#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/test-classes
mapfile -d '' fontes < <(find src/main/java src/test/java -name '*.java' -print0)
javac --release 17 -encoding UTF-8 -d build/test-classes "${fontes[@]}"
java -Dfile.encoding=UTF-8 -cp 'build/test-classes:lib/*' br.edu.educaplan.TesteIntegracao
