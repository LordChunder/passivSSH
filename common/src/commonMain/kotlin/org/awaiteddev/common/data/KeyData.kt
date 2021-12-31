package org.awaiteddev.common.data

import java.io.Serializable

data class KeyData(
    val name:String,
    val userName:String,
    val publicKey :String,
    val privateKeyPath: String
) : Serializable
