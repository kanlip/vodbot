package com.example.demo.users.adapter.out.persistence;

import com.example.demo.users.domain.Organization;
import com.example.demo.users.port.out.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepository {
    private final SpringDataOrganizationsRepository springRepo;


    @Override
    public Organization findById(UUID id) {
        Optional<OrganizationsEntity> entity = springRepo.findById(id);
        return entity.map(OrganizationMapper.INSTANCE::toDomain).orElse(null);
    }

    @Override
    public List<Organization> findAll() {
        return springRepo.findAll().stream().map(OrganizationMapper.INSTANCE::toDomain).collect(Collectors.toList());
    }

    @Override
    public Organization save(Organization organization) {
        OrganizationsEntity entity = OrganizationMapper.INSTANCE.toEntity(organization);
        OrganizationsEntity saved = springRepo.save(entity);
        return OrganizationMapper.INSTANCE.toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        springRepo.deleteById(id);
    }
}
