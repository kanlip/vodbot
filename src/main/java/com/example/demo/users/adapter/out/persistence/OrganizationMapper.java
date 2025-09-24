package com.example.demo.users.adapter.out.persistence;

import com.example.demo.users.domain.Organization;

import java.time.Instant;
import java.util.UUID;

public class OrganizationMapper {

    public static final OrganizationMapper INSTANCE = new OrganizationMapper();

    public Organization toDomain(OrganizationsEntity entity) {
        if (entity == null) return null;

        return Organization.builder()
                .id(entity.getId())
                .name(entity.getName())
                .status(entity.getStatus())
                .plan(mapPlanFromEntity(entity.getPlan()))
                .billing(mapBillingFromEntity(entity.getBilling()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public OrganizationsEntity toEntity(Organization org) {
        if (org == null) return null;

        OrganizationsEntity entity = new OrganizationsEntity();
        entity.setId(org.getId());
        entity.setName(org.getName());
        entity.setStatus(org.getStatus());
        entity.setPlan(mapPlanToEntity(org.getPlan()));
        entity.setBilling(mapBillingToEntity(org.getBilling()));
        entity.setCreatedAt(org.getCreatedAt());
        entity.setUpdatedAt(org.getUpdatedAt());

        return entity;
    }

    private Organization.Plan mapPlanFromEntity(OrganizationsEntity.Plan entityPlan) {
        if (entityPlan == null || entityPlan.getCode() == null) {
            return Organization.Plan.FREE;
        }
        try {
            return Organization.Plan.valueOf(entityPlan.getCode().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Organization.Plan.FREE;
        }
    }

    private OrganizationsEntity.Plan mapPlanToEntity(Organization.Plan plan) {
        if (plan == null) plan = Organization.Plan.FREE;

        OrganizationsEntity.Plan entityPlan = new OrganizationsEntity.Plan();
        entityPlan.setCode(plan.name());
        entityPlan.setStartedAt(Instant.now());
        // Set expires to 1 year from now for paid plans, null for free
        if (plan != Organization.Plan.FREE) {
            entityPlan.setExpiresAt(Instant.now().plusSeconds(365 * 24 * 3600));
        }
        return entityPlan;
    }

    private Organization.Billing mapBillingFromEntity(OrganizationsEntity.Billing entityBilling) {
        if (entityBilling == null) return null;

        return Organization.Billing.builder()
                .contactEmail(entityBilling.getContactEmail())
                .billingCycle(entityBilling.getBillingCycle())
                .currency(entityBilling.getCurrency())
                .build();
    }

    private OrganizationsEntity.Billing mapBillingToEntity(Organization.Billing billing) {
        if (billing == null) return null;

        OrganizationsEntity.Billing entityBilling = new OrganizationsEntity.Billing();
        entityBilling.setContactEmail(billing.getContactEmail());
        entityBilling.setBillingCycle(billing.getBillingCycle());
        entityBilling.setCurrency(billing.getCurrency());

        return entityBilling;
    }
}
