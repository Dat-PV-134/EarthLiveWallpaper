#version 320 es

precision mediump float;

in vec4 ourColor;
out vec4 fragColor;

void main() {
    fragColor = ourColor;
}