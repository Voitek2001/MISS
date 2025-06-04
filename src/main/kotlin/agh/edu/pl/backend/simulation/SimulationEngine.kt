package agh.edu.pl.backend.simulation

import agh.edu.pl.backend.simulation.GUI.SimulationApp
import agh.edu.pl.backend.simulation.GUI.SimulationCanvasElements
import agh.edu.pl.backend.simulation.agent.*
import agh.edu.pl.backend.simulation.tracker.SimulationTracker
import agh.edu.pl.backend.simulation.utils.getHospitalPos
import agh.edu.pl.backend.simulation.utils.getRandomPosition
import agh.edu.pl.backend.simulation.worldElements.Hospital
import agh.edu.pl.backend.simulation.worldElements.House
import agh.edu.pl.backend.simulation.worldElements.PointOfInterest
import agh.edu.pl.backend.simulation.worldElements.WorldElement
import javafx.scene.paint.Color
import java.io.File
import kotlin.math.max


class SimulationEngine(
    private val worldConfig: WorldConfig,
    private val simulationApp: SimulationApp
): Runnable {

    private var simulationTracker: SimulationTracker = SimulationTracker(worldConfig.startNumberOfPeople)
    private var isRunning = false
    private var simulationAgents: List<Person> = mutableListOf()
    private var world: World = World(worldConfig, simulationAgents)

    private val hospitals: List<Hospital> = mutableListOf()
    private val houses: List<House> = mutableListOf()
    private val pointsOfInterest: List<PointOfInterest> = mutableListOf()
    private val allWorldElements: List<WorldElement> = mutableListOf()
    var qTable = mutableMapOf<List<Int>, IntArray>()

    var epsilon = 0.99
    var epsilonDecayRate = 0.001
    val rewardsList: List<Double> = mutableListOf()


    init {

        for (i in 1..worldConfig.numberOfHospitals) {
            val hospital = Hospital(getHospitalPos(i, worldConfig.width, worldConfig.height))
            allWorldElements.addLast(hospital)
            hospitals.addLast(hospital)
        }

        for (i in 1..worldConfig.numberOfHouses) {
            val house = House(getRandomPosition(worldConfig.width, worldConfig.height))
            allWorldElements.addLast(house)
            houses.addLast(house)
        }

        for (i in 1..worldConfig.numberOfPointOfInterest) {
            val poi = PointOfInterest(getRandomPosition(worldConfig.width, worldConfig.height))
            allWorldElements.addLast(poi)
            pointsOfInterest.addLast(poi)
        }

        for (i in 1..worldConfig.startNumberOfPeople) {
            val currHouse = houses.random()
            simulationAgents.addLast(Person(currHouse.position, simulationTracker, currHouse, hospitals.random(), pointsOfInterest.random(), worldConfig))
        }

        for (i in 0 until INITIAL_INFECTION_COUNT) {
            simulationAgents[i].startInfection()
        }
        println(simulationTracker.getDescriptionString())
        updateGrid()

    }

    var train = false
    override fun run() {
        isRunning = true
//        if (!train) {
//            qTable = loadQTableFromFile("qtable.json")
//        }
        learn(2000)
    }
    val results: List<Double> = mutableListOf()
    val totalDead: List<Int> = mutableListOf()
        fun learn(maxAttempts: Int) {
        for (i in 1..maxAttempts) {
            val rewardSum = attempt()
            rewardsList.addLast(rewardSum)
            resetSimulation()
            epsilon = max(epsilon - epsilonDecayRate, 0.0)
            println(rewardsList.subList(max(rewardsList.size - 100, 0), rewardsList.size)
                .sum() / (rewardsList.size - max(rewardsList.size - 100, 0))
            )
            results.addLast(rewardsList.subList(max(rewardsList.size - 100, 0), rewardsList.size)
                .sum() / (rewardsList.size - max(rewardsList.size - 100, 0)))

            saveQTableToFile(qTable, "qtable.json")
            saveRewardsToFile("rewards_log.txt")
            saveStatsToFile("stats.csv")

        }
    }


    private fun saveRewardsToFile(fileName: String) {
        File(fileName).bufferedWriter().use { out ->
            results.forEach { result ->
                out.write("$result\n")
            }
        }
    }



    fun attempt(): Double {

        var observation = discretise(getObservation())

        var terminated = false
        var truncated = false
        var rewardSum = 0.0
        var day = 0
        while (!terminated && !truncated) {
            val action = pickAction(observation)
            val stepResult = simulateNextDays(action, day)
            // new_obs, reward, terminated, truncated, info
            val newObservation = discretise(stepResult.newObservation)
            truncated = stepResult.truncated
            terminated = stepResult.terminated
            updateKnowledge(action, observation, newObservation, stepResult.reward)
            observation = newObservation
            rewardSum += stepResult.reward
            day++
        }
        println(simulationTracker.getDescriptionString())
        totalDead.addLast(simulationTracker.numberOfDead)

        return rewardSum
    }

    private fun saveStatsToFile(fileName: String) {
        File(fileName).bufferedWriter().use { out ->
            results.forEach { result ->
                out.write("$result\n")
            }
        }
    }

    data class StepResult(
        val truncated: Boolean,
        val terminated: Boolean,
        val newObservation: List<Float>,
        val reward: Double
    )


    private fun updateKnowledge(action: Int, observation: List<Int>, newObservation: List<Int>, reward: Double) {

        val qValues = qTable.getOrPut(observation) { IntArray(6) }
        val nextQValues = qTable.getOrDefault(newObservation, IntArray(6))

        val lr = 0.05f
        val discount = 0.95f

        qValues[action] = (qValues[action] + lr * (reward + discount * nextQValues.max() - qValues[action])).toInt()
    }

    private fun discretise(observation: List<Float>): List<Int> {

        return observation.map {
            when {
                it < 0.1 -> 0
                it < 0.3 -> 1
                it < 0.6 -> 2
                else -> 3
            }
        }
    }

    private fun getObservation(): List<Float> {
        val total = simulationAgents.size.toFloat()
        return listOf(
            simulationAgents.count { it.healthStatus == HealthStatus.HEALTHY } / total,
            simulationAgents.count { it.healthStatus == HealthStatus.LATENT } / total,
            simulationAgents.count { it.healthStatus == HealthStatus.ASYMPTOMATIC } / total,
            simulationAgents.count { it.healthStatus == HealthStatus.SYMPTOMATIC } / total,
            simulationAgents.count { it.healthStatus == HealthStatus.DEAD } / total
        )
    }

    private fun pickAction(observation: List<Int>): Int {
        return if (Math.random() < epsilon) {
            (0..5).random()
        } else {
            qTable.getOrDefault(observation, IntArray(6)).withIndex().maxByOrNull { it.value }?.index ?: 0
        }
    }

    private fun resetSimulation() {
        simulationAgents = mutableListOf()
        simulationTracker = SimulationTracker(worldConfig.startNumberOfPeople)

        for (i in 1..worldConfig.startNumberOfPeople) {
            val currHouse = houses.random()
            simulationAgents.addLast(Person(currHouse.position, simulationTracker, currHouse, hospitals.random(), pointsOfInterest.random(), worldConfig))
        }
        world = World(worldConfig, simulationAgents)

        for (i in 0 until INITIAL_INFECTION_COUNT) {
            simulationAgents[i].startInfection()
        }

    }


    fun simulateNextDays(action: Int, day: Int): StepResult {
        var reward = 0.0
        val currentPolicy = predefinedPolicies[action]
        for (dayOffset in 0 until 5) {
            for (tick in 0 until 82) {
                simulationAgents.forEach { agent ->
                    when (tick) {
                        in 0..7 -> agent.stayAtHomeOrHospital(world.getNumberOfInfectedAround(agent), currentPolicy)
                        in 8..37 -> agent.commute(world.getNumberOfInfectedAround(agent), currentPolicy)
                        in 38..81 -> agent.returnHomeOrStayAtHospital(world.getNumberOfInfectedAround(agent), currentPolicy)
                    }
                }
                reward += calculateReward(action)
//                updateGrid()
//                Thread.sleep(100)

            }
            handleNextDay()
//            println(simulationTracker.getDescriptionString())
//            updateGrid()
//            Thread.sleep(500)
        }

        return StepResult(
            false, day > 300,
            getObservation(),
            reward
        )
    }

    fun calculateReward(lockdownLevel: Int): Double {
        return calculateRc() + calculateRh() + calculateRp(lockdownLevel)
    }

    private fun calculateRc(): Double =
        simulationAgents.sumOf {
            val worldElement = getWorldElementAtLocalization(it.position)
            it.healthStatus.s * (worldElement?.rewardPValue ?: 0.0)
        } / simulationAgents.size




    private fun getWorldElementAtLocalization(elementPosition: Position) = allWorldElements.find { it.position == elementPosition }


    private fun calculateRh() =
        simulationAgents.sumOf { it.healthStatus.h } / simulationAgents.size

    private fun calculateRp(lockdownLevel: Int): Double {
        return 1 - lockdownLevel.toDouble() / (predefinedPolicies.size - 1)
    }

    fun stopThread() {
        isRunning = false
    }

    private fun handleNextDay() = simulationAgents.forEach { it.nextDay() }


    private fun setUpHospitals() {
        simulationApp.setHospitals(hospitals.map {
            SimulationCanvasElements(Hospital.hospitalColor, it.position)
        })
    }

    private fun setUpHouses() {

        simulationApp.setHouses(houses.map {
            SimulationCanvasElements(House.houseColor, it.position)
        })
    }

    private fun setUpPOI() {

        simulationApp.setPOI(pointsOfInterest.map {
            SimulationCanvasElements(PointOfInterest.pointOfInterestColor, it.position)
        })
    }
    private fun updateGrid() {
//        println(simulationTracker.getDescriptionString())
        val simulationElements = simulationAgents.map {
            SimulationCanvasElements(getColorForStatus(it.healthStatus), it.position)
        }
        simulationApp.updateSimulationGrid(simulationElements)
        setUpHospitals()
        setUpHouses()
        setUpPOI()
        //TODO dodać jeszcze wyswietlanie tego co liczy simulationTracker
    }

    private fun getColorForStatus(healthStatus: HealthStatus): Color {
        return when(healthStatus) {
            HealthStatus.HEALTHY -> Color.GREEN
            HealthStatus.SYMPTOMATIC -> Color.GRAY
            HealthStatus.IMMUNE -> Color.BLUE
            HealthStatus.DEAD -> Color.RED
            HealthStatus.LATENT -> Color.DIMGRAY
            HealthStatus.ASYMPTOMATIC -> Color.DARKGRAY
        }
    }

}