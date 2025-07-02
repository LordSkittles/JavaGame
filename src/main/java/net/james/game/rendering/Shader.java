package net.james.game.rendering;

import net.james.game.util.BiConsumer;
import net.james.game.util.TriConsumer;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.lwjgl.opengl.GL40.*;

public class Shader
{
    public enum EShaderStage
    {
        Undefined(0, "undefined"),
        Vertex(1, "VERTEX_SHADER"),
        Fragment(5, "FRAGMENT_SHADER");

        public final int id;
        public final String directive;

        EShaderStage(int id, String directive)
        {
            this.id = id;
            this.directive = directive;
        }
    }

    public static class SubShader
    {
        public String lastError;

        private EShaderStage stage;
        private int handle;

        public SubShader(EShaderStage stage, String fileName)
        {
            this.stage = EShaderStage.Undefined;
            this.handle = 0;

            this.load(stage, fileName);
        }

        public void load(EShaderStage stage, String fileName)
        {
            createGlShader(stage);

            String source = getShaderSource(fileName);

            compileShaderSource(source);
        }

        private void createGlShader(EShaderStage stage) throws RuntimeException
        {
            if (stage.id == 0)
            {
                throw new RuntimeException("Invalid shader stage provided.");
            }

            this.stage = stage;

            switch (stage)
            {
                case Vertex:
                {
                    this.handle = glCreateShader(GL_VERTEX_SHADER);
                    break;
                }
                case Fragment:
                {
                    this.handle = glCreateShader(GL_FRAGMENT_SHADER);
                    break;
                }
            }
        }

        private void compileShaderSource(String source) throws RuntimeException
        {
            StringBuilder stringBuilder = getSourceWithDirective(source);

            glShaderSource(this.handle, stringBuilder.toString());
            glCompileShader(this.handle);

            IntBuffer success = BufferUtils.createIntBuffer(1);
            glGetShaderiv(this.handle, GL_COMPILE_STATUS, success);
            if (success.get(0) == GL_FALSE)
            {
                this.lastError = glGetShaderInfoLog(this.handle);
            }
        }

        private StringBuilder getSourceWithDirective(String source) throws RuntimeException
        {
            int versionPos = source.indexOf("#version");
            if (versionPos == -1)
            {
                throw new RuntimeException("No #version directive found in shader");
            }

            int versionEnd = source.indexOf("\n", versionPos) == -1 ? source.length() : versionPos + 1;

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(source, 0, versionPos);
            stringBuilder.append("#define ");
            stringBuilder.append(this.stage.directive);
            stringBuilder.append('\n');
            stringBuilder.append(source, versionEnd, source.length());
            return stringBuilder;
        }

        private static String getShaderSource(String fileName)
        {
            StringBuilder stringBuilder = new StringBuilder();
            String source;

            try
            {
                InputStream inputStream = Shader.class.getClassLoader().getResourceAsStream(fileName);

                assert inputStream != null;
                Scanner scanner = new Scanner(inputStream);
                while (scanner.hasNextLine())
                {
                    stringBuilder.append(scanner.nextLine());
                }
                scanner.close();

                source = stringBuilder.toString();
                stringBuilder.delete(0, stringBuilder.length());
            }
            catch (NullPointerException e)
            {
                throw new RuntimeException(e);
            }
            return source;
        }
    }

    public String lastError;

    private final List<SubShader> shaders;
    private int handle;

    public Shader(String fileName)
    {
        this.shaders = new ArrayList<>();
        this.handle = 0;
        this.lastError = "";

        this.load(fileName);
        this.link();
    }

    public void bind() throws RuntimeException
    {
        if (this.handle == 0)
        {
            throw new RuntimeException("Invalid Shader Program!");
        }

        glUseProgram(this.handle);
    }

    public void set(String name, int value) throws RuntimeException
    {
        glUniform1i(getUniformValidated(name), value);
    }

    public void set(String name, float value) throws RuntimeException
    {
        glUniform1f(getUniformValidated(name), value);
    }

    public void set(String name, Vector2f value) throws RuntimeException
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(2);
        value.get(floatBuffer);
        set(name, floatBuffer, GL20::glUniform2fv);
    }

    public void set(String name, Vector3f value) throws RuntimeException
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(3);
        value.get(floatBuffer);
        set(name, floatBuffer, GL20::glUniform2fv);
    }

    public void set(String name, Vector4f value) throws RuntimeException
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
        value.get(floatBuffer);
        set(name, floatBuffer, GL20::glUniform4fv);
    }

    public void set(String name, Matrix2f value) throws RuntimeException
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(4);
        value.get(floatBuffer);
        set(name, floatBuffer, GL20::glUniformMatrix2fv);
    }

    public void set(String name, Matrix3f value) throws RuntimeException
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(9);
        value.get(floatBuffer);
        set(name, floatBuffer, GL20::glUniformMatrix3fv);
    }

    public void set(String name, Matrix4f value) throws RuntimeException
    {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
        value.get(floatBuffer);
        set(name, floatBuffer, GL20::glUniformMatrix4fv);
    }

    public int getUniform(String name)
    {
        return glGetUniformLocation(handle, name);
    }

    private int getUniformValidated(String name) throws RuntimeException
    {
        if (this.handle == 0)
        {
            throw new RuntimeException("Invalid Shader Program!");
        }

        int loc = getUniform(name);
        if (loc < 0)
        {
            throw new RuntimeException("Uniform " + name + " not found!");
        }

        return loc;
    }

    private void set(String name, FloatBuffer buffer, BiConsumer<Integer, FloatBuffer> setter) throws RuntimeException
    {
        setter.accept(getUniformValidated(name), buffer);
    }

    private void set(String name, FloatBuffer buffer, TriConsumer<Integer, Boolean, FloatBuffer> setter) throws RuntimeException
    {
        setter.accept(getUniformValidated(name), false, buffer);
    }

    private void load(String fileName)
    {
        for (EShaderStage stage : EShaderStage.values())
        {
            if (stage == EShaderStage.Undefined)
            {
                continue;
            }

            shaders.add(new SubShader(stage, fileName));
        }
    }

    private void link()
    {
        this.handle = glCreateProgram();
        for (SubShader shader : this.shaders)
        {
            glAttachShader(this.handle, shader.handle);
        }

        glLinkProgram(this.handle);

        IntBuffer success = BufferUtils.createIntBuffer(1);
        glGetShaderiv(this.handle, GL_COMPILE_STATUS, success);
        if (success.get(0) == GL_FALSE)
        {
            this.lastError = glGetShaderInfoLog(this.handle);
        }
    }
}
