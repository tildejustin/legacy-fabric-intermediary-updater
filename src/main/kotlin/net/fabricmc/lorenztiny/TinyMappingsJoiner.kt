/*
 * This file is part of lorenz-tiny, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2020 FabricMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fabricmc.lorenztiny

import net.fabricmc.mappingio.tree.MappingTree
import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.io.MappingsReader

/**
 * A [mappings reader][MappingsReader] for reading mappings
 * from across two [tiny trees][MappingTree], using a namespace
 * present in both to match.
 *
 * @author Jamie Mansfield
 * @since 2.0.0
 */
class TinyMappingsJoiner(
    private val treeA: MappingTree, private val from: String, private val matchA: String,
    private val treeB: MappingTree, private val to: String, private val matchB: String
) : MappingsReader() {
    constructor(
        treeA: MappingTree, from: String,
        treeB: MappingTree, to: String,
        match: String
    ) : this(treeA, from, match, treeB, to, match)

    override fun read(mappings: MappingSet): MappingSet {
        // These maps have matched name -> definition from a
        val classes: MutableMap<String?, MappingTree.ClassMapping> = HashMap()
        val fields: MutableMap<String?, MappingTree.FieldMapping> = HashMap()
        val methods: MutableMap<String?, MappingTree.MethodMapping> = HashMap()

        for (klass in treeB.classes) {
            classes[klass.getName(this.matchA)] = klass

            for (field in klass.fields) {
                fields[field.getName(this.matchA)] = field
            }

            for (method in klass.methods) {
                methods[method.getName(this.matchA)] = method
            }
        }

        for (classA in treeA.classes) {
            val classB = classes[classA.getName(this.matchB)]

            val klass = mappings.getOrCreateClassMapping(classA.getName(this.from))
            if (classB != null) {
                val deobfName = classB.getName(this.to)

                if (deobfName != null) {
                    klass.setDeobfuscatedName(deobfName)
                }
            }

            for (fieldA in classA.fields) {
                val fieldB = fields[fieldA.getName(this.matchB)]

                if (fieldB != null) {
                    val deobfName = fieldB.getName(this.to)

                    if (deobfName != null) {
                        klass.getOrCreateFieldMapping(fieldA.getName(this.from), fieldA.getDesc(this.from)).setDeobfuscatedName(deobfName)
                    }
                }
            }

            for (methodA in classA.methods) {
                val methodB = methods[methodA.getName(this.matchB)]

                if (methodB != null) {
                    val deobfName = methodB.getName(this.to)

                    if (deobfName != null) {
                        klass.getOrCreateMethodMapping(methodA.getName(this.from), methodA.getDesc(this.from)).setDeobfuscatedName(deobfName)
                    }
                }
            }
        }

        return mappings
    }

    override fun close() {
    }
}
