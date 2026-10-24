package com.archivotemporal.launcher.apps

import android.content.Intent
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class AppLaunchPolicyTest {
    @Test
    fun resolve_prefers_leanback_intent() {
        val leanback = Intent("com.example.LEANBACK")
        val standard = Intent("com.example.STANDARD")

        assertSame(leanback, AppLaunchPolicy.resolve(leanback, standard))
    }

    @Test
    fun resolve_falls_back_to_standard_launch_intent() {
        val standard = Intent("com.example.STANDARD")

        assertSame(standard, AppLaunchPolicy.resolve(null, standard))
    }

    @Test
    fun resolve_returns_null_when_package_is_not_launchable() {
        assertNull(AppLaunchPolicy.resolve(null, null))
    }

    @Test
    fun resolve_does_not_query_standard_when_leanback_exists() {
        val leanback = Intent("com.example.LEANBACK")
        var standardWasQueried = false

        val resolved = AppLaunchPolicy.resolve(leanback) {
            standardWasQueried = true
            Intent("com.example.STANDARD")
        }

        assertSame(leanback, resolved)
        org.junit.Assert.assertFalse(standardWasQueried)
    }
}
