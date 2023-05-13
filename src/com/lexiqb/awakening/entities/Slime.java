package com.lexiqb.awakening.entities;

import com.rubynaxela.kyanite.game.GameContext;
import com.rubynaxela.kyanite.game.assets.AssetsBundle;
import com.rubynaxela.kyanite.game.assets.AudioHandler;
import com.rubynaxela.kyanite.game.assets.Sound;
import com.rubynaxela.kyanite.graphics.Colors;
import com.rubynaxela.kyanite.graphics.Texture;
import com.rubynaxela.kyanite.graphics.TextureAtlas;
import com.rubynaxela.kyanite.input.Keyboard;
import com.rubynaxela.kyanite.math.Direction;
import com.rubynaxela.kyanite.math.Vec2;
import com.rubynaxela.kyanite.math.Vector2f;
import com.rubynaxela.kyanite.util.Time;
import org.jetbrains.annotations.NotNull;

public class Slime extends Entity {

    private static final AssetsBundle assets = GameContext.getInstance().getAssetsBundle();
    private static final AudioHandler audioHandler = GameContext.getInstance().getAudioHandler();
    private static final Sound takeoffSound = assets.get("sound.entity.slime.takeoff");
    private static final Sound landingSound = assets.get("sound.entity.slime.land");
    private final float movementSpeed, loopLength;
    private final Texture[][] animations = assets.<TextureAtlas>get("texture.entity.player").getMatrix(32, 32, 5, 4);
    private final float[] loopTimes = {0.05f, 0.1f, 0.2f, 0.25f, 0.3f, 0.4f, 0.65f, 0.75f, 0.8f, 0.85f, 0.95f, 1f};
    private final SizeClass sizeClass;
    private float movementTime = 0;
    private Direction facing = Direction.SOUTH;
    private Motion motion = Motion.IDLE;
    private boolean onGround = true;
    protected boolean restricted = true;

    public Slime(SizeClass sizeClass) {
        this.sizeClass = sizeClass;
        movementSpeed = sizeClass.movementSpeed;
        loopLength = sizeClass.loopLength;
        setSize(sizeClass.size, sizeClass.size);
        setFillColor(Colors.MEDIUM_PURPLE);
        setPosition(200, 200);
        setOrigin(getSize().x / 2f, getSize().y);
    }

    protected Motion getMotion() {
        return motion;
    }

    @Override
    public void update(@NotNull Time deltaTime) {
        movement(deltaTime);
        keepInWorldBounds(deltaTime);
    }

    public void awaken() {
        restricted = false;
    }

    protected void resetMovementTime() {
        if (movementTime < loopLength * loopTimes[4] || movementTime > loopLength * loopTimes[9])
            movementTime = 0f;
        motion = Motion.IDLE;
    }

    protected void movement(@NotNull Time deltaTime) {
        final var direction = getDirection();
        int animationIndex = getAnimationFrame();
        float modifier = 1.0f;

        // If requested and allowed movement or animation loop is unfinished, add time to finish animation
        if ((direction != Direction.NULL && !restricted) || (movementTime > 0 && !(movementTime >= loopLength * loopTimes[3] && movementTime < loopLength * loopTimes[4])))
            movementTime += deltaTime.asSeconds();
        if (direction != Direction.NULL && !restricted) {
            if (movementTime >= 0.75f * loopLength)
                movementTime -= 0.75f * loopLength;
        }
        if (animationIndex == 2) {
            if (!onGround) {
                onGround = true;
                land();
            }
        }
        if (animationIndex == 3) {
            if (onGround) {
                onGround = false;
                takeoff();
            }
        }

        if (animationIndex <= 2) {
            if (direction != Direction.NULL) {
                facing = direction;
                if (!restricted) {
                    if (Keyboard.isKeyPressed(Keyboard.Key.LCONTROL)) motion = Motion.SPRINT;
                    else if (Keyboard.isKeyPressed(Keyboard.Key.LSHIFT)) motion = Motion.SNEAK;
                    else motion = Motion.REGULAR;
                }
            }
            // Resetting the time of animation
            if ((direction == Direction.NULL || restricted) && animationIndex == 2) resetMovementTime();
            if (direction != Direction.NULL && restricted) motion = Motion.IDLE;
            setVelocity(Vector2f.zero());
        } else {
            modifier = motion.speedModifier;
            setVelocity(Vec2.multiply(facing.vector, movementSpeed * modifier));
        }

        // Setting texture
        final Direction facingNow = switch (direction) {
            case NORTH -> Direction.NORTH;
            case SOUTH -> Direction.SOUTH;
            case WEST, NORTH_WEST, SOUTH_WEST -> Direction.WEST;
            case EAST, NORTH_EAST, SOUTH_EAST -> Direction.EAST;
            default -> facing;
        };
        setTexture(animations[animationIndex][facingId(facingNow)]);
    }

    protected Direction getDirection() {
        // TODO make little slimes follow player
        return Direction.NULL;
    }

    protected int getAnimationFrame() {
        if ((movementTime >= loopLength * loopTimes[0] && movementTime < loopLength * loopTimes[1]) ||
            (movementTime >= loopLength * loopTimes[2] && movementTime < loopLength * loopTimes[3]) ||
            (movementTime >= loopLength * loopTimes[8] && movementTime < loopLength * loopTimes[9]) ||
            (movementTime >= loopLength * loopTimes[10] && movementTime < loopLength * loopTimes[11])) {
            return 1;
        }
        if ((movementTime >= loopLength * loopTimes[1] && movementTime < loopLength * loopTimes[2]) ||
            (movementTime >= loopLength * loopTimes[9] && movementTime < loopLength * loopTimes[10])) {
            return 0;
        }
        if ((movementTime >= loopLength * loopTimes[4] && movementTime < loopLength * loopTimes[5]) ||
            (movementTime >= loopLength * loopTimes[6] && movementTime < loopLength * loopTimes[7])) {
            return 3;
        }
        if ((movementTime >= loopLength * loopTimes[5] && movementTime < loopLength * loopTimes[6])) {
            return 4;
        }
        return 2;
    }

    protected void land() {
        audioHandler.playSound(landingSound, "player", 100.0f, sizeClass.soundPitch, false);
        // TODO partiiiiicleeeesssssss :DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
    }

    protected void takeoff() {
        audioHandler.playSound(takeoffSound, "player", 100.0f, sizeClass.soundPitch, false);
    }

    private static int facingId(@NotNull Direction facing) {
        return facing.ordinal() / 2;
    }

    protected enum Motion {
        IDLE(0.0f, 1.0f),
        SNEAK(0.5f, 0.75f),
        REGULAR(1.0f, 1.0f),
        SPRINT(2.0f, 1.0f);
        final float speedModifier;

        Motion(float speedModifier, float yScale) {
            this.speedModifier = speedModifier;
        }
    }

    public enum SizeClass {

        SMOL_GUY(48, 80.0f, 0.8f, 1.5f),
        PRETTY_AVERAGE(64, 120.0f, 1.2f, 1.0f),
        BIG_BOI(96, 160.0f, 1.6f, 0.75f),
        RARELY_OBSERVED_BIG_UNIDENTIFIED_SUS_THING(380, 240.0f, 3.2f, 0.5f);

        final float size, movementSpeed, loopLength, soundPitch;

        SizeClass(float size, float movementSpeed, float loopLength, float soundPitch) {
            this.size = size;
            this.movementSpeed = movementSpeed;
            this.loopLength = loopLength;
            this.soundPitch = soundPitch;
        }
    }
}
