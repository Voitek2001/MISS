package agh.edu.pl.backend.simulation.tracker

class SimulationTracker(
    var totalNumberOfAlive: Int
) : InfectionObserver, DeathObserver, ImmuneObserver {

    var numberOfHealthy = totalNumberOfAlive
    var numberOfInfected = 0
    var numberOfDead = 0
    var numberOfImmune = 0

    override fun updateInfection() {
        numberOfInfected++
        numberOfHealthy--
    }

    override fun updateDeath() {
        numberOfDead++
        numberOfInfected--
        totalNumberOfAlive--
    }

    override fun updateImmune() {
        numberOfImmune++
        numberOfInfected--
    }

    fun getDescriptionString(): String {
        return """
            ┌────────────────────────────────┐
            │       Simulation Tracker       │
            ├────────────────────────────────┤
            │ Total Alive  : $totalNumberOfAlive   │
            │ Healthy      : $numberOfHealthy   │
            │ Infected     : $numberOfInfected   │
            │ Immune       : $numberOfImmune   │
            │ Dead         : $numberOfDead   │
            └────────────────────────────────┘
        """.trimIndent()
    }

}