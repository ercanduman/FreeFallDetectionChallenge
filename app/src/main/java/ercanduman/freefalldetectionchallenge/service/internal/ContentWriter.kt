package ercanduman.freefalldetectionchallenge.service.internal

import ercanduman.freefalldetectionchallenge.data.entities.FreeFall

interface ContentWriter {
    fun content(freeFall: FreeFall)
}