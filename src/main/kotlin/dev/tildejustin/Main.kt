package dev.tildejustin

import net.fabricmc.mappingio.*
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import java.io.*
import java.nio.file.*
import java.util.zip.*
import kotlin.io.path.readBytes

fun main() {
    val treeA = MemoryMappingTree()
    val treeB = MemoryMappingTree()
    val mergedMappings = MemoryMappingTree()
    mergedMappings.setSrcNamespace("official")
    mergedMappings.setDstNamespaces(listOf("intermediary", "named"))

    MappingReader.read(Path.of("1.12.2+build.202206171821-mergedv2.tiny"), treeA)
    MappingReader.read(Path.of("1.12.2+build.536-mergedv2.tiny"), treeB)

    for (classA in treeA.classes) {
        val classB = treeB.getClass(classA.srcName) ?: continue
        // am I allowed to reuse the ClassMapping?
        val resultClass = mergedMappings.addClass(classA)
        // intermediary from A
        resultClass.setDstName(classA.getDstName(0), 0)
        // named from B
        resultClass.setDstName(classB.getDstName(1), 1)

        for (fieldA in classA.fields) {
            val fieldB = classB.getField(fieldA.srcName, fieldA.srcDesc)
            if (fieldB == null) {
                resultClass.removeField(fieldA.srcName, fieldA.srcDesc)
                continue
            }
            val resultField = resultClass.addField(fieldA)
            // this first one is probably unnecessary, but good to be safe
            resultField.setDstName(fieldA.getDstName(0), 0)
            resultField.setDstName(fieldB.getDstName(1), 1)
        }

        for (methodA in classA.methods) {
            val methodB = classB.getMethod(methodA.srcName, methodA.srcDesc)
            if (methodB == null) {
                resultClass.removeField(methodA.srcName, methodA.srcDesc)
                continue
            }
            val resultMethod = resultClass.addMethod(methodA)
            resultMethod.setDstName(methodA.getDstName(0), 0)
            resultMethod.setDstName(methodB.getDstName(1), 1)
        }
    }

    val outputDir = Path.of("1.12.2+build.202206171821-1.12.2+build.536-translation.tiny")
    val writer = MappingWriter.create(outputDir, MappingFormat.TINY_2_FILE)!!
    mergedMappings.accept(writer)

    val zipFile = ZipOutputStream(
        BufferedOutputStream(
            FileOutputStream(
                "1.12.2+build.202206171821-1.12.2+build.536-translation.jar"
            )
        )
    )
    zipFile.putNextEntry(ZipEntry("mappings/mappings.tiny"))
    zipFile.write(outputDir.readBytes())
    zipFile.close()

    val checkedOutput = MemoryMappingTree()
    MappingReader.read(outputDir, checkedOutput)
    assert(mergedMappings == checkedOutput)
}
