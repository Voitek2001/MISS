package agh.edu.pl.backend.simulation.gui

import agh.edu.pl.backend.simulation.agent.Position

data class SimulationCanvasElementsWithImage(
    val position: Position,
    val kind: WorldElementKind
)
