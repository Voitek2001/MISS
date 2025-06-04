package agh.edu.pl.backend.simulation.tracker

import agh.edu.pl.backend.simulation.agent.HealthStatus

class SimulationTracker(
    var totalNumberOfAlive: Int
) : LatentObserver, DeathObserver, ImmuneObserver, SymptomaticObserver, AsymptomaticObserver {

    var numberOfHealthy = totalNumberOfAlive
    var numberOfLatent = 0
    var numberOfDead = 0
    var numberOfImmune = 0
    var numberOfAsymptomatic = 0
    var numberOfSymptomatic = 0



    fun updateStatusTracking(oldHealthStatus: HealthStatus, newHealthStatus: HealthStatus) {
        removeOldStatus(oldHealthStatus)
        addNewStatus(newHealthStatus)
    }

    override fun updateAsymptomatic() {
        numberOfAsymptomatic++
        numberOfLatent--
    }

    override fun updateSymptomatic() {
        numberOfSymptomatic++
        numberOfLatent--
    }

    override fun updateLatent(oldHealthStatus: HealthStatus) {
        removeOldStatus(oldHealthStatus)
        addNewStatus(HealthStatus.LATENT)
    }

    private fun addNewStatus(newHealthStatus: HealthStatus) {
        when (newHealthStatus) {
            HealthStatus.LATENT -> numberOfLatent++
            HealthStatus.IMMUNE -> numberOfImmune++
            HealthStatus.HEALTHY -> numberOfHealthy++
            HealthStatus.DEAD -> {
                numberOfDead++
                totalNumberOfAlive--
            }
            HealthStatus.SYMPTOMATIC -> numberOfSymptomatic++
            HealthStatus.ASYMPTOMATIC -> numberOfAsymptomatic++
        }
    }

    private fun removeOldStatus(oldHealthStatus: HealthStatus) {
        when (oldHealthStatus) {
            HealthStatus.LATENT -> numberOfLatent--
            HealthStatus.IMMUNE -> numberOfImmune--
            HealthStatus.HEALTHY -> numberOfHealthy--
            HealthStatus.DEAD -> throw CannotChangeStatus()
            HealthStatus.SYMPTOMATIC -> numberOfSymptomatic--
            HealthStatus.ASYMPTOMATIC -> numberOfAsymptomatic--
        }
    }

    override fun updateDeath(oldHealthStatus: HealthStatus) {
        numberOfDead++
        if (oldHealthStatus == HealthStatus.SYMPTOMATIC) {
            numberOfSymptomatic--
        } else {
            numberOfAsymptomatic--
        }
        totalNumberOfAlive--
    }

    override fun updateImmune(oldHealthStatus: HealthStatus) {
        if (oldHealthStatus == HealthStatus.SYMPTOMATIC) {
            numberOfSymptomatic--
        } else {
            numberOfAsymptomatic--
        }
        numberOfImmune++
    }

    fun getDescriptionString(): String {
        return """
            ┌────────────────────────────────┐
            │       Simulation Tracker       │
            ├────────────────────────────────┤
            │ Total Alive  : $totalNumberOfAlive   │
            │ Healthy      : $numberOfHealthy   │
            │ Latent       : $numberOfLatent   │
            │ Asymptomatic : $numberOfAsymptomatic   │
            │ Symptomatic  : $numberOfSymptomatic   │
            │ Immune       : $numberOfImmune   │
            │ Dead         : $numberOfDead   │
            └────────────────────────────────┘
        """.trimIndent()
    }

}