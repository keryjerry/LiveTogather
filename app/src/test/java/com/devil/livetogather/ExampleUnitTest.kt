package com.devil.livetogather

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAgoraTokenGeneration() {
        val tokenBuilder = com.devil.livetogather.agora.RtcTokenBuilder2()
        val token = tokenBuilder.buildTokenWithUid(
            com.devil.livetogather.agora.AgoraVoiceManager.AGORA_APP_ID,
            com.devil.livetogather.agora.AgoraVoiceManager.AGORA_PRIMARY_CERTIFICATE,
            "123456",
            654321,
            com.devil.livetogather.agora.RtcTokenBuilder2.Role.ROLE_PUBLISHER,
            86400,
            86400
        )
        println("Generated Agora RTC Token: $token")
        assertTrue("Token must not be blank", token.isNotBlank())
        assertTrue("Token must start with 007", token.startsWith("007"))
    }
}