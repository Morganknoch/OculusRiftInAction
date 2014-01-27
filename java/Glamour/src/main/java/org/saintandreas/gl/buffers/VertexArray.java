package org.saintandreas.gl.buffers;

import static org.lwjgl.opengl.GL30.*;

public class VertexArray {
  int vao;

  public VertexArray() {
    vao = glGenVertexArrays();
  }

  public void bind() {
    glBindVertexArray(vao);
  }

  public static void unbind() {
    glBindVertexArray(0);
  }
}
