package agh.edu.pl.backend.simulation.worldElements

import agh.edu.pl.backend.simulation.agent.Position
import javafx.scene.paint.Color
import javafx.scene.canvas.GraphicsContext

class Hospital(
    position: Position
): WorldElement(position, 0.0) {

    companion object {
        val hospitalFillShapeFunction = GraphicsContext::fillRect
        val hospitalColor = Color.RED

    }



}