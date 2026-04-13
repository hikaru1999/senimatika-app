import com.LambdaProject.MathArt.data.ObjectType
import com.LambdaProject.MathArt.data.TileType

fun String.toTileType(): TileType {
    return when (this) {
        "GROUND" -> TileType.GROUND
        "PATH" -> TileType.PATH
        else -> TileType.GROUND
    }
}

fun String.toObjectType(): ObjectType {
    return try {
        ObjectType.valueOf(this)
    } catch (e: Exception) {
        ObjectType.NONE
    }
}