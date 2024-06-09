package com.example.jvm_lib

import com.example.kspt.Testt


@Testt("class", 110)
class MyClassAnno {
    @Testt("method:ttt", data = 0)
    fun tttAnn(param1: String) {
        println("000")
    }
}

@Testt("class", 110)
class MyClassAnno52 {
    @Testt("method:ttt", data = 0)
    fun tttAnn(param1: String) {
        println("000")
    }
}