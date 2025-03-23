package agh.edu.pl.backend.simulation

import agh.edu.pl.backend.simulation.GUI.SimulationApp
import agh.edu.pl.backend.simulation.GUI.SimulationCanvasElements
import agh.edu.pl.backend.simulation.agent.HealthStatus
import agh.edu.pl.backend.simulation.tracker.SimulationTracker
import javafx.scene.paint.Color


class SimulationEngine(
    worldConfig: WorldConfig,
    private val simulationApp: SimulationApp
): Runnable {

    private val simulationTracker: SimulationTracker = SimulationTracker(worldConfig.startNumberOfPeople)
    private val world: World = World(worldConfig, simulationTracker)
    private var isRunning = false
    init {
        updateGrid()
    }

    override fun run() {
        isRunning = true
        while (isRunning) {

            // 1. infection faze
            handleInfectionFaze()
            // 2. death faze
            handleDeathFaze()
            // 3. make move
            handleMoveFaze()
            // 4. immune faze
            handleImmuneFaze()
            // 5. nextDay
            handleNextDay()
            // 6. updateGrid
            updateGrid()

            world.people.forEach {
                println(it)
            }
            println("----------------")
            Thread.sleep(2000)

        }

    }

    fun stopThread() {
        isRunning = false
    }

    private fun handleInfectionFaze() = world.handleInfection()

    private fun handleDeathFaze() = world.handleDeath()
    private fun handleMoveFaze() = world.handleMove()
    private fun handleImmuneFaze() = world.handleImmune()
    private fun handleNextDay() = world.notifyNextDay()
    private fun updateGrid() {
        println(simulationTracker.getDescriptionString())
        val simulationElements = world.people.map {
            SimulationCanvasElements(getColorForStatus(it.healthStatus), it.position)
        }
        simulationApp.updateSimulationGrid(simulationElements)

        //TODO dodać jeszcze wyswietlanie tego co liczy simulationTracker
    }

    private fun getColorForStatus(healthStatus: HealthStatus): Color {
        return when(healthStatus) {
            HealthStatus.HEALTHY -> Color.GREEN
            HealthStatus.INFECTED -> Color.GRAY
            HealthStatus.IMMUNE -> Color.BLUE
            HealthStatus.DEAD -> Color.RED
        }
    }

}