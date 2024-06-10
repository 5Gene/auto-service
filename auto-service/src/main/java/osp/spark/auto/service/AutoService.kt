package osp.spark.auto.service

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import java.io.File
import java.io.FileWriter

const val AUTO_SERVICE_NAME = "com.google.auto.service.AutoService"

fun KSType.fullClassName() = declaration.qualifiedName!!.asString()

val String.lookDown: String
    get() = "👇👇👇👇👇👇👇👇👇👇👇👇👇👇👇 $this 👇👇👇👇👇👇👇👇👇👇👇👇👇👇👇"

val String.lookup: String
    get() = "👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆 $this 👆👆👆👆👆👆👆👆👆👆👆👆👆👆👆"

//val logLevel = LogLevel.values().first {
//    project.logger.isEnabled(it)
//}
//cfg.logLevel.value(logLevel)

//org.gradle.logging.level=info
fun String.logInfo(logger: KSPLogger) {
    logger.info(this)
}

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

@Suppress("UNCHECKED_CAST")
fun SymbolProcessorEnvironment.getGeneratedFiles(): Collection<File> {
    if (codeGenerator.generatedFile.isEmpty()) {
        "$ $this ➱ environment.codeGenerator.generatedFile > isEmpty !! ".logInfo(logger)
        val fileMap = codeGenerator::class.java.getDeclaredField("fileMap")
        fileMap.isAccessible = true
        val filesMap = fileMap.get(codeGenerator) as Map<String, File>
        return filesMap.values
    }
    return codeGenerator.generatedFile
}

class AutoServiceProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger
    private var roundIndex = 0

    override fun process(resolver: Resolver): List<KSAnnotated> {
        roundIndex++
        //多轮的时候process对象是同一个
        ">$roundIndex process ➱ $this".logInfo(logger)

        //https://kotlinlang.org/docs/ksp-incremental.html#how-it-is-implemented
        val symbolsWithAnnotation = resolver.getSymbolsWithAnnotation(AUTO_SERVICE_NAME)
        if (symbolsWithAnnotation.toList().isEmpty()) {
            return emptyList()
        }
        val invalidateAnnotations = symbolsWithAnnotation.filter { !it.validate() }.toList()

        val autoServiceClassAnnotations = symbolsWithAnnotation.filter { it.validate() }.filterIsInstance<KSClassDeclaration>()
        val serviceImplMap = mutableMapOf<String, MutableList<String>>()
        val originatingFiles = mutableSetOf<KSFile>()
        autoServiceClassAnnotations.forEach {
            "🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰".logInfo(logger)
//          "➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖".logInfo(logger)
            //被注解的完整类名
            val beAnnotatedFullClassName = it.qualifiedName!!.asString()
            ">$roundIndex 类名 > $beAnnotatedFullClassName".logInfo(logger)

            //AutoService只有一个参数 class
            //这个类上的所有注解
            //找到AutoService注解
            val autoServiceAnnotation = it.annotations.find { it.annotationType.resolve().fullClassName() == AUTO_SERVICE_NAME }!!
            //找到AutoService(xx:class)的具体参数，找到完整接口名, 这里只支持一个参数
            val argument = autoServiceAnnotation.arguments.first()
            //每个注解支持多个参数，每个参数(key=value)这里value也支持多个，
            // AutoService(Class<?>[] value())实际上支持多个class
            val serviceFullNames = mutableListOf<String>()
            (argument.value as List<*>).map { it as KSType }.forEach { argType ->
                //service接口名
                val serviceFullName = argType.fullClassName()
                serviceFullNames.add(serviceFullName)
                ">$roundIndex 接口名 > $serviceFullName".logInfo(logger)
                serviceImplMap.getOrPut(serviceFullName) {
                    mutableListOf()
                }.add(beAnnotatedFullClassName)
            }
            ">$roundIndex @AutoService(${serviceFullNames.joinToString()})".logInfo(logger)
            ">$roundIndex $beAnnotatedFullClassName".logInfo(logger)
            "🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰🟰".logInfo(logger)
//           "➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖➖".logInfo(logger)
            it.containingFile!!.fileName.logInfo(logger)
            originatingFiles.add(it.containingFile!!)
        }
        if (serviceImplMap.isNotEmpty()) {
            generateServicesFile(serviceImplMap, originatingFiles.toList())
        }
        return invalidateAnnotations
    }

    private fun generateServicesFile(serviceImpls: Map<String, MutableList<String>>, originatingFiles: List<KSFile>) {
        serviceImpls.forEach { (service, impls) ->
            val resourceFile = "META-INF/services/$service"
            logger.warn(">$roundIndex ➤  $resourceFile")
            logger.warn(service.lookDown)
            val createdFile = environment.getGeneratedFiles().find { it.name == service }
            if (createdFile != null) {
                //process生成注解后第二轮新增
                FileWriter(createdFile, true).use { writer ->
                    impls.forEach {
                        writer.write(it)
                        writer.write(System.lineSeparator())
                        logger.warn("➤ ➱ $it")
                    }
                }
            } else {
                //aggregating=true意味着输出可能潜在地依赖于新信息，这些信息可能来自新文件，或已更改的现有文件。
                //aggregating=false意味着处理器确信信息仅来自某些输入文件，而绝不会来自其他文件或新文件。
                val constructor = Dependencies::class.java.getDeclaredConstructor(Boolean::class.java, Boolean::class.java, List::class.java)
                constructor.isAccessible = true
                //aggregating=true, originatingFiles，生成的文件需要依赖多个输入文件的时候使用
                // 表示生成的文件和originatingFiles有关只要originatingFiles之一修改删除，就会全部扫描刷新
                // 删除originatingFiles其中之一，会重新全部扫描刷新，不会遗漏，新增任意文件和修改任意文件也会全部扫描
                //aggregating=false
                // 新增一个AutoService注解的类只会扫描新增的一个，会导致复写整个文件，会丢失之前收集并写好的AutoService丢失
                // 删除一个AutoService不会触发扫描，会导致问题

                environment.codeGenerator.createNewFile(
                    constructor.newInstance(false, true, originatingFiles), "", resourceFile, ""
                ).bufferedWriter().use { writer ->
                    impls.forEach {
                        writer.write(it)
                        writer.newLine()
                        logger.warn("➤ ➱ ➾ ➜ ➣  $it")
                    }
                }
            }

            logger.warn(service.lookup)
        }
    }
}