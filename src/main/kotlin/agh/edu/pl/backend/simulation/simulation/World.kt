package agh.edu.pl.backend.simulation.simulation

import agh.edu.pl.backend.simulation.agent.Agent
import agh.edu.pl.backend.simulation.agent.INFECTION_RADIUS

class World(
    private val people: List<Agent>
) {


    fun getNumberOfInfectedAround(agent: Agent) = people.count { secondPerson ->
        agent.position.manhattanDistance(secondPerson.position) < INFECTION_RADIUS
                && secondPerson.isInfected()
    }

}