package agh.edu.pl.backend.simulation.gui

import agh.edu.pl.backend.simulation.simulation.SimulationEngine
import agh.edu.pl.backend.simulation.simulation.WorldConfig
import agh.edu.pl.backend.simulation.utils.Percentage
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.image.Image
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import kotlin.math.max

class SimulationApp : Application() {

    private var gc: GraphicsContext? = null
    private lateinit var hospitalImage: Image
    private lateinit var houseImage: Image
    private lateinit var pointOfInterestImage: Image

    override fun start(primaryStage: Stage) {

        loadSimulationImages()

        var simulationEngine: SimulationEngine? = null
        var simulationThread: Thread

        val root = BorderPane()
        val controls = VBox(10.0)
        val buttons = HBox(10.0)
        val canvas = Canvas(CANVAS_WIDTH, CANVAS_HEIGHT)
        gc = canvas.graphicsContext2D
        init()
        // Suwaki parametrów

        val peopleNumber = Slider(MIN_NUMBER_OF_PEOPLE, MAX_NUMBER_OF_PEOPLE, 1.0).apply {
            isShowTickLabels = true
            isShowTickMarks = true
        }

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
                width = CANVAS_WIDTH.toInt(),
                height = CANVAS_HEIGHT.toInt(),
                startNumberOfPeople = peopleNumber.value.toInt(),
                infectiousness = Percentage.from(0.14)?: throw RuntimeException(),
                infectionDistanceThreshold = 5,
                numberOfPointOfInterest = max(2, peopleNumber.value.toInt() / 8),
                numberOfHouses = max(2, peopleNumber.value.toInt() / 5),
                numberOfHospitals = 4,
                infectiousnessWithMask = Percentage.from(max(0.14 - 0.1, 0.0))?: throw RuntimeException()
            )
            simulationEngine = SimulationEngine(worldConfig, this)
        }
        buttons.children.addAll(startBtn, setupBtn)

        //listenery

        val peopleNumberLabel = Label("People number: ${peopleNumber.value.toInt()}")
        peopleNumber.valueProperty().addListener { _, _, newValue ->
            peopleNumberLabel.text = "People number: ${newValue.toInt()}"
        }

        // Układ kontrolny
        controls.children.addAll(
            peopleNumberLabel, peopleNumber,
            buttons,
//            healthyLabel, infectedLabel, recoveredLabel
        )
        root.right = controls
        root.center = canvas

        val scene = Scene(root, SCENE_WIDTH, SCENE_HEIGHT)
        primaryStage.title = SCENE_TITLE
        primaryStage.scene = scene
        primaryStage.show()
    }


    fun updateSimulationGrid(simulationElements: List<SimulationCanvasElements>) {
        gc!!.clearRect(0.0, 0.0, CANVAS_WIDTH, CANVAS_HEIGHT)
        simulationElements.forEach {
            gc!!.fill = it.color
            gc!!.fillOval(it.position.x.toDouble(), it.position.y.toDouble(), AGENT_WIDTH, AGENT_HEIGHT)
        }

    }

    fun drawImage(pointOfInterestList: List<SimulationCanvasElementsWithImage>) {
        pointOfInterestList.forEach {
            val imageToDraw = when (it.kind) {
                WorldElementKind.HOUSE -> houseImage
                WorldElementKind.POI -> pointOfInterestImage
                WorldElementKind.HOSPITAL -> hospitalImage
            }
            gc!!.drawImage(imageToDraw, it.position.x.toDouble(), it.position.y.toDouble(), POI_WIDTH, POI_HEIGHT)
        }
    }

    private fun loadSimulationImages() {
        hospitalImage = loadImage("/hospital.png")
        houseImage = loadImage("/house.png")
        pointOfInterestImage = loadImage("/POI.png")
    }

    private fun loadImage(imagePath: String): Image {
        return Image(
            javaClass.getResource(imagePath)?.toExternalForm()
                ?: throw ResourceNotFoundException()
        )
    }
}
