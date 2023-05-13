package com.lexiqb.awakening.world;

import com.lexiqb.awakening.entities.Player;
import com.lexiqb.awakening.ui.GameplayHUD;
import com.rubynaxela.kyanite.game.Scene;
import com.rubynaxela.kyanite.graphics.ConstView;
import com.rubynaxela.kyanite.graphics.RectangleShape;
import com.rubynaxela.kyanite.graphics.Texture;
import com.rubynaxela.kyanite.graphics.View;
import com.rubynaxela.kyanite.math.*;
import com.rubynaxela.kyanite.util.Unit;
import com.rubynaxela.kyanite.window.Window;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class World extends Scene {

    private final Vector2i size;
    private final Window window;
    private Player player;
    private ArrayList<Sensor> sensors = new ArrayList<>();
    private final float dissipationFactor = 1f, criticalLevel = 25f;
    private float dangerLevel = 0f;

    public World(@NotNull Vector2i size, @NotNull Texture background) {
        this(size.x, size.y, background);
    }

    public World(int width, int height, @NotNull Texture background) {
        size = Vec2.i(width, height);
        window = getContext().getWindow();
        setOrderingPolicy(OrderingPolicy.Y_BASED);
        final var backgroundShep = new RectangleShape(width, height);
        backgroundShep.setTexture(background);
        backgroundShep.setLayer(-1);
        add(backgroundShep);
    }

    public Player getPlayer() {
        return player;
    }

    public void attachPlayer(@NotNull Player player) {
        this.player = player;
        add(player);
        player.assignWorld(this);
    }

    private void readjustView() {
        final var playerPosition = player.getPosition();
        final var windowSize = window.getSize();
        final var offsetY = player.getGlobalBounds().height  / player.getScale().y / 3f;
        final boolean followOnX = playerPosition.x > windowSize.x / 2f && playerPosition.x < size.x - windowSize.x / 2f;
        final boolean followOnY = playerPosition.y > windowSize.y / 2f + offsetY && playerPosition.y < size.y - windowSize.y / 2f + offsetY;

        final ConstView view = getContext().getWindow().getView();
        final var playerCenter = player.getGlobalBounds().getCenter();
        float viewCenterX = view.getCenter().x, viewCenterY = view.getCenter().y;
        if (followOnX) viewCenterX = playerCenter.x;
        if (followOnY) viewCenterY = playerCenter.y + player.getGlobalBounds().height / 2f - offsetY;
        ((View) view).setCenter(Vec2.f(viewCenterX, viewCenterY));
        getContext().getWindow().setView(view);
    }

    @Override
    protected void init() {
        Sensor testS = new Sensor();
        testS.setPosition(600, 200);
        testS.assignWorld(this);
        sensors.add(testS);
        add(testS);
        updateOrder();
    }

    @Override
    protected void loop() {
        if (dangerLevel > criticalLevel) {
            // TODO game over, ROBUST eats you
        }
        dangerLevel -= 0.5f * getDeltaTime().asSeconds();
        if (dangerLevel < 0f) dangerLevel = 0f;

        readjustView();

        player.update(getDeltaTime());

        getContext().getWindow().<GameplayHUD>getHUD().setStamina(player.getStaminaPercentage());
        getContext().getWindow().<GameplayHUD>getHUD().setNoise(dangerLevel / criticalLevel);

        updateOrder();
    }

    public IntRect getBounds() {
        return new IntRect(0, 0, size.x, size.y);
    }

    public void makeNoise(Vector2f position, float volume) {
        // TODO calculate strength of noise, alert sensors
        for (var s : sensors) {
            float distSqr = (MathUtils.pow((long) (s.getPosition().x - position.x), 2) + MathUtils.pow((long) (s.getPosition().y - position.y), 2)) / 10000f;
            s.disturb(convertTodB(convertFromdB(volume) / (distSqr * dissipationFactor)));
        }
    }

    public void increaseDangerLevel(float value) {
        dangerLevel += value;
    }

    public float convertTodB(@Unit("W/m^2") float value) {
        return (float) (10 * Math.log10(value * Math.pow(10, 12)));
    }

    public float convertFromdB(@Unit("dB") float value) {
        return (float) Math.pow(10, (value/10f - 12));
    }
}
