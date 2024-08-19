package info.skyblond.rabbitmq.rpc.demo

import reactor.rabbitmq.Sender
import kotlin.reflect.KClass

fun main() {
    val stub = DemoRpcStub(Sender())

    val result = stub.foo(DemoFooRequest("Hello"))
    println(result)

}