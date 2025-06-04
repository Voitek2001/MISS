package agh.edu.pl.backend.simulation

import agh.edu.pl.backend.simulation.agent.Person
import agh.edu.pl.backend.simulation.tracker.SimulationTracker
import agh.edu.pl.backend.simulation.utils.getRandomPosition

class World(
    private val worldConfig: WorldConfig,
    val people: List<Person>
) {


    fun getNumberOfInfectedAround(person: Person) = people.count { secondPerson ->
        person.position.manhattanDistance(secondPerson.position) < worldConfig.infectionDistanceThreshold
                && secondPerson.isInfected()
    }

}