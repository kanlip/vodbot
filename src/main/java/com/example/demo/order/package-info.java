@ApplicationModule(
    allowedDependencies = {"webhook :: events", "shared"}
)
package com.example.demo.order;

import org.springframework.modulith.ApplicationModule;