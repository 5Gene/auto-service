@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.jvm_lib

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.random.Random

fun main() = runBlocking {
    val numberOfProducers = 3 // This value can be dynamic as per your requirement
    val maxProducers = 10
    val channel = Channel<Int>()

    println(Runtime.getRuntime().availableProcessors())
    // Launch the consumer coroutine
    val consumerJob = launch(Dispatchers.IO) {
        consume(channel)
    }

    // Use a coroutine scope with a limited dispatcher to control the number of concurrent producers
    val producerDispatcher = Executors.newFixedThreadPool(10).asCoroutineDispatcher()
    val producerJobs = List(numberOfProducers) {
        launch(producerDispatcher) {
            produce(channel, it)
        }
    }

    println("producerJobs.joinAll() --------------------")
    // Wait for all producers to finish
    producerJobs.joinAll()
    println("producerJobs.joinAll() -===========================")

    // Signal the consumer to finish
    channel.close()

    // Wait for the consumer to finish
    consumerJob.join()
}

suspend fun produce(channel: Channel<Int>, index: Int) {
    // Simulate producing a byte array
    val data = index // Replace with actual production logic
    delay(Random(100).nextInt(100) + 100L)
    channel.send(data)
    println("${Thread.currentThread().id} xxxxxxxxxxxxxxx $index")
}

suspend fun consume(channel: Channel<Int>) {
    println("consume start -xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
    for (data in channel) {
        println("000000000 ${data}")
        delay(1000)
    }
}