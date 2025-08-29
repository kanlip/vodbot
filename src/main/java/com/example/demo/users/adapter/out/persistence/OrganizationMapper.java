package com.example.demo.users.adapter.out.persistence;

import com.example.demo.users.domain.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Mapper
interface OrganizationMapper {

    OrganizationMapper INSTANCE = org.mapstruct.factory.Mappers.getMapper(OrganizationMapper.class);
    Organization toDomain(OrganizationsEntity entity);
    OrganizationsEntity toEntity(Organization org);
}

//public class OrganizationMapper {
//    @Mapper
//    interface OrganizationMapper {
//        Organization toDomain(OrganizationsEntity entity);
//        OrganizationsEntity toEntity(Organization org);
//    }
//    public static Organization toDomain(OrganizationsEntity entity) {
//        if (entity == null) return null;
//        Organization org = new Organization();
//        org.setId(entity.getId() != null ? entity.getId().toString() : null);
//        org.setName(entity.getName());
//        org.setStatus(entity.getStatus());
//        if (entity.getPlan() != null) {
//            Organization.Plan plan = new Organization.Plan();
//            plan.setCode(entity.getPlan().getCode());
//            plan.setStartedAt(entity.getPlan().getStartedAt() != null ? Date.from(entity.getPlan().getStartedAt()) : null);
//            plan.setExpiresAt(entity.getPlan().getExpiresAt() != null ? Date.from(entity.getPlan().getExpiresAt()) : null);
//            org.setPlan(plan);
//        }
//        if (entity.getBilling() != null) {
//            Organization.Billing billing = new Organization.Billing();
//            billing.setContactEmail(entity.getBilling().getContactEmail());
//            billing.setBillingCycle(entity.getBilling().getBillingCycle());
//            billing.setCurrency(entity.getBilling().getCurrency());
//            org.setBilling(billing);
//        }
//        org.setCreatedAt(entity.getCreatedAt() != null ? Date.from(entity.getCreatedAt()) : null);
//        org.setUpdatedAt(entity.getUpdatedAt() != null ? Date.from(entity.getUpdatedAt()) : null);
//        return org;
//    }
//
//    public static OrganizationsEntity toEntity(Organization org) {
//        if (org == null) return null;
//        OrganizationsEntity entity = new OrganizationsEntity();
//        if (org.getId() != null) entity.setId(UUID.fromString(org.getId()));
//        entity.setName(org.getName());
//        entity.setStatus(org.getStatus());
//        if (org.getPlan() != null) {
//            OrganizationsEntity.Plan plan = new OrganizationsEntity.Plan();
//            plan.setCode(org.getPlan().getCode());
//            plan.setStartedAt(org.getPlan().getStartedAt() != null ? org.getPlan().getStartedAt().toInstant() : null);
//            plan.setExpiresAt(org.getPlan().getExpiresAt() != null ? org.getPlan().getExpiresAt().toInstant() : null);
//            entity.setPlan(plan);
//        }
//        if (org.getBilling() != null) {
//            OrganizationsEntity.Billing billing = new OrganizationsEntity.Billing();
//            billing.setContactEmail(org.getBilling().getContactEmail());
//            billing.setBillingCycle(org.getBilling().getBillingCycle());
//            billing.setCurrency(org.getBilling().getCurrency());
//            entity.setBilling(billing);
//        }
//        entity.setCreatedAt(org.getCreatedAt() != null ? org.getCreatedAt().toInstant() : null);
//        entity.setUpdatedAt(org.getUpdatedAt() != null ? org.getUpdatedAt().toInstant() : null);
//        return entity;
//    }
//}
