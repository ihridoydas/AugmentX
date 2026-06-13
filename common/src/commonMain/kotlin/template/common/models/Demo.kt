package template.common.models

data class DemoItem(
    val id: String,
    val title: String,
    val description: String = ""
)

data class DemoCategory(
    val title: String,
    val items: List<DemoItem>
)

val demoCategories = listOf(
    DemoCategory(
        title = "3D",
        items = listOf(
            DemoItem("model-viewer", "Model Viewer"),
            DemoItem("geometry", "Geometry"),
            DemoItem("animation", "Animation"),
            DemoItem("scene-gallery", "Scene Gallery")
        )
    ),
    DemoCategory(
        title = "Environment",
        items = listOf(
            DemoItem("lighting", "Lighting"),
            DemoItem("movable-light", "Movable Light"),
            DemoItem("fog", "Fog"),
            DemoItem("environment", "Environment")
        )
    ),
    DemoCategory(
        title = "Interaction",
        items = listOf(
            DemoItem("camera-controls", "Camera Controls"),
            DemoItem("gesture-editing", "Gesture Editing"),
            DemoItem("collision", "Collision"),
            DemoItem("view-node", "View Node")
        )
    ),
    DemoCategory(
        title = "Content",
        items = listOf(
            DemoItem("text", "Text"),
            DemoItem("lines-paths", "Lines & Paths"),
            DemoItem("image", "Image"),
            DemoItem("billboard", "Billboard"),
            DemoItem("video", "Video")
        )
    ),
    DemoCategory(
        title = "Advanced",
        items = listOf(
            DemoItem("dynamic-sky", "Dynamic Sky"),
            DemoItem("multi-model", "Multi-Model"),
            DemoItem("materials", "Materials"),
            DemoItem("physics", "Physics"),
            DemoItem("double-pendulum", "Double Pendulum"),
            DemoItem("post-processing", "Post-Processing"),
            DemoItem("custom-mesh", "Custom Mesh"),
            DemoItem("shape", "Shape"),
            DemoItem("reflection-probes", "Reflection Probes"),
            DemoItem("secondary-camera", "Secondary Camera"),
            DemoItem("debug-overlay", "Debug Overlay")
        )
    ),
    DemoCategory(
        title = "AR",
        items = listOf(
            DemoItem("ar-placement", "AR Placement"),
            DemoItem("ar-image", "AR Image"),
            DemoItem("ar-video", "AR Video"),
            DemoItem("ar-face", "AR Face"),
            DemoItem("ar-cloud-anchor", "AR Cloud Anchor"),
            DemoItem("ar-streetscape", "AR Streetscape"),
            DemoItem("ar-pose", "AR Pose"),
            DemoItem("ar-rerun", "AR Rerun"),
            DemoItem("ar-record-playback", "AR Record Playback"),
            DemoItem("ar-depth-occlusion", "AR Depth Occlusion"),
            DemoItem("ar-instant-placement", "AR Instant Placement"),
            DemoItem("ar-terrain", "AR Terrain"),
            DemoItem("ar-rooftop", "AR Rooftop"),
            DemoItem("ar-image-stabilization", "AR Image Stabilization"),
            DemoItem("ar-orbital", "AR Orbital")
        )
    ),
    DemoCategory(
        title = "Creator",
        items = listOf(
            DemoItem("ar-creator", "Create New AR (Web)", "Upload image and 3D model"),
            DemoItem("ar-manage", "Manage My AR (Web)", "View, Edit, or Delete your AR experiences"),
            DemoItem("ar-creator_android", "Create AR (Android)", "Native SceneView Experience"),
            DemoItem("ar-manage_Android", "Manage AR (Android)", "Native Asset Management")
        )
    )
)
