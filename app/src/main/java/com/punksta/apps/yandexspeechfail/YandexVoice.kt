package com.punksta.apps.yandexspeechfail

import ru.yandex.speechkit.Voice

enum class YandexVoice(val yandexVoiceId: String) {
    ALYSS(Voice.ALYSS.value),
    ERMIL(Voice.ERMIL.value),
    JANE(Voice.JANE.value),
    OMAZH(Voice.OMAZH.value),
    ZAHAR(Voice.ZAHAR.value)
}