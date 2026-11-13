package com.grafkom.visualrealism;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMapFBO {
    private final int fbo, depthTexture, width, height;
    public ShadowMapFBO(int width,int height) {
        this.width=width;this.height=height;
        fbo=glGenFramebuffers(); depthTexture=glGenTextures();
        glBindTexture(GL_TEXTURE_2D,depthTexture);
        glTexImage2D(GL_TEXTURE_2D,0,GL_DEPTH_COMPONENT32F,width,height,0,GL_DEPTH_COMPONENT,GL_FLOAT,0L);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D,GL_TEXTURE_BORDER_COLOR,new float[]{1,1,1,1});
        glBindFramebuffer(GL_FRAMEBUFFER,fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,depthTexture,0);
        glDrawBuffer(GL_NONE);glReadBuffer(GL_NONE);
        if(glCheckFramebufferStatus(GL_FRAMEBUFFER)!=GL_FRAMEBUFFER_COMPLETE)
            throw new IllegalStateException("Shadow framebuffer tidak lengkap");
        glBindFramebuffer(GL_FRAMEBUFFER,0);
    }
    public void bindForWriting() { glViewport(0,0,width,height);glBindFramebuffer(GL_FRAMEBUFFER,fbo); }
    public void unbind(int screenW,int screenH) { glBindFramebuffer(GL_FRAMEBUFFER,0);glViewport(0,0,screenW,screenH); }
    public int getDepthTexture() { return depthTexture; }
    public void cleanup() { glDeleteFramebuffers(fbo);glDeleteTextures(depthTexture); }
}
