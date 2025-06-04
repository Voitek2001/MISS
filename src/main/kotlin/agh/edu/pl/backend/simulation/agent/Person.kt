package agh.edu.pl.backend.simulation.agent

import agh.edu.pl.backend.simulation.WorldConfig
import agh.edu.pl.backend.simulation.tracker.*
import agh.edu.pl.backend.simulation.utils.Percentage
import agh.edu.pl.backend.simulation.worldElements.Hospital
import agh.edu.pl.backend.simulation.worldElements.House
import agh.edu.pl.backend.simulation.worldElements.PointOfInterest
import agh.edu.pl.backend.simulation.worldElements.WorldElement
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random


class Person(
    var position: Position,
    val simulationTracker: SimulationTracker,
    val house: House,
    val hospital: Hospital,
    val pointOfInterest: PointOfInterest,
    val worldConfig: WorldConfig,
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

    fun infect(numberOfInfectedAround: Int, infectiousness: Double) {
        if (healthStatus != HealthStatus.HEALTHY) return

        if (infectionChance(numberOfInfectedAround, infectiousness) > Random.nextDouble()) {
            changeStatus(HealthStatus.LATENT)
        }
    }

    override fun nextDay() {
        if (healthStatus == HealthStatus.DEAD) return
        daysAlive++
        if (healthStatus == HealthStatus.LATENT) {
            daysLatent++
            if (daysLatent == LATENT_PERIOD_DAYS) {
                if (Random.nextDouble() < ASYMPTOTIC_INFECTION_RATE) {
                    changeStatus(HealthStatus.ASYMPTOMATIC)
                } else {
                    changeStatus(HealthStatus.SYMPTOMATIC)
                }
                return
            }
        }

        if (healthStatus == HealthStatus.IMMUNE) {
            daysImmune++
            if (daysImmune == IMMUNITY_LOSS_DAYS) {
                changeStatus(HealthStatus.HEALTHY)
                daysImmune = 0
                daysLatent = 0
                daysInfected = 0

                if (Random.nextDouble() < REINFECTION_RATE) {
                    changeStatus(HealthStatus.LATENT)
                }
            }
            return
        }

        if (healthStatus == HealthStatus.ASYMPTOMATIC || healthStatus == HealthStatus.SYMPTOMATIC) {
            daysInfected++
            if (daysInfected >= INFECTION_PERIOD_DAYS) {
                if (Random.nextDouble() < INFECTION_FATALITY_RATE) {
                    changeStatus(HealthStatus.DEAD)
                } else {
                    changeStatus(HealthStatus.IMMUNE)
                    daysImmune = 0
                }
            }
            return
        }

    }

    fun isInfected() = healthStatus == HealthStatus.SYMPTOMATIC || healthStatus == HealthStatus.ASYMPTOMATIC

    private fun infectionChance(numberOfInfected: Int, infectiousness: Double) =
        1 - (1 - infectiousness).pow(numberOfInfected)

    private fun recoveryChance(immuneRate: Double) = 1 - (1 - immuneRate)


    private fun changeStatus(newHealthStatus: HealthStatus) {
        val oldHealthStatus = healthStatus
        healthStatus = newHealthStatus
        simulationTracker.updateStatusTracking(oldHealthStatus, newHealthStatus)
    }


    override fun toString(): String {
        return buildString {
            append("Person at $position")
            append(" | Alive: $daysAlive days")
            if (isInfected()) append(" | Infected for: $daysLatent days")
            append(" | Status: $healthStatus")
        }
    }

    fun stayAtHomeOrHospital(numberOfInfectedAround: Int, currentPolicy: GovernmentPolicy) {
        if (healthStatus == HealthStatus.DEAD) return
        // Przypadek: zdrowy może się zarazić
        // W tym kroku sie nie ruszam, jestem tam gdzie jestem, moge sie jedynie zarazic,
        // sprawdzam jedynie czy powinienem nosic maske bo to zmienia szanse na zarażenie.
        if (healthStatus == HealthStatus.HEALTHY) {
            if (Random.nextDouble() < currentPolicy.maskRate) {
                infect(numberOfInfectedAround, worldConfig.infectiousnessWithMask.value)
            } else {
                infect(numberOfInfectedAround, worldConfig.infectiousness.value)
            }
        }

    }

    fun commute(numberOfInfectedAround: Int, currentPolicy: GovernmentPolicy) {
        // agent podrózuje
        if (healthStatus == HealthStatus.DEAD) return

        if (currentPolicy.areaLockdown ||
            currentPolicy.isolation && healthStatus == HealthStatus.SYMPTOMATIC) {
            // Całkowity lockdown – agent nie wychodzi
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

    private fun checkIfCanBeInfected(currentPolicy: GovernmentPolicy, numberOfInfectedAround: Int) {
        if (healthStatus == HealthStatus.HEALTHY) {
            if (Random.nextDouble() < currentPolicy.maskRate) {
                infect(numberOfInfectedAround, worldConfig.infectiousnessWithMask.value)
            } else {
                infect(numberOfInfectedAround, worldConfig.infectiousness.value)
            }
        }
    }

    fun stayAtDestination(numberOfInfectedAround: Int, currentPolicy: GovernmentPolicy) {
        if (healthStatus == HealthStatus.DEAD) return
        checkIfCanBeInfected(currentPolicy, numberOfInfectedAround)
    }

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
}