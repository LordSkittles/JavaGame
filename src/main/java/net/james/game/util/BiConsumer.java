package net.james.game.util;

@FunctionalInterface
public interface BiConsumer<T, U>
{
    void accept(T t, U u);
}