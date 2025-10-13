package org.agora

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.agora.backend.module
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
