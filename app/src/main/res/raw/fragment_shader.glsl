#version 320 es
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D earthTexture;

void main() {
    fragColor = texture(earthTexture, vTexCoord);
}
