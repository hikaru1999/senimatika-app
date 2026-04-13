package com.LambdaProject.MathArt.data.model

import com.LambdaProject.MathArt.data.InteractionType
import com.LambdaProject.MathArt.data.ObjectType
import com.LambdaProject.MathArt.data.TileType
import com.LambdaProject.MathArt.data.isBlocking

data class TileData(
    val ground: TileType,
    var groundVariant: String = "tile_ground_1",
    val obj: ObjectType = ObjectType.NONE,
    val objectVariant: String = ""
)

fun TileData.isWalkable(): Boolean {
    val groundWalkable = groundVariant.startsWith("tile_path")
    // Station is now non-walkable as requested
    return groundWalkable && !obj.isBlocking() && obj != ObjectType.STATION
}

fun TileData.getInteraction(): InteractionType {
    return when (obj) {
        ObjectType.CHEST -> InteractionType.CHEST
        ObjectType.BOSS -> InteractionType.BOSS
        ObjectType.PORTAL -> InteractionType.PORTAL
        ObjectType.STATION -> InteractionType.STATION
        ObjectType.FLAG, ObjectType.FINISH -> InteractionType.FINISH
        else -> InteractionType.NONE
    }
}
