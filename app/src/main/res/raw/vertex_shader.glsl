#version 320 es

uniform mat4 projectionMatrix;
layout(location = 0) uniform mat4 modelMatrix;
layout(location = 1) uniform mat4 viewMatrix;

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec2 aTexCoord;
layout(location = 2) in vec3 aNormal;

out vec2 vTexCoord;
out vec3 FragPos;
out vec3 Normal;

void main() {
    // Tính vị trí vertex trong world space
    vec4 worldPos = modelMatrix * vec4(aPos, 1.0);
    FragPos = worldPos.xyz;

    // Tính normal trong world space
    Normal = mat3(transpose(inverse(modelMatrix))) * aNormal;

    // Gửi texCoord
    vTexCoord = aTexCoord;

    // Tính vị trí clip space
    gl_Position = projectionMatrix * viewMatrix * worldPos;
}
