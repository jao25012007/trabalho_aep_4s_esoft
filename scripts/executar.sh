#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
command -v javac >/dev/null || { echo 'Instale um JDK 17 ou superior e configure o PATH.'; exit 1; }
mkdir -p build/classes
mapfile -d '' fontes < <(find src/main/java -name '*.java' -print0)
javac --release 17 -encoding UTF-8 -d build/classes "${fontes[@]}"
java -Dfile.encoding=UTF-8 -cp 'build/classes:lib/*' br.edu.educaplan.Main "$@"
