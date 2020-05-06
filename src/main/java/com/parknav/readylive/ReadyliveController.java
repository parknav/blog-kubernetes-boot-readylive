package com.parknav.readylive;

import com.github.kkuegler.PermutationBasedHumanReadableIdGenerator;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Timer;
import java.util.TimerTask;

@RestController
public class ReadyliveController {

    private String id;
    private boolean temporarilyBroken = false;
    private boolean broken = false;

    private final ApplicationEventPublisher eventPublisher;

    public ReadyliveController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void initId() {
        this.id = new PermutationBasedHumanReadableIdGenerator().generate();
    }

    @GetMapping("id")
    public String hello() {
        if (temporarilyBroken) {
            // We don't want to be bothered in the next 2 seconds.
            AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
            return "Service temporarily disabled";
        } else if (broken) {
            // We realize something broke and we cannot recover from it. We want Kubernetes to restart the container.
            AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
            throw new RuntimeException("Service is broken");
        } else {
            return id;
        }
    }

    @GetMapping("disable")
    public String disable() {
        temporarilyBroken = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // Back to normal
                temporarilyBroken = false;
                AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
            }
        }, 4000L);
        return "Disabled 4s";
    }

    @GetMapping("break")
    public String breakIt() {
        broken = true;
        return "Done. All broken now.";

    }

}
