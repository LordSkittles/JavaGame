package net.james.game.rendering;

import org.lwjgl.stb.STBImage;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL30C.GL_RG;
import static org.lwjgl.opengl.GL30C.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture
{
    public enum ETextureFormat
    {
        R(1),
        RG(2),
        RGB(3),
        RGBA(4);

        public final int index;

        ETextureFormat(int index)
        {
            this.index = index;
        }
    }

    public int width;
    public int height;

    private int handle;

    private String fileName;
    private ETextureFormat textureFormat;
    private ByteBuffer pixels;

    public Texture(String fileName)
    {
        this.fileName = fileName;
        load(fileName);
    }

    public Texture(int w, int h, ETextureFormat format, ByteBuffer pixels)
    {
        this.width = w;
        this.height = h;
        this.textureFormat = format;

        create(this.width, this.height, this.textureFormat, pixels);
    }

    public void load(String fileName)
    {
        if (this.handle != 0)
        {
            glDeleteTextures(this.handle);
            this.width = 0;
            this.height = 0;
            this.handle = 0;
            this.fileName = "none";
        }

        IntBuffer w = IntBuffer.allocate(1), h = IntBuffer.allocate(1), format = IntBuffer.allocate(1);
        this.pixels = STBImage.stbi_load(fileName, w, h, format, STBImage.STBI_default);

        if (this.pixels != null)
        {
            this.handle = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, this.handle);

            switch (format.get())
            {
                case STBI_grey:
                {
                    textureFormat = ETextureFormat.R;
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, w.get(), h.get(), 0, GL_RED, GL_UNSIGNED_INT, this.pixels);
                    break;
                }
                case STBI_grey_alpha:
                {
                    textureFormat = ETextureFormat.RG;
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RG, w.get(), h.get(), 0, GL_RG, GL_UNSIGNED_INT, this.pixels);
                    break;
                }
                case STBI_rgb:
                {
                    textureFormat = ETextureFormat.RGB;
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, w.get(), h.get(), 0, GL_RGB, GL_UNSIGNED_INT, this.pixels);
                    break;
                }
                case STBI_rgb_alpha:
                {
                    textureFormat = ETextureFormat.RGBA;
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(), h.get(), 0, GL_RGBA, GL_UNSIGNED_INT, this.pixels);
                    break;
                }
                default:
                {
                    break;
                }
            }

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

            glGenerateMipmap(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);

            this.width = w.get();
            this.height = h.get();
            this.fileName = fileName;
        }
    }

    public void create(int w, int h, ETextureFormat format, ByteBuffer pixels)
    {
        if (this.handle != 0)
        {
            glDeleteTextures(this.handle);
            this.handle = 0;
            this.fileName = "none";
        }

        this.width = w;
        this.height = h;
        this.textureFormat = format;

        this.handle = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, this.handle);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        switch (this.textureFormat)
        {
            case R:
            {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, this.width, this.height, 0, GL_RED, GL_UNSIGNED_INT, pixels);
                break;
            }
            case RG:
            {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RG, this.width, this.height, 0, GL_RG, GL_UNSIGNED_INT, pixels);
                break;
            }
            case RGB:
            {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, this.width, this.height, 0, GL_RGB, GL_UNSIGNED_INT, pixels);
                break;
            }
            case RGBA:
            {
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_INT, pixels);
                break;
            }
            default:
            {
                break;
            }
        }

        this.pixels = pixels;
    }

    public void bind(int slot)
    {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, this.handle);
    }
}
