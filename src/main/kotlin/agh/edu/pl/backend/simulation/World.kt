package agh.edu.pl.backend.simulation

import agh.edu.pl.backend.simulation.agent.Person
import agh.edu.pl.backend.simulation.tracker.SimulationTracker
import agh.edu.pl.backend.simulation.utils.getRandomPosition

class World(
    private val worldConfig: WorldConfig,
    simulationTracker: SimulationTracker
) {

    val people: List<Person> = mutableListOf()

    init {
        for (i in 0..< worldConfig.startNumberOfPeople) {
            people.addLast(Person(getRandomPosition(worldConfig.width, worldConfig.height), simulationTracker = simulationTracker))
        }
    }

    fun handleInfection() = people.forEach { person ->
            person.infect(getNumberOfInfectedAround(person), worldConfig.infectiousness.value)
        }

    fun handleDeath() = people.forEach { it.death() }

    fun handleImmune() =
        people.forEach { it.immune(worldConfig.recoverChance.value) }

    fun handleMove() {
        people.forEach {
            it.makeRandomMove()
        }
    }

    fun notifyNextDay() = people.forEach {
            it.nextDay()
        }

    private fun getNumberOfInfectedAround(person: Person) = people.count { secondPerson ->
        person.position.manhattanDistance(secondPerson.position) < worldConfig.infectionDistanceThreshold
                && secondPerson.isInfected()
    }

}