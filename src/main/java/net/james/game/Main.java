package net.james.game;

import net.james.game.core.Color;
import net.james.game.core.Screen;
import net.james.game.rendering.Shader;
import org.lwjgl.Version;

public class Main
{
    private static final Screen screen = new Screen(800, 600, "Hello LWJGL!", Color.fromHex("#ffffffff"));

    public void run()
    {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        try
        {
            screen.open();

            Shader shader = new Shader("shaders/standard.glsl");

            loop();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        screen.close();
    }

    private void loop()
    {
        while (!screen.shouldClose())
        {
            screen.newFrame();
        }
    }

    public static void main(String[] args)
    {
        new Main().run();
    }
}