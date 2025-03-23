package agh.edu.pl.backend.simulation.agent

import agh.edu.pl.backend.simulation.tracker.*
import kotlin.math.pow
import kotlin.random.Random


class Person(
    var position: Position,
    val simulationTracker: SimulationTracker,
    private var daysAlive: Int = 0,
    private var daysInfected: Int = 0,
    var healthStatus: HealthStatus = HealthStatus.HEALTHY
) : NextDayObserver {

    private val infectionObservers = mutableListOf<InfectionObserver>()
    private val deathObservers = mutableListOf<DeathObserver>()
    private val immuneObservers = mutableListOf<ImmuneObserver>()

    init {
        infectionObservers.add(simulationTracker)
        deathObservers.add(simulationTracker)
        immuneObservers.add(simulationTracker)
    }

    fun infect(numberOfInfectedAround: Int, infectiousness: Double) {
        if (healthStatus != HealthStatus.HEALTHY) return

        if (infectionChance(numberOfInfectedAround, infectiousness) > Random.nextDouble()) {
            changeStatus(HealthStatus.INFECTED)
        }
    }

    fun death() {
        if (isInfected() && daysInfected >= 10) { //TODO jakos madrzej to zrobić
            changeStatus(HealthStatus.DEAD)
        }
    }

    fun makeRandomMove() {
        if (healthStatus == HealthStatus.DEAD) return
        position = position.plus(Position(Random.nextInt(-10, 10), Random.nextInt(-10, 10)))
    }

    fun immune(immuneRate: Double) {
        if (isInfected() && recoveryChance(immuneRate) > Random.nextDouble()) {
            changeStatus(HealthStatus.IMMUNE)
        }
    }

    override fun nextDay() {
        if (healthStatus == HealthStatus.DEAD) return
        daysAlive++
        if (healthStatus == HealthStatus.IMMUNE) return
        if (healthStatus == HealthStatus.INFECTED) {
            daysInfected++
        }
    }

    fun isInfected() = healthStatus == HealthStatus.INFECTED

    private fun infectionChance(numberOfInfected: Int, infectiousness: Double) =
        1 - (1 - infectiousness).pow(numberOfInfected + 1)

    private fun recoveryChance(immuneRate: Double) = 1 - (1 - immuneRate)


    private fun changeStatus(newHealthStatus: HealthStatus) {
        healthStatus = newHealthStatus
        if (healthStatus == HealthStatus.DEAD) deathNotify()
        if (healthStatus == HealthStatus.IMMUNE) immuneNotify()
        if (healthStatus == HealthStatus.INFECTED) infectionNotify()
    }

    private fun immuneNotify() {
        immuneObservers.forEach {
            it.updateImmune()
        }
    }

    private fun deathNotify() {
        deathObservers.forEach {
            it.updateDeath()
        }
    }

    private fun infectionNotify() {
        infectionObservers.forEach {
            it.updateInfection()
        }
    }


    override fun toString(): String {
        return buildString {
            append("Person at $position")
            append(" | Alive: $daysAlive days")
            if (isInfected()) append(" | Infected for: $daysInfected days")
            append(" | Status: $healthStatus")
        }
    }
}