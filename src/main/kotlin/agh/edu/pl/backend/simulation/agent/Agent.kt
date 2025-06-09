package agh.edu.pl.backend.simulation.agent

import agh.edu.pl.backend.simulation.simulation.WorldConfig
import agh.edu.pl.backend.simulation.policy.GovernmentPolicy
import agh.edu.pl.backend.simulation.tracker.*
import agh.edu.pl.backend.simulation.worldElements.Hospital
import agh.edu.pl.backend.simulation.worldElements.House
import agh.edu.pl.backend.simulation.worldElements.PointOfInterest
import agh.edu.pl.backend.simulation.worldElements.WorldElement
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random


class Agent(
    var position: Position,
    private var simulationTracker: SimulationTracker,
    private val house: House,
    private val hospital: Hospital,
    private val pointOfInterest: PointOfInterest,
    private val worldConfig: WorldConfig,
    private var daysAlive: Int = 0,
    private var daysLatent: Int = 0,
    private var daysInfected: Int = 0,
    private var daysImmune: Int = 0,
    var healthStatus: HealthStatus = HealthStatus.HEALTHY
) : NextDayObserver {

    private val latentObservers = mutableListOf<LatentObserver>()
    private val deathObservers = mutableListOf<DeathObserver>()
    private val immuneObservers = mutableListOf<ImmuneObserver>()
    private val asymptomaticObservers = mutableListOf<AsymptomaticObserver>()
    private val symptomaticObservers = mutableListOf<SymptomaticObserver>()
    private var isMovingToPOI = false

    init {
        latentObservers.add(simulationTracker)
        deathObservers.add(simulationTracker)
        immuneObservers.add(simulationTracker)
        asymptomaticObservers.add(simulationTracker)
        symptomaticObservers.add(simulationTracker)
    }

    fun startInfection() {
        changeStatus(HealthStatus.LATENT)
    }

    fun stayAtHomeOrHospital(numberOfInfectedAround: Int, currentPolicy: GovernmentPolicy) {
        if (healthStatus == HealthStatus.DEAD) return
        if (healthStatus == HealthStatus.HEALTHY) {
            if (Random.nextDouble() < currentPolicy.maskRate) {
                infect(numberOfInfectedAround, worldConfig.infectiousnessWithMask.value)
            } else {
                infect(numberOfInfectedAround, worldConfig.infectiousness.value)
            }
        }

    }


    fun commute(numberOfInfectedAround: Int, currentPolicy: GovernmentPolicy) {
        if (healthStatus == HealthStatus.DEAD) return

        if (currentPolicy.areaLockdown ||
            currentPolicy.isolation && healthStatus == HealthStatus.SYMPTOMATIC) {
            return
        }

        if (healthStatus == HealthStatus.SYMPTOMATIC) {
            moveToward(hospital)
            return
        }

        if (!isMovingToPOI && pointOfInterest.currentNumberOfPeople >= currentPolicy.gatheringLimit) {
            return
        }

        if (!isMovingToPOI) {
            isMovingToPOI = true
            pointOfInterest.currentNumberOfPeople++
        }

        moveToward(pointOfInterest)
        checkIfCanBeInfected(currentPolicy, numberOfInfectedAround)

    }

    private fun infect(numberOfInfectedAround: Int, infectiousness: Double) {
        if (healthStatus != HealthStatus.HEALTHY) return

        if (infectionChance(numberOfInfectedAround, infectiousness) > Random.nextDouble()) {
            changeStatus(HealthStatus.LATENT)
        }
    }

    override fun nextDay() {
        if (healthStatus == HealthStatus.DEAD) return
        daysAlive++
        when (healthStatus) {
            HealthStatus.LATENT -> {
                daysLatent++
                nextDayForLatent()
            }
            HealthStatus.IMMUNE -> {
                daysImmune++
                nextDayForImmune()
            }
            HealthStatus.ASYMPTOMATIC, HealthStatus.SYMPTOMATIC -> {
                daysInfected++
                nextDayForInfected()
            }

            else -> {}
        }

    }

    private fun nextDayForLatent() {
        if (daysLatent == LATENT_PERIOD_DAYS && Random.nextDouble() < ASYMPTOTIC_INFECTION_RATE) {
            changeStatus(HealthStatus.ASYMPTOMATIC)
        } else {
            changeStatus(HealthStatus.SYMPTOMATIC)
        }
    }

    private fun nextDayForImmune() {
        if (daysImmune == IMMUNITY_LOSS_DAYS) {
            changeStatus(HealthStatus.HEALTHY)
            daysImmune = 0
            daysLatent = 0
            daysInfected = 0

            if (Random.nextDouble() < REINFECTION_RATE) {
                changeStatus(HealthStatus.LATENT)
            }
        }
    }

    private fun nextDayForInfected() {
        if (daysInfected >= INFECTION_PERIOD_DAYS) {
            if (Random.nextDouble() < INFECTION_FATALITY_RATE) {
                changeStatus(HealthStatus.DEAD)
            } else {
                changeStatus(HealthStatus.IMMUNE)
                daysImmune = 0
            }
        }
    }

    fun isInfected() = healthStatus == HealthStatus.SYMPTOMATIC || healthStatus == HealthStatus.ASYMPTOMATIC

    private fun infectionChance(numberOfInfected: Int, infectiousness: Double) =
        1 - (1 - infectiousness).pow(numberOfInfected)

    fun returnHomeOrStayAtHospital(numberOfInfectedAround: Int, currentPolicy: GovernmentPolicy) {

        if (isMovingToPOI) {
            isMovingToPOI = false
            pointOfInterest.currentNumberOfPeople--
        }
        if (healthStatus == HealthStatus.DEAD) return
        if (healthStatus == HealthStatus.SYMPTOMATIC && position == hospital.position) {
            //stay at hospital
            return
        }
        moveToward(house)
        checkIfCanBeInfected(currentPolicy, numberOfInfectedAround)
    }

    fun reset(simulationTracker: SimulationTracker) {
        this.simulationTracker = simulationTracker
        healthStatus = HealthStatus.HEALTHY
        position = house.position
        daysAlive = 0
        daysLatent = 0
        daysInfected = 0
        daysImmune = 0
    }

    private fun checkIfCanBeInfected(currentPolicy: GovernmentPolicy, numberOfInfectedAround: Int) {
        if (healthStatus == HealthStatus.HEALTHY) {
            if (Random.nextDouble() < currentPolicy.maskRate) {
                infect(numberOfInfectedAround, worldConfig.infectiousnessWithMask.value)
            } else {
                infect(numberOfInfectedAround, worldConfig.infectiousness.value)
            }
        }
    }

    private fun moveToward(dest: WorldElement) {

        val direction = dest.position.minus(position)

        val stepX = when {
            direction.x > 0 -> min(15, direction.x)
            direction.x < 0 -> max(-15, direction.x)
            else -> 0
        }

        val stepY = when {
            direction.y > 0 -> min(15, direction.y)
            direction.y < 0 -> max(-15, direction.y)
            else -> 0
        }

        position = position.plus(Position(stepX, stepY))
    }

    private fun changeStatus(newHealthStatus: HealthStatus) {
        val oldHealthStatus = healthStatus
        healthStatus = newHealthStatus
        simulationTracker.updateStatusTracking(oldHealthStatus, newHealthStatus)
    }

    override fun toString(): String {
        return buildString {
            append("Agent at $position")
            append(" | Alive: $daysAlive days")
            if (isInfected()) append(" | Infected for: $daysLatent days")
            append(" | Status: $healthStatus")
        }
    }
}