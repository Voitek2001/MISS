package agh.edu.pl.backend.simulation.agent

import kotlin.math.abs

class Position(
    val x: Int,
    val y: Int
) {

    operator fun plus(position: Position) = Position(x + position.x, y + position.y)
    operator fun minus(position: Position) = Position(x - position.x, y - position.y)

//    fun euclideanDistance(position: Position): Float {
        //Może jest sens tej użyć, nie wiem
//    }

    fun manhattanDistance(position: Position) = abs(position.x - x) + abs(position.y - y)



    override fun toString(): String {
        return "pos:x${x}y:${y}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Position

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}