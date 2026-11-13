
package com.grafkom.visualrealism;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

public class Main {

    private long window;

    private int width = 1100;
    private int height = 760;

    private ShaderProgram sceneShader, depthShader;
    private Mesh sphere, floor;
    private Texture2D texture;
    private ShadowMapFBO shadow;

    private final Vector3f cameraPos =
            new Vector3f(0, 1.2f, 5.2f);

    private float rotation;
    private float rotationX;

    private boolean mouseDragging = false;

    private double lastMouseX;
    private double lastMouseY;

    private boolean blinn = true;
    private boolean flat = false;
    private boolean depthTest = true;
    private boolean culling = true;

    private boolean useTexture = true;
    private boolean useShadow = true;
    private boolean linearFilter = true;

    private boolean[] keys =
            new boolean[GLFW_KEY_LAST + 1];

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException(
                    "GLFW initialization failed"
            );
        }

        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MAJOR, 3
        );

        glfwWindowHint(
                GLFW_CONTEXT_VERSION_MINOR, 3
        );

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );

        glfwWindowHint(
                GLFW_OPENGL_FORWARD_COMPAT,
                GLFW_TRUE
        );

        window = glfwCreateWindow(
                width,
                height,
                "3D Visual Realism Suite",
                0,
                0
        );

        if (window == 0) {
            throw new RuntimeException(
                    "Gagal membuat window OpenGL 3.3"
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        // FRAMEBUFFER RESIZE
        glfwSetFramebufferSizeCallback(
                window,
                (w, newW, newH) -> {

                    width = Math.max(newW, 1);
                    height = Math.max(newH, 1);

                    glViewport(0, 0, width, height);
                }
        );

        // KEYBOARD INPUT
        glfwSetKeyCallback(
                window,
                (w, key, scancode, action, mods) -> {

                    if (key == GLFW_KEY_ESCAPE
                            && action == GLFW_PRESS) {

                        glfwSetWindowShouldClose(
                                w, true
                        );
                    }

                    if (key >= 0 && key < keys.length) {

                        if (action == GLFW_PRESS) {

                            if (!keys[key]) {
                                handleKey(key);
                            }

                            keys[key] = true;

                        } else if (action == GLFW_RELEASE) {

                            keys[key] = false;
                        }
                    }
                }
        );

        // MOUSE BUTTON INPUT
        glfwSetMouseButtonCallback(
                window,
                (w, button, action, mods) -> {

                    if (button == GLFW_MOUSE_BUTTON_LEFT) {

                        if (action == GLFW_PRESS) {

                            mouseDragging = true;

                            double[] xpos = new double[1];
                            double[] ypos = new double[1];

                            glfwGetCursorPos(
                                    w, xpos, ypos
                            );

                            lastMouseX = xpos[0];
                            lastMouseY = ypos[0];

                        } else if (action == GLFW_RELEASE) {

                            mouseDragging = false;
                        }
                    }
                }
        );

        // MOUSE MOVEMENT INPUT
        glfwSetCursorPosCallback(
                window,
                (w, xpos, ypos) -> {

                    if (mouseDragging) {

                        double deltaX =
                                xpos - lastMouseX;

                        double deltaY =
                                ypos - lastMouseY;

                        rotation +=
                                (float) deltaX * 0.01f;

                        rotationX +=
                                (float) deltaY * 0.01f;

                        // Batasi rotasi vertikal
                        rotationX = Math.max(
                                -1.3f,
                                Math.min(1.3f, rotationX)
                        );
                    }

                    lastMouseX = xpos;
                    lastMouseY = ypos;
                }
        );

        // MOUSE SCROLL / ZOOM INPUT
        glfwSetScrollCallback(
                window,
                (w, xoffset, yoffset) -> {

                    cameraPos.z -=
                            (float) yoffset * 0.35f;

                    cameraPos.z = Math.max(
                            2.7f,
                            Math.min(9.0f, cameraPos.z)
                    );
                }
        );

        // OPENGL SETTINGS
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        glClearColor(
                0.055f, 0.075f, 0.11f, 1f
        );

        // LOAD SHADERS
        sceneShader = new ShaderProgram(
                "/shaders/scene.vert",
                "/shaders/scene.frag"
        );

        depthShader = new ShaderProgram(
                "/shaders/depth.vert",
                "/shaders/depth.frag"
        );

        // CREATE OBJECTS
        sphere = Mesh.createSphere(48, 32);
        floor = Mesh.createPlane();

        // LOAD TEXTURE
        texture = new Texture2D(
                "/textures/stone.png"
        );

        // SHADOW MAP
        shadow = new ShadowMapFBO(
                2048, 2048
        );

        printControls();
    }

    private void handleKey(int key) {

        switch (key) {

            case GLFW_KEY_B -> {
                blinn = !blinn;
                status(
                        "Shading: "
                        + (blinn
                        ? "Blinn-Phong"
                        : "Phong")
                );
            }

            case GLFW_KEY_F -> {
                flat = !flat;
                status(
                        "Shading geometry: "
                        + (flat ? "Flat" : "Smooth")
                );
            }

            case GLFW_KEY_Z -> {
                depthTest = !depthTest;
                status(
                        "Z-Buffer: "
                        + (depthTest ? "ON" : "OFF")
                );
            }

            case GLFW_KEY_C -> {
                culling = !culling;
                status(
                        "Back-Face Culling: "
                        + (culling ? "ON" : "OFF")
                );
            }

            case GLFW_KEY_T -> {
                useTexture = !useTexture;
                status(
                        "Texture: "
                        + (useTexture ? "ON" : "OFF")
                );
            }

            case GLFW_KEY_M -> {
                linearFilter = !linearFilter;

                texture.setFiltering(
                        linearFilter
                );

                status(
                        "Filter: "
                        + (linearFilter
                        ? "Linear + Mipmap"
                        : "Nearest")
                );
            }

            case GLFW_KEY_H -> {
                useShadow = !useShadow;
                status(
                        "Shadow Mapping: "
                        + (useShadow ? "ON" : "OFF")
                );
            }
        }
    }

    private void loop() {

        while (!glfwWindowShouldClose(window)) {

            update();

            render();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }
    }

    private void update() {

        if (keys[GLFW_KEY_A]) {
            rotation -= 0.015f;
        }

        if (keys[GLFW_KEY_D]) {
            rotation += 0.015f;
        }

        if (keys[GLFW_KEY_W]) {
            cameraPos.z -= 0.045f;
        }

        if (keys[GLFW_KEY_S]) {
            cameraPos.z += 0.045f;
        }

        cameraPos.z = Math.max(
                2.7f,
                Math.min(9f, cameraPos.z)
        );
    }

    private void render() {

        Matrix4f model = new Matrix4f()
                .translate(0, 0.85f, 0)
                .rotateX(rotationX)
                .rotateY(rotation)
                .scale(0.85f);

        Matrix4f floorModel = new Matrix4f()
                .translate(0, -0.04f, 0)
                .scale(5f, 1f, 5f);

        Vector3f lightPos = new Vector3f(
                -2.5f, 5.0f, 2.5f
        );

        Matrix4f lightView = new Matrix4f()
                .lookAt(
                        lightPos,
                        new Vector3f(0, 0, 0),
                        new Vector3f(0, 1, 0)
                );

        Matrix4f lightProjection = new Matrix4f()
                .ortho(
                        -7, 7,
                        -7, 7,
                        0.1f, 15f
                );

        Matrix4f lightSpace =
                new Matrix4f(lightProjection)
                        .mul(lightView);

        // SHADOW PASS
        if (useShadow) {

            shadow.bindForWriting();

            glClear(GL_DEPTH_BUFFER_BIT);

            depthShader.use();

            depthShader.setMat4(
                    "lightSpaceMatrix",
                    lightSpace
            );

            depthShader.setMat4(
                    "model",
                    model
            );

            sphere.render();

            depthShader.setMat4(
                    "model",
                    floorModel
            );

            floor.render();

            shadow.unbind(width, height);

        } else {

            glViewport(0, 0, width, height);
        }

        // MAIN RENDER PASS
        glClear(
                GL_COLOR_BUFFER_BIT
                | GL_DEPTH_BUFFER_BIT
        );

        if (depthTest) {

            glEnable(GL_DEPTH_TEST);
            glDepthFunc(GL_LESS);

        } else {

            glDisable(GL_DEPTH_TEST);
        }

        if (culling) {

            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            glFrontFace(GL_CCW);

        } else {

            glDisable(GL_CULL_FACE);
        }

        Matrix4f projection = new Matrix4f()
                .perspective(
                        (float) Math.toRadians(45),
                        (float) width / height,
                        0.1f,
                        100f
                );

        Matrix4f view = new Matrix4f()
                .lookAt(
                        cameraPos,
                        new Vector3f(0, 0.65f, 0),
                        new Vector3f(0, 1, 0)
                );

        sceneShader.use();

        sceneShader.setMat4("view", view);
        sceneShader.setMat4("projection", projection);

        sceneShader.setMat4(
                "lightSpaceMatrix",
                lightSpace
        );

        sceneShader.setVec3(
                "viewPos",
                cameraPos
        );

        sceneShader.setVec3(
                "lightPos",
                lightPos
        );

        sceneShader.setVec3(
                "lightColor",
                new Vector3f(1f, 0.96f, 0.88f)
        );

        sceneShader.setInt(
                "useBlinnPhong",
                blinn ? 1 : 0
        );

        sceneShader.setInt(
                "useFlat",
                flat ? 1 : 0
        );

        sceneShader.setInt(
                "useTexture",
                useTexture ? 1 : 0
        );

        sceneShader.setInt(
                "useShadow",
                useShadow ? 1 : 0
        );

        sceneShader.setInt(
                "diffuseTexture",
                0
        );

        sceneShader.setInt(
                "shadowMap",
                1
        );

        // TEXTURE
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        glActiveTexture(GL_TEXTURE1);

        glBindTexture(
                GL_TEXTURE_2D,
                shadow.getDepthTexture()
        );

        // RENDER SPHERE
        sceneShader.setMat4(
                "model",
                model
        );

        sceneShader.setVec3(
                "objectColor",
                new Vector3f(0.82f, 0.72f, 0.52f)
        );

        sphere.render();

        // RENDER FLOOR
        sceneShader.setMat4(
                "model",
                floorModel
        );

        sceneShader.setVec3(
                "objectColor",
                new Vector3f(0.42f, 0.48f, 0.55f)
        );

        sceneShader.setInt("useTexture", 0);
        sceneShader.setInt("useFlat", 0);

        floor.render();
    }

    private void cleanup() {

        sphere.cleanup();
        floor.cleanup();

        texture.cleanup();
        shadow.cleanup();

        sceneShader.cleanup();
        depthShader.cleanup();

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void status(String s) {

        System.out.println("[STATUS] " + s);

        glfwSetWindowTitle(
                window,
                "3D Visual Realism Suite | " + s
        );
    }

    private void printControls() {

        System.out.println("""
        ==========================================
           3D VISUAL REALISM SUITE - CONTROLS
        ==========================================

        MOUSE:
        Left Click + Drag : Rotate Object
        Scroll Up        : Zoom In
        Scroll Down      : Zoom Out

        KEYBOARD:
        B : Phong / Blinn-Phong
        F : Flat / Smooth
        Z : Z-Buffer ON / OFF
        C : Back-Face Culling ON / OFF
        T : Texture ON / OFF
        M : Nearest / Linear + Mipmap
        H : Shadow Mapping ON / OFF

        A/D : Rotate object
        W/S : Zoom

        ESC : Exit

        ==========================================
        """);
    }
}
