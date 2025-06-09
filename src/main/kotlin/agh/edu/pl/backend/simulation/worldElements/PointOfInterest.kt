package agh.edu.pl.backend.simulation.worldElements

import agh.edu.pl.backend.simulation.agent.Position

class PointOfInterest(
    position: Position,
    var currentNumberOfPeople: Int = 0
): WorldElement(position, 1.0)