package com.archivotemporal.launcher

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class ManifestContractTest {
    private val androidNamespace = "http://schemas.android.com/apk/res/android"

    @Test
    fun declaresTvOnlyLauncherContract() {
        val manifestFile = File("src/main/AndroidManifest.xml")
        assertTrue("Main manifest must exist", manifestFile.isFile)

        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val manifest = factory.newDocumentBuilder().parse(manifestFile).documentElement
        val features = manifest.getElementsByTagName("uses-feature")
        val leanback = (0 until features.length)
            .map { features.item(it) as Element }
            .find { it.getAttributeNS(androidNamespace, "name") == "android.software.leanback" }
        assertNotNull("Leanback feature must be declared", leanback)

        val touchscreen = (0 until features.length)
            .map { features.item(it) as Element }
            .find { it.getAttributeNS(androidNamespace, "name") == "android.hardware.touchscreen" }
        assertNotNull("Touchscreen feature must be declared", touchscreen)
        assertEquals("false", touchscreen!!.getAttributeNS(androidNamespace, "required"))

        val application = manifest.getElementsByTagName("application").item(0) as Element
        assertTrue("Application banner must be declared", application.hasAttributeNS(androidNamespace, "banner"))

        val activity = manifest.getElementsByTagName("activity").item(0) as Element
        assertTrue("Leanback activity banner must be declared", activity.hasAttributeNS(androidNamespace, "banner"))

        val categories = manifest.getElementsByTagName("category")
        assertTrue(
            "Leanback launcher category must be declared",
            (0 until categories.length)
                .map { categories.item(it) as Element }
                .any { it.getAttributeNS(androidNamespace, "name") == "android.intent.category.LEANBACK_LAUNCHER" },
        )
    }
}
