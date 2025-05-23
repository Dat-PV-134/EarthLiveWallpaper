#version 320 es
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D earthTexture;
uniform vec3 lightColor;
uniform vec3 lightPos;
uniform vec3 viewPos;

in vec3 FragPos;
in vec3 Normal;

void main() {
    // ambient
    float ambientStrength = 0.6;
    vec3 ambient = ambientStrength * lightColor;

    // diffuse
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = diff * lightColor;

    // specular
    float specularStrength = 0.8;
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 2.0);
    vec3 specular = specularStrength * spec * lightColor;

    // texture color
    vec4 texColor = texture(earthTexture, vTexCoord);
    vec3 finalColor = texColor.rgb * (ambient + diffuse + specular);

    // final color
    fragColor = vec4(finalColor, texColor.a);
}