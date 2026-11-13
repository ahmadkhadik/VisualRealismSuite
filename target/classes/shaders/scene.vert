#version 330 core
layout(location=0) in vec3 aPos;
layout(location=1) in vec3 aNormal;
layout(location=2) in vec3 aFaceNormal;
layout(location=3) in vec2 aTexCoords;
out vec3 FragPos;
out vec3 Normal;
out vec3 FaceNormal;
out vec2 TexCoords;
out vec4 FragPosLightSpace;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform mat4 lightSpaceMatrix;
void main() {
    vec4 worldPos = model * vec4(aPos,1.0);
    FragPos = worldPos.xyz;
    mat3 normalMatrix = mat3(transpose(inverse(model)));
    Normal = normalize(normalMatrix * aNormal);
    FaceNormal = normalize(normalMatrix * aFaceNormal);
    TexCoords = aTexCoords;
    FragPosLightSpace = lightSpaceMatrix * worldPos;
    gl_Position = projection * view * worldPos;
}
