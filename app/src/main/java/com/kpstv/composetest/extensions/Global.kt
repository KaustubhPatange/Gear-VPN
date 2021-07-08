package com.kpstv.composetest.extensions

import kotlin.random.Random

fun getRandomInt(max: Int = 400, offset: Int = 150) = Random.nextInt(max) + offset