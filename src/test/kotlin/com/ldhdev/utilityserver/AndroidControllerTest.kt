package com.ldhdev.utilityserver

import com.ldhdev.utilityserver.android.AndroidController
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
internal class AndroidControllerTest {

    @Autowired
    lateinit var controller: AndroidController

    @Test
    fun kotlinScript_CorrectStatement_Success() {
        val result = controller.executeScript("2+3", "kotlin")
        assertTrue(result.success)
        assertEquals(result.result, "5")
    }

    @Test
    fun kotlinScript_IncorrectStatement_Failure() {
        val result = controller.executeScript("abc", "kotlin")
        assertFalse(result.success)
    }

    @Test
    fun javaScript_CorrectStatement_Success() {
        val result = controller.executeScript("[] == false", "js")
        assertTrue(result.success)
        assertEquals(result.result, "true")
    }

    @Test
    fun javaScript_IncorrectStatement_Failure() {
        val result = controller.executeScript("abc", "js")
        assertFalse(result.success)
    }

    @Test
    fun unknownScript_Throws() {
        assertThrows<ResponseStatusException> {
            controller.executeScript("1", "python")
        }
    }
}