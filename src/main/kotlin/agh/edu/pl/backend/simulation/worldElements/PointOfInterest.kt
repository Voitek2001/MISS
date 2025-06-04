package agh.edu.pl.backend.simulation.worldElements

import agh.edu.pl.backend.simulation.agent.Position
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color

class PointOfInterest(
    position: Position,
    var currentNumberOfPeople: Int = 0
): WorldElement(position, 1.0) {


    companion object {
        val pointOfInterestFillShapeFunction = GraphicsContext::fillRoundRect
        val pointOfInterestColor = Color.BLUE
    }

}