#!/bin/sh
mkdir Souffleur.iconset
sips -z 16 16     1024_transparent.png --out Souffleur.iconset/icon_16x16.png
sips -z 32 32     1024_transparent.png --out Souffleur.iconset/icon_16x16@2x.png
sips -z 32 32     1024_transparent.png --out Souffleur.iconset/icon_32x32.png
sips -z 64 64     1024_transparent.png --out Souffleur.iconset/icon_32x32@2x.png
sips -z 128 128   1024_transparent.png --out Souffleur.iconset/icon_128x128.png
sips -z 256 256   1024_transparent.png --out Souffleur.iconset/icon_128x128@2x.png
sips -z 256 256   1024_transparent.png --out Souffleur.iconset/icon_256x256.png
sips -z 512 512   1024_transparent.png --out Souffleur.iconset/icon_256x256@2x.png
sips -z 512 512   1024_transparent.png --out Souffleur.iconset/icon_512x512.png
cp 1024_transparent.png Souffleur.iconset/icon_512x512@2x.png
iconutil -c icns Souffleur.iconset
rm -R Souffleur.iconset
mv Souffleur.icns ../server/artwork
