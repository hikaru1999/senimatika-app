package com.LambdaProject.MathArt.utils

import com.LambdaProject.MathArt.data.TileType
import com.LambdaProject.MathArt.R

fun TileType.getDrawable(x: Int, y: Int): Int {
    return when (this) {

        TileType.GROUND -> {
            val variants = listOf(
                R.drawable.tile_ground_1,
                R.drawable.tile_ground_2
            )
            variants[(x + y) % variants.size]
        }

        TileType.PATH -> {
            val variants = listOf(
                R.drawable.tile_path_1,
                R.drawable.tile_path_2,
                R.drawable.tile_path_3,
                R.drawable.tile_path_4,
                R.drawable.tile_path_5,
                R.drawable.tile_path_6,
                R.drawable.tile_path_7,
                R.drawable.tile_path_8,
                R.drawable.tile_path_9,
                R.drawable.tile_path_10,
                R.drawable.tile_path_11,
                R.drawable.tile_path_12,
                R.drawable.tile_path_13,
                R.drawable.tile_path_14,
                R.drawable.tile_path_15,

            )
            variants[(x * 3 + y * 7) % variants.size]
        }

        else -> R.drawable.tile_ground_1
    }
}