package com.ldhdev.utilityserver.nameless

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/nameless/admin")
class NamelessAdminController(private val repository: ModSessionRepository) {

    @GetMapping
    fun showAdminPage(model: Model): String {

        model.addAttribute("sessions", repository.findAll())

        return "admin"
    }
}