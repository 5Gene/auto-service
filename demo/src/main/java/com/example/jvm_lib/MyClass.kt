package com.example.jvm_lib

import com.example.kspt.Testt

@Testt("class", 11)
class MyClass {

    @get:Testt("only get")
    val mem: Int = 100

    @Testt("ano_pro", 11)
    val ano_pro: Int = 1

    @set:Testt("only set")
    var nn: String = ""
        get() {
            return ""
        }
        set(value) {
            field = value
        }

    @Testt("method:ttt", data = 0)
    fun tttWithDefault(param1: String = "default", num: Number) {
        println("000")
    }

    @Testt("fanxing", data = 10)
    inline fun <reified D : Any> fanxingFun() {
        println("xxxx")
    }

    fun normalFun() {
        println("0909")
    }
}


@Testt("class", data = 100)
class PacTest32 {
    @Testt("filed, a", data = 90)
    var a_pro = 90

    fun notfun() {
        println("000")
    }

    @Testt("method:ttt", data = 0)
    fun ttt(param1: String) {
        println("000")
    }
}