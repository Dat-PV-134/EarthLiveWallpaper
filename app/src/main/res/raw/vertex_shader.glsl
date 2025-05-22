#version 320 es

uniform mat4 projectionMatrix;
layout(location = 0) uniform mat4 modelMatrix;
layout(location = 1) uniform mat4 viewMatrix;

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec4 aColor;

out vec4 ourColor;

void main() {
    gl_Position =  projectionMatrix * viewMatrix * modelMatrix * vec4(aPos.x, aPos.y, aPos.z, 1.0);
    ourColor = aColor;
}