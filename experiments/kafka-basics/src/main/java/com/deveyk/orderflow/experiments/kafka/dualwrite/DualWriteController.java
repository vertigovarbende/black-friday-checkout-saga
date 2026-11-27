package com.deveyk.orderflow.experiments.kafka.dualwrite;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/dualwrite")
public class DualWriteController {

    private final DualWriteOrders orders;
    private final DualWriteCheck check;

    public DualWriteController(final DualWriteOrders orders, final DualWriteCheck check) {
        this.orders = orders;
        this.check = check;
    }

    @PostMapping("/orders")
    public Map<String, Object> place(@RequestParam final String flow,
                                     @RequestParam(defaultValue = DualWriteOrders.NONE) final String fault,
                                     @RequestParam(defaultValue = "true") final boolean ack,
                                     @RequestParam(defaultValue = "default") final String run) {

        return orders.place(flow, fault, ack, run);
    }

    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam final String path,
                                     @RequestParam(defaultValue = "default") final String run) {

        return check.check(path, run);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> badRequest(final IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }
}
