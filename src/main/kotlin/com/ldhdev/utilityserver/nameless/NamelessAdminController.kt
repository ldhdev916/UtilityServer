package com.ldhdev.utilityserver.nameless

import com.ldhdev.utilityserver.db.ModPlayerSession
import com.ldhdev.utilityserver.db.ModSessionRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/nameless/admin")
class NamelessAdminController(private val repository: ModSessionRepository) {
    @GetMapping("sessions")
    @ResponseBody
    fun showAllSessions(): List<ModPlayerSession> = repository.findAll()
}