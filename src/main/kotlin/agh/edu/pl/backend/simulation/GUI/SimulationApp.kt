package agh.edu.pl.backend.simulation.GUI

import agh.edu.pl.backend.simulation.SimulationEngine
import agh.edu.pl.backend.simulation.WorldConfig
import agh.edu.pl.backend.simulation.utils.Percentage
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage

class SimulationApp : Application() {

    private var gc: GraphicsContext? = null

    override fun start(primaryStage: Stage) {
        var simulationEngine: SimulationEngine? = null
        var simulationThread: Thread

        val root = BorderPane()
        val controls = VBox(10.0)
        val buttons = HBox(10.0)
        val canvas = Canvas(500.0, 500.0)
        gc = canvas.graphicsContext2D
        init()
        // Suwaki parametrów
        val infectionRate = Slider(0.0, 1.0, 0.5).apply {
            isShowTickLabels = true
            isShowTickMarks = true
        }

        val infectionLabel = Label("Infection Rate")

        val peopleNumber = Slider(10.0, 250.0, 1.0).apply {
            isShowTickLabels = true
            isShowTickMarks = true
        }


        val recoveryRate = Slider(0.0, 1.0, 0.5).apply {
            isShowTickLabels = true
            isShowTickMarks = true
        }
        val recoveryLabel = Label("Recovery Rate")

        // Przycisk sterowania
        val startBtn = Button("Start")

        startBtn.setOnMouseClicked {
            simulationThread = Thread(simulationEngine)
            simulationThread.start()
        }

        val setupBtn = Button("Setup")

        setupBtn.setOnMouseClicked {
            simulationEngine?.stopThread()
            val worldConfig = WorldConfig(
                width = 500,
                height = 500,
                startNumberOfPeople = peopleNumber.value.toInt(),
                infectiousness = Percentage.from(infectionRate.value)?: throw RuntimeException(),
                recoverChance = Percentage.from(recoveryRate.value)?: throw RuntimeException(),
                daysOfSimulation = 20,
                infectionDistanceThreshold = 5
            )
            simulationEngine = SimulationEngine(worldConfig, this)
        }
        buttons.children.addAll(startBtn, setupBtn)

//        // Informacje o stanie
//        val healthyLabel = Label("Healthy: 100")
//        val infectedLabel = Label("Infected: 10")
//        val recoveredLabel = Label("Recovered: 0")

        //listenery

        val infectionRateLabel = Label("Infection Rate: ${infectionRate.value}")
        infectionRate.valueProperty().addListener { _, _, newValue ->
            infectionRateLabel.text = "Infection Rate: ${"%.2f".format(newValue.toDouble())}"
        }

        val recoveryRateLabel = Label("Recovery Rate: ${recoveryRate.value}")
        recoveryRate.valueProperty().addListener { _, _, newValue ->
            recoveryRateLabel.text = "Recovery Rate: ${"%.2f".format(newValue.toDouble())}"
        }

        val peopleNumberLabel = Label("People number: ${peopleNumber.value.toInt()}")
        peopleNumber.valueProperty().addListener { _, _, newValue ->
            peopleNumberLabel.text = "People number: ${newValue.toInt()}"
        }

        // Układ kontrolny
        controls.children.addAll(
            infectionRateLabel, infectionRate,
            recoveryRateLabel, recoveryRate,
            peopleNumberLabel, peopleNumber,
            buttons,
//            healthyLabel, infectedLabel, recoveredLabel
        )
        root.right = controls
        root.center = canvas



        val scene = Scene(root, 700.0, 500.0)
        primaryStage.title = "Virus Simulation"
        primaryStage.scene = scene
        primaryStage.show()
    }


    fun updateSimulationGrid(simulationElements: List<SimulationCanvasElements>) {
        gc!!.clearRect(0.0, 0.0, 500.0, 500.0)
        simulationElements.forEach {
            gc!!.fill = it.color
            gc!!.fillOval(it.position.x.toDouble(), it.position.y.toDouble(), 10.0, 10.0)
        }

    }
}
