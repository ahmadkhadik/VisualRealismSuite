package com.grafkom.visualrealism;

import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.nio.FloatBuffer;
import org.lwjgl.system.MemoryUtil;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private final int vao, vbo, vertexCount;
    private Mesh(float[] data) {
        vertexCount = data.length / 11;
        vao = glGenVertexArrays(); vbo = glGenBuffers();
        glBindVertexArray(vao); glBindBuffer(GL_ARRAY_BUFFER, vbo);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip(); glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW); MemoryUtil.memFree(buffer);
        int stride = 11 * Float.BYTES;
        glVertexAttribPointer(0,3,GL_FLOAT,false,stride,0L); glEnableVertexAttribArray(0);
        glVertexAttribPointer(1,3,GL_FLOAT,false,stride,3L*Float.BYTES); glEnableVertexAttribArray(1);
        glVertexAttribPointer(2,3,GL_FLOAT,false,stride,6L*Float.BYTES); glEnableVertexAttribArray(2);
        glVertexAttribPointer(3,2,GL_FLOAT,false,stride,9L*Float.BYTES); glEnableVertexAttribArray(3);
        glBindVertexArray(0);
    }
    public static Mesh createSphere(int sectors, int stacks) {
        List<Float> out = new ArrayList<>();
        for (int i=0;i<stacks;i++) {
            float v0=(float)i/stacks, v1=(float)(i+1)/stacks;
            float p0=(float)(Math.PI*v0), p1=(float)(Math.PI*v1);
            for (int j=0;j<sectors;j++) {
                float u0=(float)j/sectors, u1=(float)(j+1)/sectors;
                float t0=(float)(2*Math.PI*u0), t1=(float)(2*Math.PI*u1);
                Vector3f a=point(p0,t0), b=point(p1,t0), c=point(p1,t1), d=point(p0,t1);
                // Urutan vertex dibuat counter-clockwise dari sisi luar bola agar Back-Face Culling benar.
                addTriangle(out,a,c,b,u0,v0,u1,v1,u0,v1);
                addTriangle(out,a,d,c,u0,v0,u1,v0,u1,v1);
            }
        }
        float[] data = new float[out.size()]; for(int i=0;i<data.length;i++) data[i]=out.get(i);
        return new Mesh(data);
    }
    private static Vector3f point(float p,float t) {
        return new Vector3f((float)(Math.sin(p)*Math.cos(t)), (float)Math.cos(p),
                (float)(Math.sin(p)*Math.sin(t)));
    }
    private static void addTriangle(List<Float> o, Vector3f a,Vector3f b,Vector3f c,
                                    float au,float av,float bu,float bv,float cu,float cv) {
        Vector3f face = new Vector3f(b).sub(a).cross(new Vector3f(c).sub(a)).normalize();
        addVertex(o,a,au,av,face); addVertex(o,b,bu,bv,face); addVertex(o,c,cu,cv,face);
    }
    private static void addVertex(List<Float> o,Vector3f p,float u,float v,Vector3f face) {
        o.add(p.x);o.add(p.y);o.add(p.z);
        o.add(p.x);o.add(p.y);o.add(p.z);
        o.add(face.x);o.add(face.y);o.add(face.z);
        o.add(u);o.add(v);
    }
    public static Mesh createPlane() {
        List<Float> o=new ArrayList<>();
        Vector3f n=new Vector3f(0,1,0);
        // Winding order counter-clockwise when viewed from above.
        addPlaneVertex(o,-1,0,-1,0,0,n); addPlaneVertex(o,-1,0,1,0,1,n); addPlaneVertex(o,1,0,1,1,1,n);
        addPlaneVertex(o,-1,0,-1,0,0,n); addPlaneVertex(o,1,0,1,1,1,n); addPlaneVertex(o,1,0,-1,1,0,n);
        float[] d=new float[o.size()];for(int i=0;i<d.length;i++)d[i]=o.get(i);return new Mesh(d);
    }
    private static void addPlaneVertex(List<Float> o,float x,float y,float z,float u,float v,Vector3f n) {
        o.add(x);o.add(y);o.add(z);o.add(n.x);o.add(n.y);o.add(n.z);o.add(n.x);o.add(n.y);o.add(n.z);o.add(u);o.add(v);
    }
    public void render() { glBindVertexArray(vao); glDrawArrays(GL_TRIANGLES,0,vertexCount); glBindVertexArray(0); }
    public void cleanup() { glDeleteBuffers(vbo); glDeleteVertexArrays(vao); }
}
