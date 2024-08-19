package info.skyblond.rabbitmq.rpc.demo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.StreamReadFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.reflect.KClass

private val objectMapper = jacksonObjectMapper()

fun serialize(obj: Any): ByteArray {
    val baos = ByteArrayOutputStream()
    objectMapper.writer().writeValue(baos, obj)
    return baos.toByteArray()
}

fun <T : Any> deserialize(ins: InputStream, clazz: KClass<T>): T {
    return objectMapper.reader()
        .without(JsonParser.Feature.AUTO_CLOSE_SOURCE)
        .without(StreamReadFeature.AUTO_CLOSE_SOURCE)
        .readValue(ins, clazz.java)
}

inline fun <reified T : Any> deserialize(ins: InputStream): T {
    return deserialize(ins, T::class)
}