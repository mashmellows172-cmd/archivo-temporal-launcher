package com.archivotemporal.launcher.apps

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

class CatalogVisibilityTest {
    @Test
    fun manifest_declares_both_launcher_activity_queries() {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val manifest = factory.newDocumentBuilder().parse(File("src/main/AndroidManifest.xml"))
        val queries = manifest.getElementsByTagName("queries").item(0) as? Element
        val intents = queries?.getElementsByTagName("intent")
        val categories = (0 until (intents?.length ?: 0)).map { index ->
            val intent = intents!!.item(index) as Element
            val children = intent.getElementsByTagName("category")
            (0 until children.length).map { childIndex ->
                (children.item(childIndex) as Element).getAttributeNS("http://schemas.android.com/apk/res/android", "name")
            }
        }.flatten()

        assertEquals(
            setOf("android.intent.category.LEANBACK_LAUNCHER", "android.intent.category.LAUNCHER"),
            categories.toSet(),
        )
    }
}
