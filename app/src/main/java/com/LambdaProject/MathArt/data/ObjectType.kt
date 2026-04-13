package com.LambdaProject.MathArt.data

enum class ObjectType {
    NONE,
    BLUE_BANNER,
    RED_BANNER,
    FLAG,
    BUSHES_LARGE,
    BUSHES_MEDIUM,
    BUSHES_SMALL,
    CAMPFIRE,
    CASTLE_ROUND,
    CASTLE_SQUARE,
    HOUSE,
    ROCK_LARGE,
    ROCK_MEDIUM,
    ROCK_MEDIUM_1,
    ROCK_SMALL_1,
    ROCK_SMALL_2,
    TENT,
    TREE_LARGE,
    TREE_MEDIUM,
    TREE_SMALL,
    TREE_STUMP_TALL,
    TREE_STUMP_LONG,
    WATCHTOWER_TALL,
    WATCHTOWER_SHORT,
    WINDMILL,
    BARREL,
    STATION,
    FENCE_HORIZONTAL,
    FENCE_VERTICAL,
    WELL,
    CHEST,
    CART,
    BOSS,
    PORTAL,
    FINISH
}

fun ObjectType.isBlocking(): Boolean {
    return when (this) {
        ObjectType.BLUE_BANNER,
        ObjectType.RED_BANNER,
        // ObjectType.FLAG dihilangkan dari sini agar bisa dilewati
        ObjectType.BUSHES_LARGE,
        ObjectType.BUSHES_MEDIUM,
        ObjectType.BUSHES_SMALL,
        ObjectType.CAMPFIRE,
        ObjectType.CASTLE_ROUND,
        ObjectType.CASTLE_SQUARE,
        ObjectType.HOUSE,
        ObjectType.TENT,
        ObjectType.TREE_LARGE,
        ObjectType.TREE_MEDIUM,
        ObjectType.TREE_SMALL,
        ObjectType.TREE_STUMP_TALL,
        ObjectType.TREE_STUMP_LONG,
        ObjectType.WATCHTOWER_TALL,
        ObjectType.WATCHTOWER_SHORT,
        ObjectType.WINDMILL,
        ObjectType.BARREL,
        ObjectType.FENCE_HORIZONTAL,
        ObjectType.FENCE_VERTICAL,
        ObjectType.WELL -> true

        else -> false
    }
}
