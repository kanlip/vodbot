package com.example.demo.users;

import org.springframework.modulith.ApplicationModule;
import org.springframework.modulith.NamedInterface;

@ApplicationModule(
        displayName = "Users Module",
        allowedDependencies = {}
)
@NamedInterface("users")
public class UsersModule {
}
