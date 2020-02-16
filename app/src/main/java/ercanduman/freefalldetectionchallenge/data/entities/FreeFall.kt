package ercanduman.freefalldetectionchallenge.data.entities

import java.sql.Timestamp

data class FreeFall(
    val timestamp: Timestamp,
    val duration: Long
)