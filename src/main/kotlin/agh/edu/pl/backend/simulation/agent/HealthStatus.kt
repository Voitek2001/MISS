package agh.edu.pl.backend.simulation.agent

enum class HealthStatus(val s: Double, val h: Double) {
    HEALTHY(1.0, 1.0),
    LATENT(1.0, 0.9), // utajony
    ASYMPTOMATIC(0.8, 0.5), // bezobjawowy
    SYMPTOMATIC(0.2, 0.0),
    IMMUNE(1.0, 0.9),
    DEAD(0.0, -2.0)

}