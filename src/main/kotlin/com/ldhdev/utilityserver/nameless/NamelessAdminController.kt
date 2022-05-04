package com.ldhdev.utilityserver.nameless

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/nameless/admin")
class NamelessAdminController(private val repository: ModSessionRepository)