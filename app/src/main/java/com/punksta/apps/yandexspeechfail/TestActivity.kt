package com.punksta.apps.yandexspeechfail

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import ru.yandex.speechkit.*
import java.util.*
import java.util.concurrent.TimeUnit

class TestActivity : AppCompatActivity() {

    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SpeechKit.getInstance().init(this, "")
        SpeechKit.getInstance().uuid = UUID.randomUUID().toString()

        textView = TextView(this)

        setContentView(textView)
    }


    val toastLisener = object : VocalizerListener {
        override fun onPlayingBegin(p0: Vocalizer) {
            textView.text = "onPlayingBegin"
        }

        override fun onVocalizerError(p0: Vocalizer, p1: Error) {
            textView.text = "onVocalizerError"
        }

        override fun onSynthesisDone(p0: Vocalizer) {
            textView.text = "onSynthesisDone"
        }

        override fun onPartialSynthesis(p0: Vocalizer, p1: Synthesis) {
            textView.text = "onPartialSynthesis"
        }

        override fun onPlayingDone(p0: Vocalizer) {
            textView.text = "onPlayingDone"
        }

    }

    private val vocalizerCache: MutableMap<YandexVoice, OnlineVocalizer> = mutableMapOf()

    private fun getVocalizer(yandexVoice: YandexVoice): OnlineVocalizer {
        return vocalizerCache.getOrPut(yandexVoice) {
            return@getOrPut OnlineVocalizer.Builder(Language.RUSSIAN, toastLisener)
                    .setAutoPlay(true)
                    .setVoice(Voice(yandexVoice.yandexVoiceId))
                    .setEmotion(Emotion.EVIL)
                    .setQuality(Quality.LOW)
                    .build()
        }
    }

    private var lastVocalizer: OnlineVocalizer? = null;


    private val random = Random()

    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    private val getRandomText: (textSize: Int) -> String = { textSize ->
        (0..textSize).map { random.nextInt().rem(4).toChar() }.joinToString("")
    }

    private val INTERVAL_MS = 500L
    private val TEXT_SIZE = 500

    override fun onStart() {
        super.onStart()


        Observable.interval(INTERVAL_MS, TimeUnit.MILLISECONDS)
                .map { TEXT_SIZE to random.nextInt(YandexVoice.values().size) }
                .observeOn(AndroidSchedulers.mainThread())
                .map { (textSize, voiceNumber) -> getRandomText(textSize) to getVocalizer(YandexVoice.values()[voiceNumber]) }
                .subscribe { (text, vocalizer) ->

                    println("synthesize")

                    lastVocalizer?.cancel()
                    lastVocalizer = vocalizer.also {
                        it.synthesize(text, Vocalizer.TextSynthesizingMode.INTERRUPT)
                    }
                }.also {
                    compositeDisposable.add(it)
                }
    }


    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }

}
