package com.example.ksptt.auto

import com.google.auto.service.AutoService

@AutoService(TestService::class)
class TestServiceImpl : TestService {
    override fun test(msg: String) {
        println(msg)
    }
}

@AutoService(TestService::class)
class MockServiceImpl : TestService {
    override fun test(msg: String) {
        println(msg)
    }
}

@AutoService(TestService::class)
class FakeServiceImpl : TestService {
    override fun test(msg: String) {
        println(msg)
    }
}