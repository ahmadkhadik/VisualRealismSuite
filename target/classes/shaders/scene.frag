#version 330 core
out vec4 FragColor;
in vec3 FragPos;
in vec3 Normal;
in vec3 FaceNormal;
in vec2 TexCoords;
in vec4 FragPosLightSpace;
uniform vec3 lightPos;
uniform vec3 viewPos;
uniform vec3 lightColor;
uniform vec3 objectColor;
uniform sampler2D diffuseTexture;
uniform sampler2D shadowMap;
uniform int useBlinnPhong;
uniform int useFlat;
uniform int useTexture;
uniform int useShadow;

float calculateShadow(vec3 norm, vec3 lightDir) {
    vec3 proj = FragPosLightSpace.xyz / FragPosLightSpace.w;
    proj = proj * 0.5 + 0.5;
    if(proj.z > 1.0 || proj.x < 0.0 || proj.x > 1.0 || proj.y < 0.0 || proj.y > 1.0) return 0.0;
    float closestDepth = texture(shadowMap, proj.xy).r;
    float currentDepth = proj.z;
    float bias = max(0.005 * (1.0 - dot(norm, lightDir)), 0.0008);
    float shadow = 0.0;
    vec2 texel = 1.0 / textureSize(shadowMap,0);
    for(int x=-1;x<=1;x++) for(int y=-1;y<=1;y++) {
        float pcfDepth = texture(shadowMap,proj.xy+vec2(x,y)*texel).r;
        shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
    }
    shadow /= 9.0;
    return shadow;
}
void main() {
    vec3 norm = normalize(useFlat == 1 ? FaceNormal : Normal);
    vec3 lightDir = normalize(lightPos - FragPos);
    vec3 viewDir = normalize(viewPos - FragPos);
    float ambientStrength = 0.22;
    vec3 ambient = ambientStrength * lightColor;
    float diff = max(dot(norm,lightDir),0.0);
    vec3 diffuse = diff * lightColor;
    float spec = 0.0;
    if(useBlinnPhong == 1) {
        vec3 halfwayDir = normalize(lightDir + viewDir);
        spec = pow(max(dot(norm,halfwayDir),0.0),48.0);
    } else {
        vec3 reflectDir = reflect(-lightDir,norm);
        spec = pow(max(dot(viewDir,reflectDir),0.0),32.0);
    }
    vec3 specular = 0.65 * spec * lightColor;
    vec3 base = useTexture == 1 ? texture(diffuseTexture,TexCoords).rgb : objectColor;
    float shadow = useShadow == 1 ? calculateShadow(norm,lightDir) : 0.0;
    vec3 lighting = ambient + (1.0-shadow)*(diffuse+specular);
    FragColor = vec4(lighting * base,1.0);
}
