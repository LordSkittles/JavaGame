#version 150

#if VERTEX_SHADER

in vec4 position;
in vec4 color;
in vec2 texCoord;

out vec4 vColor;
out vec4 vTexCoord;
out float vTextureID;

uniform mat4 projectionMatrix;

void main()
{
    vColor = color;
    vTexCoord = texCoord;
    vTextureID = position.w;

    gl_Position = projectionMatrix * vec4(position.x, position.y, position.z, 1.0f);
}

#endif

#if FRAGMENT_SHADER

in vec4 vColor;
in vec2 vTexCoord;
in float vTextureID;

out vec4 fragColor;

const int TEXTURE_STACK_SIZE = 16;

uniform sampler2D textureStack[TEXTURE_STACK_SIZE];
uniform int isFontTexture[TEXTURE_STACK_SIZE];

void main()
{
    int id = int(vTextureID);

    if(id < TEXTURE_STACK_SIZE)
    {
        vec4 rgba = texture2D(textureStack[id], vTexCoord);

        if(isFontTexture[id] == 1)
        {
            rgba = rgba.rrrr;
        }

        fragColor = rgba * vColor;
    }
    else
    {
        fragColor = vColor;
    }

    if(fragColor.a < 0.001f)
    {
        discard;
    }
}

#endif