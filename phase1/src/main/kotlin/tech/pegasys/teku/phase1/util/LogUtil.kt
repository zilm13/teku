package tech.pegasys.teku.phase1.util

import tech.pegasys.teku.phase1.onotole.phase1.BLSSignature
import tech.pegasys.teku.phase1.onotole.phase1.Root

fun printRoot(root: Root): String = root.toString().substring(0..10)
fun printSignature(signature: BLSSignature): String = signature.toString().substring(0..18)
