package com.ldhdev.utilityserver.nameless

import java.security.Principal

data class NamelessUser(private val id: String) : Principal {
    override fun getName() = id
}
