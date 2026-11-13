package com.grafkom.visualrealism;

import org.lwjgl.system.MemoryStack;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture2D {
    private final int id;
    public Texture2D(String resource) {
        id=glGenTextures(); glBindTexture(GL_TEXTURE_2D,id);
        stbi_set_flip_vertically_on_load(true);
        try (MemoryStack stack=MemoryStack.stackPush()) {
            IntBuffer w=stack.mallocInt(1), h=stack.mallocInt(1), c=stack.mallocInt(1);
            ByteBuffer pixels;
            try (var in=getClass().getResourceAsStream(resource)) {
                if(in==null) throw new IllegalArgumentException("Tekstur tidak ditemukan: "+resource);
                byte[] bytes=in.readAllBytes();
                ByteBuffer encoded=stack.malloc(bytes.length); encoded.put(bytes).flip();
                pixels=stbi_load_from_memory(encoded,w,h,c,4);
            } catch(Exception e) { throw new RuntimeException(e); }
            if(pixels==null) throw new RuntimeException("STBImage gagal: "+stbi_failure_reason());
            glTexImage2D(GL_TEXTURE_2D,0,GL_RGBA,w.get(0),h.get(0),0,GL_RGBA,GL_UNSIGNED_BYTE,pixels);
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(pixels);
        }
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);
        setFiltering(true);
    }
    public void setFiltering(boolean linear) {
        glBindTexture(GL_TEXTURE_2D,id);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,linear?GL_LINEAR_MIPMAP_LINEAR:GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,linear?GL_LINEAR:GL_NEAREST);
    }
    public void bind() { glBindTexture(GL_TEXTURE_2D,id); }
    public void cleanup() { glDeleteTextures(id); }
}
