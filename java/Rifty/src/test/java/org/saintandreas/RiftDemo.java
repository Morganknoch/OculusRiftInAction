package org.saintandreas;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.*;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.saintandreas.Hmd.Eye;
import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.shaders.Program;

public class RiftDemo extends LwjglApp {
  IndexedGeometry cubeGeometry;
  IndexedGeometry eyeMeshes[] = new IndexedGeometry[2];
  Program renderProgram;
  Program distortProgram;
  FrameBuffer frameBuffer;
  
  protected void setupDisplay() throws LWJGLException {
    super.setupDisplay();
    Display.setLocation(100, -700 + 100);
  }


  @Override
  protected void initGl() {
    super.initGl();
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glEnable(GL_PRIMITIVE_RESTART);
    glPrimitiveRestartIndex(Short.MAX_VALUE);
    MatrixStack.MODELVIEW.lookat(new Vector3f(-2, 1, 3), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

    frameBuffer = new FrameBuffer(640, 800);
    frameBuffer.getTexture().bind();
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    frameBuffer.getTexture().unbind();
    
    eyeMeshes[0] = new RiftDK1().getDistortionMesh(Eye.LEFT, 128);
    eyeMeshes[1] = new RiftDK1().getDistortionMesh(Eye.RIGHT, 128);

    distortProgram = new Program("shaders/SimpleTextured.vs", "shaders/Texture.fs");
    distortProgram.link();
    renderProgram = new Program("shaders/SimpleColored.vs", "shaders/Simple.fs");
    renderProgram.link();
    cubeGeometry = OpenGL.makeColorCube();
  }

  @Override
  protected void onResize(int width, int height) {
    super.onResize(width, height);
    MatrixStack.PROJECTION.perspective(80f, aspect / 2.0f, 0.01f, 1000.0f);
  }

  @Override 
  protected void update() {
    MatrixStack.MODELVIEW.rotate((float)(Math.PI / 42), new Vector3f(0, 1, 0));
  }

  @Override
  public void drawFrame() {
    OpenGL.checkError();
    glViewport(0, 0, width, height);
    glClearColor(.1f, .1f, .1f, 0.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    for (int i = 0; i < 2; ++i) {
      frameBuffer.activate();
      float offset = RiftDK1.LensOffset;
      float ipd = 0.06400f;
      float mvo = ipd / 2.0f;
      if (i == 1) {
        offset *= -1f;
        mvo *= -1f;
      }
      new Matrix4f().translate(new Vector2f(offset, 0));
      MatrixStack.PROJECTION.push().preTranslate(offset);
      MatrixStack.MODELVIEW.push().preTranslate(mvo);
      renderScene();
      MatrixStack.MODELVIEW.pop();;
      MatrixStack.PROJECTION.pop();

      frameBuffer.deactivate();

      int x = i == 0 ? 0 : (width / 2);
      glViewport(x, 0, width / 2, height);
      distortProgram.use();
      frameBuffer.getTexture().bind();
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
      eyeMeshes[i].bindVertexArray();
      eyeMeshes[i].draw();
      VertexArray.unbind();
      Program.clear();
    }
  }

  public void renderScene() {
    glViewport(1, 1, width / 2 - 1, height - 1);
    glClearColor(.3f, .3f, .3f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    renderProgram.use();
    MatrixStack.bind(renderProgram);
    cubeGeometry.bindVertexArray();
    cubeGeometry.draw();
    VertexArray.unbind();
    Program.clear();
  }

  public static void main(String[] args) {
    new RiftDemo().run();
  }
}
