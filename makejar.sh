#!/bin/bash
cp dist/TILT.jar .
jar uf TILT.jar -C lib graphics
cp TILT.jar /Library/WebServer/Documents/
cp tilt.jnlp /Library/WebServer/Documents/
cp applet.html /Library/WebServer/Documents/
