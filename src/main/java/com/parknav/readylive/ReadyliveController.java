package com.parknav.readylive;

import com.github.kkuegler.PermutationBasedHumanReadableIdGenerator;
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

    @PostConstruct
    public void initId() {
        this.id = new PermutationBasedHumanReadableIdGenerator().generate();
    }

    @GetMapping("id")
    public String hello() {
        if (temporarilyBroken) {
            return "Service temporarily disabled";
        } else if (broken) {
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
