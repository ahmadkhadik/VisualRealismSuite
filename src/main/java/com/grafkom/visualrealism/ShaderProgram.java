package com.grafkom.visualrealism;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int id;
    public ShaderProgram(String vertexPath, String fragmentPath) {
        int vs = compile(GL_VERTEX_SHADER, read(vertexPath));
        int fs = compile(GL_FRAGMENT_SHADER, read(fragmentPath));
        id = glCreateProgram();
        glAttachShader(id, vs); glAttachShader(id, fs); glLinkProgram(id);
        if (glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader link error: " + glGetProgramInfoLog(id));
        glDeleteShader(vs); glDeleteShader(fs);
    }
    private int compile(int type, String source) {
        int shader = glCreateShader(type); glShaderSource(shader, source); glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(shader));
        return shader;
    }
    private String read(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("Resource tidak ditemukan: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    public void use() { glUseProgram(id); }
    private int loc(String name) { return glGetUniformLocation(id, name); }
    public void setInt(String name, int value) { glUniform1i(loc(name), value); }
    public void setVec3(String name, Vector3f v) { glUniform3f(loc(name), v.x, v.y, v.z); }
    public void setMat4(String name, Matrix4f m) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16); m.get(fb); glUniformMatrix4fv(loc(name), false, fb);
        }
    }
    public void cleanup() { glDeleteProgram(id); }
}
