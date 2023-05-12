package com.lexiqb.awakening;

import com.rubynaxela.kyanite.game.Game;
import com.rubynaxela.kyanite.window.Window;

public class Awakening extends Game {

    public static void main(String[] args) {
        Game.run(Awakening.class, args);
    }

    @Override
    protected void preInit() {
    }

    @Override
    protected void init() {
        getContext().setupWindow(1200, 800);
        getContext().getWindow().setScene(new FirstScene());
    }
}