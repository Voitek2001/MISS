package agh.edu.pl.backend.simulation.worldElements

import agh.edu.pl.backend.simulation.agent.Position
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

class House(
    position: Position
): WorldElement(position, 0.8) {


    companion object {
        val houseFillShapeFunction = GraphicsContext::fillRect
        val houseColor = Color.GREEN
    }


}