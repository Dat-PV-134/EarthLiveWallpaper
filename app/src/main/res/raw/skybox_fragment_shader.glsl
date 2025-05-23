#version 320 es
precision mediump float;

in vec3 TexCoords;

uniform samplerCube skybox;

out vec4 FragColor;

void main() {
    FragColor = texture(skybox, TexCoords);
}
