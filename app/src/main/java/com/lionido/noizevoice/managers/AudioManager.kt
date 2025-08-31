package com.lionido.noizevoice.managers

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable

class AudioManager : Disposable {
    private var currentMusic: Music? = null
    private val sounds = mutableMapOf<String, Sound>()
    private val musicTracks = mutableMapOf<String, Music>()

    var musicVolume = 0.7f
    var soundVolume = 0.8f
    var isMusicEnabled = true
    var isSoundEnabled = true

    init {
        loadAudioAssets()
    }

    private fun loadAudioAssets() {
        try {
            // Загрузка треков Noize MC
            loadMusic("make_some_noize", "audio/music/make_some_noize.mp3")
            loadMusic("vidihay", "audio/music/vidihay.mp3")
            loadMusic("vselennaya", "audio/music/vselennaya_beskonechna.mp3")
            loadMusic("igra_slov", "audio/music/igra_slov.mp3")
            loadMusic("moe_more", "audio/music/moe_more.mp3")
            loadMusic("mercedes", "audio/music/mercedes_s666.mp3")

            // Загрузка звуковых эффектов
            loadSound("jump", "audio/sfx/jump.wav")
            loadSound("attack", "audio/sfx/attack.wav")
            loadSound("collect", "audio/sfx/collect.wav")
            loadSound("hurt", "audio/sfx/hurt.wav")
            loadSound("ability", "audio/sfx/ability.wav")
            loadSound("rhythm_hit", "audio/sfx/rhythm_hit.wav")
            loadSound("cassette_pickup", "audio/sfx/cassette_pickup.wav")

        } catch (e: Exception) {
            Gdx.app.log("AudioManager", "Ошибка загрузки аудио: ${e.message}")
        }
    }

    private fun loadMusic(name: String, path: String) {
        try {
            if (Gdx.files.internal(path).exists()) {
                musicTracks[name] = Gdx.audio.newMusic(Gdx.files.internal(path))
            }
        } catch (e: Exception) {
            Gdx.app.log("AudioManager", "Не удалось загрузить музыку $name: ${e.message}")
        }
    }

    private fun loadSound(name: String, path: String) {
        try {
            if (Gdx.files.internal(path).exists()) {
                sounds[name] = Gdx.audio.newSound(Gdx.files.internal(path))
            }
        } catch (e: Exception) {
            Gdx.app.log("AudioManager", "Не удалось загрузить звук $name: ${e.message}")
        }
    }

    fun playMusic(trackName: String, loop: Boolean = true) {
        if (!isMusicEnabled) return

        currentMusic?.stop()

        musicTracks[trackName]?.let { music ->
            currentMusic = music
            music.isLooping = loop
            music.volume = musicVolume
            music.play()
            Gdx.app.log("AudioManager", "Играет трек: $trackName")
        }
    }

    fun stopMusic() {
        currentMusic?.stop()
        currentMusic = null
    }

    fun pauseMusic() {
        currentMusic?.pause()
    }

    fun resumeMusic() {
        currentMusic?.play()
    }

    fun playSound(soundName: String, volume: Float = 1f) {
        if (!isSoundEnabled) return

        sounds[soundName]?.play(soundVolume * volume)
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        currentMusic?.volume = musicVolume
    }

    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }

    fun toggleMusic() {
        isMusicEnabled = !isMusicEnabled
        if (!isMusicEnabled) {
            stopMusic()
        }
    }

    fun toggleSound() {
        isSoundEnabled = !isSoundEnabled
    }

    // Специальные методы для ритм-битв
    fun startRhythmBattle(trackName: String) {
        playMusic(trackName, false)
        Gdx.app.log("AudioManager", "Начинается ритм-битва с треком: $trackName")
    }

    fun getCurrentMusicPosition(): Float {
        return currentMusic?.position ?: 0f
    }

    fun isMusicPlaying(): Boolean {
        return currentMusic?.isPlaying ?: false
    }

    override fun dispose() {
        currentMusic?.dispose()
        musicTracks.values.forEach { it.dispose() }
        sounds.values.forEach { it.dispose() }
        musicTracks.clear()
        sounds.clear()
    }
}