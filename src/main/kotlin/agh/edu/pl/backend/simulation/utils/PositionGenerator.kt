package agh.edu.pl.backend.simulation.utils

import agh.edu.pl.backend.simulation.agent.Position
import kotlin.random.Random


fun getRandomPosition(maxWidth: Int, maxHeight: Int) =
    Position(Random.nextInt(0, maxHeight), Random.nextInt(0, maxWidth))


// * * * * * * * *
// * H * * * * H *
fun getHospitalPos(i: Int, maxWidth: Int, maxHeight: Int): Position {
    return when (i) {
        1 -> Position((0.25 * maxHeight).toInt(), (0.25 * maxWidth).toInt())
        2 -> Position((0.75 * maxHeight).toInt(), (0.25 * maxWidth).toInt())
        3 -> Position((0.25 * maxHeight).toInt(), (0.75 * maxWidth).toInt())
        else -> Position((0.75 * maxHeight).toInt(), (0.75 * maxWidth).toInt())
    }
}