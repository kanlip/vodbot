package com.example.demo.users.port.out;

import com.example.demo.users.domain.Organization;
import java.util.List;
import java.util.UUID;

public interface OrganizationRepository {
    Organization findById(UUID id);
    List<Organization> findAll();
    Organization save(Organization organization);
    void deleteById(UUID id);
}

