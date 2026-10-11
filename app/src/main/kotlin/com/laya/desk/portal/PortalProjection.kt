package com.laya.desk.portal

import kotlin.math.abs

/** Metres in portal-local coordinates: +X right, +Y up, +Z toward the viewer. */
object PortalProjection {
    const val WIDTH = 0.30f
    const val HEIGHT = 0.20f
    const val CENTER_HEIGHT = 0.25f

    data class Frustum(
        val left: Double, val right: Double, val bottom: Double, val top: Double,
        val near: Double, val far: Double,
    )

    fun forEye(x: Double, y: Double, z: Double): Frustum? {
        if (!x.isFinite() || !y.isFinite() || !z.isFinite()) return null
        // A front-only window. Reject the singular plane/back and extreme grazing angles.
        if (z !in 0.08..2.5 || abs(x) > z * 1.5 || abs(y) > z * 1.2) return null
        // The near plane IS the aperture (z=0), so near / eyeDistance = 1.
        // The camera axes remain parallel to the portal; never lookAt the window centre.
        return Frustum(
            -WIDTH.toDouble() / 2 - x, WIDTH.toDouble() / 2 - x,
            -HEIGHT.toDouble() / 2 - y, HEIGHT.toDouble() / 2 - y,
            z, z + 2.0,
        )
    }
}
