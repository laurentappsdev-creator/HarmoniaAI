#!/usr/bin/env sh
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
if [ -x "$HOME/.gradle/bin/gradle" ]; then
  exec "$HOME/.gradle/bin/gradle" "$@"
fi
echo "Gradle executable not found." >&2
exit 1
