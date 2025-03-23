package agh.edu.pl.backend.simulation.utils


@JvmInline
value class Percentage private constructor(val value: Double) {
    companion object {
        fun from(value: Double): Percentage? = if (value in 0.0..1.0) Percentage(value) else null
    }
}