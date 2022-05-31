package com.ldhdev.utilityserver

import com.ldhdev.utilityserver.android.AndroidController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
internal class AndroidControllerTest {

    @Autowired
    lateinit var controller: AndroidController

    @Test
    fun executeKotlinScript_CorrectStatement_Success() {
        val result = controller.executeKotlinScript("2+3")

        assertTrue(result.success)
        assertEquals(result.result, "5")
    }

    @Test
    fun executeKotlinScript_InCorrectStatement_Failure() {
        val result = controller.executeKotlinScript("dfasd")

        assertFalse(result.success)
    }
}