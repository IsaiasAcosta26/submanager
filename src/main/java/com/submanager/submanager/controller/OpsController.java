package com.submanager.submanager.controller;

import com.submanager.submanager.jobs.RenewalReminderJob;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ops")
public class OpsController {

    private final RenewalReminderJob job;
    public OpsController(RenewalReminderJob job) { this.job = job; }

    @PostMapping("/run-reminders")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void runReminders() {
        job.runInternal("manual");
    }
}
