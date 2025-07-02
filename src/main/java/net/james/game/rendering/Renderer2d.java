package net.james.game.rendering;

import net.james.game.core.Rectangle;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL40.*;

public class Renderer2d
{
    private static final int TEXTURE_STACK_SIZE = 16;
    private static final int MAX_SPRITES = 512;

    private record SbVertex(Vector4f pos, Vector4f color, Vector2f texCoord)
    {

    }

    private boolean renderBegun;
    private Vector2f cameraPos;

    private Texture nullTexture;
    private final Texture[] textureStack = new Texture[TEXTURE_STACK_SIZE];
    private final int[] fontTexture = new int[TEXTURE_STACK_SIZE];
    private int currentTexture;

    private Rectangle uv;
    private Vector4f color;

    private final SbVertex[] vertices = new SbVertex[MAX_SPRITES * 4];
    private final short[] indices = new short[MAX_SPRITES * 6];

    private short currentVertex;
    private int currentIndex;

    private int vao;
    private int vbo;
    private int ibo;

    private Shader shader;
    private Matrix4f projectionMatrix;

    public Renderer2d()
    {

    }

    public void setRenderColor(float r, float g, float b)
    {
        setRenderColor(r, g, b, 1);
    }

    public void setRenderColor(float r, float g, float b, float a)
    {
        color = new Vector4f(r, g, b, a);
    }

    public void setUVRect(float uvX, float uvY, float uvWidth, float uvHeight)
    {
        uv = new Rectangle(uvX, uvY, uvWidth, uvHeight);
    }

    public void setCameraPos(Vector2f pos)
    {
        cameraPos = pos;
    }

    public void begin()
    {
        renderBegun = true;
        currentTexture = 0;
        currentIndex = 0;
        currentVertex = 0;

        IntBuffer width = IntBuffer.allocate(1), height = IntBuffer.allocate(1);
        long context = GLFW.glfwGetCurrentContext();
        GLFW.glfwGetWindowSize(context, width, height);

        shader.bind();

        var projection = new Matrix4f().ortho(cameraPos.x, cameraPos.x + width.get(), cameraPos.y, cameraPos.y + height.get(), 1, -101);
        shader.set("projectionMatrix", projection);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        setRenderColor(1, 1, 1, 1);
    }

    public void end()
    {
        if(!renderBegun)
        {
            return;
        }

        flushBatch();

        glUseProgram(0);

        renderBegun = false;
    }

    public void drawBox(Vector2f pos, Vector2f size)
    {

    }

    private void flushBatch()
    {
        if(currentVertex == 0 || currentIndex == 0 || !renderBegun)
        {
            return;
        }

        for(int i = 0; i < TEXTURE_STACK_SIZE; i++)
        {
            shader.set("isFontTexture[" + i + "]", fontTexture[i]);
        }

        IntBuffer depthFunc = IntBuffer.wrap(new int[]{ GL_LESS });
        glGetIntegerv(GL_DEPTH_FUNC, depthFunc);
        glDepthFunc(GL_LEQUAL);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

        ByteBuffer buffer = BufferUtils.createByteBuffer(vertices.length);
        
        glBufferSubData(GL_ARRAY_BUFFER, 0, currentIndex, );
    }
}
