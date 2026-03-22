#!/usr/bin/env bash
# =============================================================================
# Vibely POS — Icon PNG Generation Script
# =============================================================================
# Generates raster PNG icons from ic_app_logo.svg for Android mipmap buckets
# and PWA manifest sizes.
#
# Dependencies (install one of these):
#   macOS:   brew install librsvg        → provides rsvg-convert
#   macOS:   brew install inkscape       → provides inkscape CLI
#   Linux:   apt install librsvg2-bin    → provides rsvg-convert
#   Linux:   apt install inkscape        → provides inkscape CLI
#
# Usage:
#   chmod +x scripts/generate_icons.sh
#   ./scripts/generate_icons.sh
#
# Run from the repository root. All output paths are relative to project root.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

SVG_SOURCE="$PROJECT_ROOT/composeApp/src/commonMain/composeResources/drawable/ic_app_logo.svg"
ANDROID_RES="$PROJECT_ROOT/composeApp/src/androidMain/res"
WEB_RES="$PROJECT_ROOT/composeApp/src/wasmJsMain/resources"

# ---------------------------------------------------------------------------
# Detect conversion tool
# ---------------------------------------------------------------------------
if command -v rsvg-convert &>/dev/null; then
    TOOL="rsvg-convert"
elif command -v inkscape &>/dev/null; then
    TOOL="inkscape"
else
    echo "ERROR: Neither rsvg-convert nor inkscape found."
    echo ""
    echo "Install one of:"
    echo "  macOS:  brew install librsvg"
    echo "  macOS:  brew install inkscape"
    echo "  Linux:  apt install librsvg2-bin"
    echo "  Linux:  apt install inkscape"
    exit 1
fi

echo "Using conversion tool: $TOOL"
echo "Source SVG: $SVG_SOURCE"
echo ""

# ---------------------------------------------------------------------------
# Conversion helper
# ---------------------------------------------------------------------------
convert_svg() {
    local size="$1"
    local output="$2"

    mkdir -p "$(dirname "$output")"

    if [ "$TOOL" = "rsvg-convert" ]; then
        rsvg-convert -w "$size" -h "$size" -o "$output" "$SVG_SOURCE"
    elif [ "$TOOL" = "inkscape" ]; then
        inkscape \
            --export-type=png \
            --export-filename="$output" \
            --export-width="$size" \
            --export-height="$size" \
            "$SVG_SOURCE" \
            2>/dev/null
    fi

    echo "  Generated: $output (${size}×${size})"
}

# ---------------------------------------------------------------------------
# Android mipmap launcher PNGs
# ic_launcher.png in each density bucket
# ---------------------------------------------------------------------------
echo "--- Android Launcher Icons ---"

convert_svg 48  "$ANDROID_RES/mipmap-mdpi/ic_launcher.png"
convert_svg 72  "$ANDROID_RES/mipmap-hdpi/ic_launcher.png"
convert_svg 96  "$ANDROID_RES/mipmap-xhdpi/ic_launcher.png"
convert_svg 144 "$ANDROID_RES/mipmap-xxhdpi/ic_launcher.png"
convert_svg 192 "$ANDROID_RES/mipmap-xxxhdpi/ic_launcher.png"

# Round variants (same source — adaptive icon XML handles actual round mask on API 26+)
convert_svg 48  "$ANDROID_RES/mipmap-mdpi/ic_launcher_round.png"
convert_svg 72  "$ANDROID_RES/mipmap-hdpi/ic_launcher_round.png"
convert_svg 96  "$ANDROID_RES/mipmap-xhdpi/ic_launcher_round.png"
convert_svg 144 "$ANDROID_RES/mipmap-xxhdpi/ic_launcher_round.png"
convert_svg 192 "$ANDROID_RES/mipmap-xxxhdpi/ic_launcher_round.png"

# ---------------------------------------------------------------------------
# PWA manifest icons
# ---------------------------------------------------------------------------
echo ""
echo "--- PWA / Web Icons ---"

convert_svg 192 "$WEB_RES/icon-192.png"
convert_svg 512 "$WEB_RES/icon-512.png"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
echo "Done. All icons generated successfully."
echo ""
echo "Next steps:"
echo "  1. Verify PNGs look correct by opening them in Preview / an image viewer."
echo "  2. Add icon-192.png and icon-512.png to your web app manifest if you have one."
echo "  3. The Android adaptive icon XML files in mipmap-anydpi-v26/ will be used"
echo "     automatically on Android 8.0+ devices. PNG fallbacks cover older devices."
