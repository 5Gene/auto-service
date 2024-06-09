package osp.spark.auto.service

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType

const val AUTO_SERVICE_NAME = "com.google.auto.service.AutoService"


val String.lookDown: String
    get() = "👇👇👇👇👇👇👇👇👇👇👇👇👇👇👇 $this 👇👇👇👇👇👇👇👇👇👇👇👇👇👇👇"

val String.lookup: String
    get() = "👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆 $this 👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆"

/**
 * - Create a file named `META-INF/services/<interface>`
 * - For each [AutoService] annotated class for this interface
 * - Create an entry in the file
 */
class AutoServiceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AutoServiceProcessor(environment)
    }
}

class AutoServiceProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    val logger = environment.logger
    val service_impl_map = mutableMapOf<String, MutableList<String>>()
    override fun process(resolver: Resolver): List<KSAnnotated> {
        //多轮的时候process对象是同一个
        logger.info("process ➱ $this")
        //https://kotlinlang.org/docs/ksp-incremental.html#how-it-is-implemented
        val autoServiceClassAnnotations = resolver.getSymbolsWithAnnotation(AUTO_SERVICE_NAME).filterIsInstance<KSClassDeclaration>()
        if (autoServiceClassAnnotations.toList().isEmpty()) {
            return emptyList()
        }
        autoServiceClassAnnotations.forEach {
            logger.info("🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰")
//            logger.warn("➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖")
            //被注解的完整类名
            val beAnnotatedFullClassName = it.qualifiedName!!.asString()
            logger.info("类名 > $beAnnotatedFullClassName")

            //AutoService只有一个参数 class
            //这个类上的所有注解
            //找到AutoService注解
            val autoServiceAnnotation = it.annotations.find { it.annotationType.resolve().declaration.qualifiedName?.asString() == AUTO_SERVICE_NAME }!!
            //找到AutoService(xx:class)的具体参数，找到完整接口名, 这里只支持一个参数
            val argument = autoServiceAnnotation.arguments.first()
            val argumentType = (argument.value as List<*>).first() as KSType
            //service接口名
            val serviceFullName = argumentType.declaration.qualifiedName!!.asString()
            logger.info("接口名 > $serviceFullName")

            service_impl_map.getOrPut(serviceFullName) {
                mutableListOf()
            }.add(beAnnotatedFullClassName)

            logger.info("@AutoService($serviceFullName)")
            logger.info(beAnnotatedFullClassName)
            logger.info("🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰")
//            logger.warn("➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖")
        }
        generateServicesFile()
        return emptyList()
    }

    private fun generateServicesFile() {
        service_impl_map.forEach { service, impls ->
            val resourceFile = "META-INF/services/$service"
//            val dependencies = Dependencies(true, *ksFiles.toTypedArray())
            logger.warn(service.lookDown)
            environment.codeGenerator.createNewFile(
                Dependencies(false), "", resourceFile, ""
            ).bufferedWriter().use { writer ->
                impls.forEach {
                    writer.write(it)
                    writer.newLine()
                    logger.warn("➤ ➱ ➾ ➜ ➣  $it")
                }
            }
            logger.warn(service.lookup)
        }
    }
}