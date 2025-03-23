package agh.edu.pl.backend.simulation.utils

import agh.edu.pl.backend.simulation.agent.Position
import kotlin.random.Random


fun getRandomPosition(maxWidth: Int, maxHeight: Int) =
    Position(Random.nextInt(0, maxHeight), Random.nextInt(0, maxWidth))
