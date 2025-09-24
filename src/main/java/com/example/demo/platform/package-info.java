@ApplicationModule(
    allowedDependencies = {"shared", "users", "order", "recording"}
)
package com.example.demo.platform;

import org.springframework.modulith.ApplicationModule;