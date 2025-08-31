package com.lionido.noizevoice.systems

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.lionido.noizevoice.NoizeGame

class ParticleSystem {

    private val particles = mutableListOf<Particle>()

    data class Particle(
        val position: Vector2,
        val velocity: Vector2,
        val color: Color,
        val size: Float,
        var life: Float,
        val maxLife: Float,
        val type: ParticleType
    ) {
        fun update(deltaTime: Float) {
            position.add(velocity.x * deltaTime, velocity.y * deltaTime)
            life -= deltaTime

            // Эффекты в зависимости от типа
            when (type) {
                ParticleType.SOUND_WAVE -> {
                    velocity.scl(0.98f) // Затухание
                    color.a = life / maxLife
                }
                ParticleType.MUSIC_NOTE -> {
                    velocity.y += 100f * deltaTime // Подъем вверх
                    color.a = life / maxLife
                }
                ParticleType.ENERGY_SPARK -> {
                    velocity.add(MathUtils.random(-50f, 50f), MathUtils.random(-50f, 50f))
                }
                ParticleType.CASSETTE_GLOW -> {
                    size += 20f * deltaTime
                    color.a = (life / maxLife) * 0.7f
                }
            }
        }

        fun isDead(): Boolean = life <= 0f
    }

    enum class ParticleType {
        SOUND_WAVE,    // Звуковые волны
        MUSIC_NOTE,    // Музыкальные ноты
        ENERGY_SPARK,  // Искры энергии
        CASSETTE_GLOW  // Свечение кассет
    }

    fun createSoundWave(x: Float, y: Float, intensity: Float = 1f) {
        for (i in 0 until (10 * intensity).toInt()) {
            val angle = MathUtils.random(0f, 360f)
            val speed = MathUtils.random(100f, 300f) * intensity
            val velocity = Vector2(
                MathUtils.cosDeg(angle) * speed,
                MathUtils.sinDeg(angle) * speed
            )

            particles.add(Particle(
                Vector2(x, y),
                velocity,
                Color.CYAN.cpy(),
                MathUtils.random(5f, 15f),
                MathUtils.random(0.5f, 1.5f),
                1.5f,
                ParticleType.SOUND_WAVE
            ))
        }
    }

    fun createMusicNotes(x: Float, y: Float, count: Int = 5) {
        for (i in 0 until count) {
            val velocity = Vector2(
                MathUtils.random(-50f, 50f),
                MathUtils.random(80f, 150f)
            )

            particles.add(Particle(
                Vector2(x + MathUtils.random(-20f, 20f), y),
                velocity,
                Color.YELLOW.cpy(),
                MathUtils.random(8f, 12f),
                MathUtils.random(2f, 3f),
                3f,
                ParticleType.MUSIC_NOTE
            ))
        }
    }

    fun createEnergyBurst(x: Float, y: Float) {
        for (i in 0 until 15) {
            val angle = MathUtils.random(0f, 360f)
            val speed = MathUtils.random(50f, 200f)
            val velocity = Vector2(
                MathUtils.cosDeg(angle) * speed,
                MathUtils.sinDeg(angle) * speed
            )

            particles.add(Particle(
                Vector2(x, y),
                velocity,
                Color.BLUE.cpy(),
                MathUtils.random(3f, 8f),
                MathUtils.random(1f, 2f),
                2f,
                ParticleType.ENERGY_SPARK
            ))
        }
    }

    fun createCassetteEffect(x: Float, y: Float, cassetteColor: Color) {
        for (i in 0 until 8) {
            particles.add(Particle(
                Vector2(x + MathUtils.random(-10f, 10f), y + MathUtils.random(-10f, 10f)),
                Vector2(0f, MathUtils.random(20f, 50f)),
                cassetteColor.cpy(),
                MathUtils.random(10f, 20f),
                2f,
                2f,
                ParticleType.CASSETTE_GLOW
            ))
        }
    }

    fun update(deltaTime: Float) {
        particles.forEach { it.update(deltaTime) }
        particles.removeAll { it.isDead() }
    }

    fun render(batch: SpriteBatch) {
        if (particles.isEmpty()) return

        batch.end()

        val shapeRenderer = ShapeRenderer()
        shapeRenderer.projectionMatrix = batch.projectionMatrix
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        particles.forEach { particle ->
            shapeRenderer.color = particle.color

            when (particle.type) {
                ParticleType.SOUND_WAVE -> {
                    shapeRenderer.circle(particle.position.x, particle.position.y, particle.size)
                }
                ParticleType.MUSIC_NOTE -> {
                    // Простая нота
                    shapeRenderer.circle(particle.position.x, particle.position.y, particle.size)
                    shapeRenderer.rect(
                        particle.position.x + particle.size,
                        particle.position.y,
                        2f,
                        particle.size * 2
                    )
                }
                ParticleType.ENERGY_SPARK -> {
                    shapeRenderer.rect(
                        particle.position.x - 1f,
                        particle.position.y - 1f,
                        particle.size,
                        particle.size
                    )
                }
                ParticleType.CASSETTE_GLOW -> {
                    shapeRenderer.circle(particle.position.x, particle.position.y, particle.size)
                }
            }
        }

        shapeRenderer.end()
        batch.begin()
    }

    fun clear() {
        particles.clear()
    }

    fun getParticleCount(): Int = particles.size
}