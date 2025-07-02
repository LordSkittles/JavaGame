package net.james.game.core;

import org.joml.Vector4f;

public class Color
{
    public static Color fromHex(String hexColor)
    {
        String hex = hexColor;
        if (hex.charAt(0) == '#')
        {
            hex = hex.substring(1);
        }

        if (hex.length() != 6 && hex.length() != 8)
        {
            throw new IllegalArgumentException("Invalid hex color format: " + hexColor);
        }

        long decimal = Long.parseLong(hex, 16); // Use long to avoid integer overflow

        if (hex.length() == 6)
        {
            return new Color(
                    (int) ((decimal >> 16) & 0xff),
                    (int) ((decimal >> 8) & 0xff),
                    (int) (decimal & 0xff),
                    255 // Default alpha
            );
        }

        return new Color(
                (int) ((decimal >> 24) & 0xff),
                (int) ((decimal >> 16) & 0xff),
                (int) ((decimal >> 8) & 0xff),
                (int) (decimal & 0xff)
        );
    }

    private int value;

    public Color(int r, int g, int b)
    {
        this(r, g, b, 255);
    }

    public Color(int r, int g, int b, int a)
    {
        // Ensure values are in valid range [0, 255]
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));
        a = Math.max(0, Math.min(255, a));

        this.value = (r << 24) | (g << 16) | (b << 8) | a;
    }

    public Color(int value)
    {
        this.value = value;
    }

    public Vector4f toVec4()
    {
        return new Vector4f(
                red() / 255.0f,
                green() / 255.0f,
                blue() / 255.0f,
                alpha() / 255.0f
        );
    }

    public int red()
    {
        return (value >> 24) & 0xff;
    }

    public void setRed(int r)
    {
        r = Math.max(0, Math.min(255, r));
        value = (value & 0x00ffffff) | (r << 24);
    }

    public int green()
    {
        return (value >> 16) & 0xff;
    }

    public void setGreen(int g)
    {
        g = Math.max(0, Math.min(255, g));
        value = (value & 0xff00ffff) | (g << 16);
    }

    public int blue()
    {
        return (value >> 8) & 0xff;
    }

    public void setBlue(int b)
    {
        b = Math.max(0, Math.min(255, b));
        value = (value & 0xffff00ff) | (b << 8);
    }

    public int alpha()
    {
        return value & 0xff;
    }

    public void setAlpha(int a)
    {
        a = Math.max(0, Math.min(255, a));
        value = (value & 0xffffff00) | a;
    }

    @Override
    public String toString()
    {
        return String.format("Color{r=%d, g=%d, b=%d, a=%d}", red(), green(), blue(), alpha());
    }
}