package com.laya.desk.portal

import androidx.compose.ui.graphics.Color
import com.google.android.filament.Camera
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.MaterialInstance
import com.google.android.filament.Renderer
import com.google.android.filament.RenderTarget
import com.google.android.filament.Scene
import com.google.android.filament.Skybox
import com.google.android.filament.Texture
import com.google.android.filament.TextureSampler
import com.google.android.filament.View
import com.google.android.filament.Viewport
import io.github.sceneview.EngineDestroyQueue
import io.github.sceneview.loaders.MaterialLoader
import io.github.sceneview.math.Direction
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.node.ConeNode
import io.github.sceneview.node.CubeNode
import io.github.sceneview.node.GeometryNode
import io.github.sceneview.node.Node
import io.github.sceneview.node.PlaneNode
import io.github.sceneview.node.SphereNode

/**
 * Owns a tiny offscreen world and the rectangle that samples it. Only the rectangle/back/border
 * are children of this node; interior entities belong exclusively to the private Filament scene.
 * All methods run on the host SceneView's main/render thread.
 */
class PortalNode(
    engine: Engine,
    private val materials: MaterialLoader,
    private val renderer: Renderer,
) : Node(engine) {
    private val ownedMaterials = mutableListOf<MaterialInstance>()
    private val interior = mutableListOf<GeometryNode>()
    private var colorTexture: Texture? = null
    private var depthTexture: Texture? = null
    private var target: RenderTarget? = null
    private var world: Scene? = null
    private var portalView: View? = null
    private var cameraEntity: Int? = null
    private var portalCamera: Camera? = null
    private var sky: Skybox? = null
    private var display: PlaneNode? = null
    private var closed = false

    init {
        try {
            createResources()
        } catch (failure: Exception) {
            destroy()
            throw failure
        }
    }

    private fun color(value: Long): MaterialInstance =
        materials.createUnlitColorInstance(Color(value)).also { ownedMaterials += it }

    private fun createResources() {
        val color = Texture.Builder().width(640).height(400).levels(1)
            .sampler(Texture.Sampler.SAMPLER_2D).format(Texture.InternalFormat.RGBA8)
            .usage(Texture.Usage.COLOR_ATTACHMENT or Texture.Usage.SAMPLEABLE).build(engine)
            .also { colorTexture = it }
        val depth = Texture.Builder().width(640).height(400).levels(1)
            .sampler(Texture.Sampler.SAMPLER_2D).format(Texture.InternalFormat.DEPTH24)
            .usage(Texture.Usage.DEPTH_ATTACHMENT).build(engine).also { depthTexture = it }
        val renderTarget = RenderTarget.Builder()
            .texture(RenderTarget.AttachmentPoint.COLOR, color)
            .texture(RenderTarget.AttachmentPoint.DEPTH, depth).build(engine).also { target = it }
        val scene = engine.createScene().also { world = it }
        val entity = EntityManager.get().create().also { cameraEntity = it }
        val camera = engine.createCamera(entity).also { portalCamera = it }
        // A custom target cannot use Renderer.ClearOptions. A skybox fills untouched pixels.
        scene.skybox = Skybox.Builder().color(0.36f, 0.62f, 0.70f, 1f).build(engine).also { sky = it }
        engine.createView().also { portalView = it }.apply {
            this.scene = scene
            this.camera = camera
            this.renderTarget = renderTarget
            viewport = Viewport(0, 0, 640, 400)
            isPostProcessingEnabled = false
            setShadowingEnabled(false)
            antiAliasing = View.AntiAliasing.NONE
            dithering = View.Dithering.NONE
        }

        val image = materials.createImageInstance(color, TextureSampler(
            TextureSampler.MinFilter.LINEAR, TextureSampler.MagFilter.LINEAR,
            TextureSampler.WrapMode.CLAMP_TO_EDGE,
        )).also { ownedMaterials += it }
        display = PlaneNode(engine, size = Size(PortalProjection.WIDTH, PortalProjection.HEIGHT, 0f),
            normal = Direction(0f, 0f, 1f), materialInstance = image).apply {
            isVisible = false // Never sample an uninitialised render target.
            isTouchable = false
            this@PortalNode.addChildNode(this)
        }
        val rim = color(0xFF34464B)
        addChildNode(CubeNode(engine, size = Size(0.306f, 0.206f, 0.004f),
            center = Position(0f, 0f, -0.003f), materialInstance = rim))
        for (side in listOf(-1, 1)) {
            addChildNode(CubeNode(engine, size = Size(0.006f, 0.212f, 0.008f),
                center = Position(side * 0.153f, 0f, 0f), materialInstance = rim))
            addChildNode(CubeNode(engine, size = Size(0.312f, 0.006f, 0.008f),
                center = Position(0f, side * 0.103f, 0f), materialInstance = rim))
        }
        createWorld()
    }

    private fun addInterior(node: GeometryNode) {
        interior += node
        world!!.addEntity(node.entity)
    }

    private fun createWorld() {
        val grass = color(0xFF6E966C)
        val path = color(0xFFE2D6B4)
        val grid = color(0xFF567651)
        val near = color(0xFFE7AC4B)
        val far = color(0xFF687CBD)
        val cream = color(0xFFFFEDC7)
        val skin = color(0xFFAD7653)
        val hair = color(0xFF382C27)
        fun box(size: Size, position: Position, material: MaterialInstance) {
            addInterior(CubeNode(engine, size = size, center = position, materialInstance = material))
        }
        box(Size(1.5f, 0.02f, 1.35f), Position(0f, -0.11f, -0.70f), grass)
        box(Size(0.11f, 0.004f, 1.25f), Position(0f, -0.098f, -0.65f), path)
        for (step in 1..6) {
            box(Size(1.4f, 0.002f, 0.004f), Position(0f, -0.098f, -step * 0.20f), grid)
        }
        for (column in listOf(-2, -1, 1, 2)) {
            box(Size(0.004f, 0.002f, 1.3f), Position(column * 0.20f, -0.098f, -0.68f), grid)
        }
        // Different shape AND colour make near/far occlusion and UV orientation easy to judge.
        box(Size(0.055f, 0.13f, 0.055f), Position(-0.11f, -0.035f, -0.20f), near)
        box(Size(0.15f, 0.035f, 0.06f), Position(0.15f, 0.10f, -0.72f), far)
        for (side in listOf(-1, 1)) {
            box(Size(0.025f, 0.20f, 0.06f), Position(0.15f + side * 0.065f, 0f, -0.72f), far)
        }
        box(Size(0.12f, 0.09f, 0.10f), Position(-0.22f, -0.055f, -1.05f), near)

        // Static fairy-shaped depth marker, facing into the world (-Z).
        addInterior(ConeNode(engine, radius = 0.026f, height = 0.046f, sideCount = 12,
            center = Position(0f, -0.077f, -0.34f), materialInstance = cream))
        box(Size(0.024f, 0.025f, 0.020f), Position(0f, -0.048f, -0.34f), grass)
        addInterior(SphereNode(engine, radius = 0.018f, stacks = 10, slices = 12,
            center = Position(0f, -0.022f, -0.34f), materialInstance = skin))
        addInterior(SphereNode(engine, radius = 0.017f, stacks = 10, slices = 12,
            center = Position(0f, -0.016f, -0.332f), materialInstance = hair))
        for (side in listOf(-1, 1)) {
            addInterior(CubeNode(engine, size = Size(0.018f, 0.050f, 0.006f),
                materialInstance = cream).apply {
                position = Position(side * 0.025f, -0.045f, -0.324f)
                rotation = Rotation(0f, 0f, side * 24f)
            })
        }
    }

    /** Called before the host begins its swap-chain frame; never from a separate animation loop. */
    fun renderFrom(viewerWorld: Position): Boolean {
        if (closed) return false
        val eye = getLocalPosition(viewerWorld) // Uses the live parent/anchor transform.
        val x = eye.x.toDouble()
        val y = eye.y.toDouble()
        val z = eye.z.toDouble()
        val frustum = PortalProjection.forEye(x, y, z)
        if (frustum == null) {
            display?.isVisible = false
            return false
        }
        portalCamera!!.apply {
            lookAt(x, y, z, x, y, z - 1.0, 0.0, 1.0, 0.0)
            setProjection(Camera.Projection.PERSPECTIVE, frustum.left, frustum.right,
                frustum.bottom, frustum.top, frustum.near, frustum.far)
        }
        renderer.renderStandaloneView(portalView!!)
        display?.isVisible = true
        return true
    }

    fun hideInterior() { if (!closed) display?.isVisible = false }

    override fun destroy() {
        if (closed) return
        closed = true
        // In this pin, Node.destroy() releases Filament components, but not EntityManager IDs.
        // Only our own direct children/interior nodes are included, never the host DSL parent.
        val ownedEntityIds = listOf(entity) + childNodes.map { it.entity } + interior.map { it.entity }
        // A host disposal can reach descendants after the engine has already reclaimed its
        // resources. Do not call any native node/scene API through those stale handles.
        if (!engine.isValid) {
            ownedEntityIds.forEach { EntityManager.get().destroy(it) }
            cameraEntity?.let { EntityManager.get().destroy(it) }
            clearReferences()
            return
        }
        // Detaching notifies the host node manager synchronously before material release.
        parent = null
        super.destroy()
        display = null
        interior.forEach { world?.removeEntity(it.entity); it.destroy() }
        interior.clear()
        ownedEntityIds.forEach { EntityManager.get().destroy(it) }
        portalView?.let { engine.destroyView(it) }
        portalView = null
        world?.skybox = null
        sky?.let { engine.destroySkybox(it) }
        sky = null
        world?.let { engine.destroyScene(it) }
        world = null
        cameraEntity?.let {
            engine.destroyEntity(it)
            EntityManager.get().destroy(it)
        }
        cameraEntity = null
        portalCamera = null
        target?.let { engine.destroyRenderTarget(it) }
        target = null
        ownedMaterials.forEach { materials.destroyMaterialInstance(it) }
        ownedMaterials.clear()
        // Filament can retain a bound texture after material destruction. Use SceneView's
        // three-frame queue, which also drains when the owning host engine is torn down.
        colorTexture?.let { EngineDestroyQueue.of(engine).enqueueTexture(it) }
        depthTexture?.let { EngineDestroyQueue.of(engine).enqueueTexture(it) }
        colorTexture = null
        depthTexture = null
    }

    private fun clearReferences() {
        display = null
        interior.clear()
        portalView = null
        sky = null
        world = null
        cameraEntity = null
        portalCamera = null
        target = null
        ownedMaterials.clear()
        colorTexture = null
        depthTexture = null
    }
}
